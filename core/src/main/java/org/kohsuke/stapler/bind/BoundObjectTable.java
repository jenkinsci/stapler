/*
 * Copyright (c) 2004-2010, Kohsuke Kawaguchi
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided
 * that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice, this list of
 *       conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright notice, this list of
 *       conditions and the following disclaimer in the documentation and/or other materials
 *       provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS
 * OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
 * AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER
 * IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF
 * THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.kohsuke.stapler.bind;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.kohsuke.stapler.Ancestor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerFallback;
import org.kohsuke.stapler.StaplerRequest2;
import org.kohsuke.stapler.StaplerResponse2;

/**
 * Objects exported and bound by JavaScript proxies.
 *
 * TODO: think about some kind of eviction strategy, beyond the session eviction.
 * Maybe it's not necessary, I don't know.
 *
 * @author Kohsuke Kawaguchi
 */
public class BoundObjectTable implements StaplerFallback {

    public static boolean isValidJavaScriptIdentifier(String variableName) {
        // Ultimately this will be used as a JS identifier, so we need (a subset of) what's valid there.
        // The primary purpose of this check however is to prevent injection attacks.
        return variableName.matches("^[a-zA-Z_][a-zA-Z0-9_]*$");
    }

    public static boolean isValidJavaIdentifier(String name) {
        if (name == null || name.isBlank()) {
            return false;
        }
        if (!Character.isJavaIdentifierStart(name.charAt(0)) || Character.codePointAt(name, 0) > 255) {
            return false;
        }
        // Limit characters to legal Java identifier parts in Latin-1 that aren't ignorable
        return name.substring(1)
                .chars()
                .allMatch(it -> Character.isJavaIdentifierPart(it) && !Character.isIdentifierIgnorable(it) && it < 256);
    }

    /**
     * This serves the script content for a bound object. Support CSP-compatible st:bind and similar methods of making
     * objects accessible to JS.
     *
     * @param req The request
     * @param rsp The response
     * @param var the variable name to assign the Stapler proxy to
     * @param methods the list of methods (needed for {@link WithWellKnownURL})
     * @throws IOException If an I/O error occurs
     */
    public void doScript(
            StaplerRequest2 req, StaplerResponse2 rsp, @QueryParameter String var, @QueryParameter String methods)
            throws IOException {
        final String boundUrl = req.getRestOfPath();

        if (var == null) {
            return;
        }

        if (!isValidJavaScriptIdentifier(var)) {
            LOGGER.log(Level.FINE, () -> "Rejecting invalid JavaScript identifier: " + var);
            return;
        }

        rsp.setContentType("text/javascript");
        final PrintWriter writer = rsp.getWriter();

        if ("/null".equals(boundUrl)) {
            /* This is the case when the object was null in the first place */
            writer.append(var).append(" = null;");
            return;
        }

        final String script;

        /* If this is not a WithWellKnownURL, look UUID up in bound object table and return null if not found. */
        final String contextAndPrefix = Stapler.getCurrentRequest2().getContextPath() + PREFIX;
        if (boundUrl.startsWith(contextAndPrefix)) {
            final String id = boundUrl.replace(contextAndPrefix, "");
            final Table table = resolve(false);
            if (table == null) {
                rsp.sendError(404);
                return;
            }
            Object object = table.resolve(id);
            if (object == null) {
                /* Support null bound objects */
                writer.append(var).append(" = null;");
                return;
            }
            script = Bound.getProxyScript(boundUrl, object.getClass());
        } else {
            if (methods == null) {
                /* This will result in an empty file rather than an explicit null assignment,
                but it's unexpected to have a WithWellKnownURL without ?methods query parameter. */
                return;
            }
            final String[] methodsArray = methods.split(",");
            if (Arrays.stream(methodsArray).anyMatch(it -> !isValidJavaIdentifier(it))) {
                LOGGER.log(
                        Level.FINE, () -> "Rejecting method list that includes an invalid Java identifier: " + methods);
                // TODO Alternatively, filter out invalid method names and only include valid ones.
                //  Could help with non-malicious but encoding related issues
                return;
            }
            script = Bound.getProxyScript(boundUrl, methodsArray);
        }
        writer.append(var).append(" = ").append(script).append(";");
    }

    @Override
    public Table getStaplerFallback() {
        return resolve(false);
    }

    /**
     * Binds an object temporarily and returns its URL.
     */
    public Bound bind(Object o) {
        return resolve(true).add(new StrongRef(o));
    }

    /**
     * Called from within the request handling of a bound object, to release the object explicitly.
     */
    public void releaseMe() {
        Ancestor eot = Stapler.getCurrentRequest2().findAncestor(BoundObjectTable.class);
        if (eot == null) {
            throw new IllegalStateException("The thread is not handling a request to a abound object");
        }
        String id = eot.getNextToken(0);

        resolve(false).release(id); // resolve(false) can't fail because we are processing this request now.
    }

    /**
     * Obtains a {@link Table} associated with this session.
     */
    private Table resolve(boolean createIfNotExist) {
        HttpSession session = Stapler.getCurrentRequest2().getSession(createIfNotExist);
        if (session == null) {
            return null;
        }

        Table t = (Table) session.getAttribute(Table.class.getName());
        if (t == null) {
            if (createIfNotExist) {
                session.setAttribute(Table.class.getName(), t = new Table());
            } else {
                return null;
            }
        }
        return t;
    }

    /**
     * Explicit call to create the table if one doesn't exist yet.
     */
    public Table getTable() {
        return resolve(true);
    }

    /**
     * Per-session table that remembers all the bound instances.
     */
    public static class Table implements Serializable {
        private final Map<String, Ref> entries = new HashMap<>();

        private synchronized Bound add(Ref ref) {
            final Object target = ref.get();
            if (target instanceof WithWellKnownURL w) {
                String url = w.getWellKnownUrl();
                if (!url.startsWith("/")) {
                    LOGGER.warning("WithWellKnownURL.getWellKnownUrl must start with a slash. But we got " + url
                            + " from " + w);
                }
                return new WellKnownObjectHandle(url, w);
            }

            // Before adding a new entry, clean up any old ones:
            var it = entries.entrySet().iterator();
            while (it.hasNext()) {
                var entry = it.next();
                if (!entry.getValue().valid()) {
                    LOGGER.fine(() -> "removing stale " + entry.getKey() + ": "
                            + entry.getValue().get());
                    it.remove();
                }
            }

            final String id = newId();
            entries.put(id, ref);
            LOGGER.fine(() -> String.format("%s binding %s for %s", toString(), target, id));

            return new Bound() {
                @Override
                public void release() {
                    Table.this.release(id);
                }

                @Override
                public String getURL() {
                    return Stapler.getCurrentRequest2().getContextPath() + PREFIX + id;
                }

                @Override
                public Object getTarget() {
                    return target;
                }

                @Override
                public void generateResponse(StaplerRequest2 req, StaplerResponse2 rsp, Object node)
                        throws IOException, ServletException {
                    rsp.sendRedirect2(getURL());
                }
            };
        }

        public Object getDynamic(String id) {
            return resolve(id);
        }

        /**
         * Releases a particular id from the table.
         * Normally would be called via {@link #releaseMe} which infers the id from the current request.
         */
        public void release(String id) {
            var ref = entries.remove(id);
            LOGGER.fine(() -> "releasing " + id + ": " + (ref != null ? ref.get() : null));
        }

        private synchronized Object resolve(String id) {
            Ref e = entries.get(id);
            if (e == null) {
                LOGGER.fine(() -> toString() + " doesn't have binding for " + id);
                return null;
            }
            return e.get();
        }
    }

    private static final class WellKnownObjectHandle extends Bound {
        private final String url;
        private final Object target;

        WellKnownObjectHandle(String url, Object target) {
            this.url = url;
            this.target = target;
        }

        /**
         * Objects with well-known URLs cannot be released, as their URL bindings are controlled
         * implicitly by the application.
         */
        @Override
        public void release() {}

        @Override
        public String getURL() {
            return Stapler.getCurrentRequest2().getContextPath() + url;
        }

        @Override
        public Object getTarget() {
            return target;
        }

        @Override
        public void generateResponse(StaplerRequest2 req, StaplerResponse2 rsp, Object node)
                throws IOException, ServletException {
            rsp.sendRedirect2(getURL());
        }
    }

    /**
     * Reference that resolves to an object.
     */
    interface Ref extends Serializable {
        Object get();

        boolean valid();
    }

    private static class StrongRef implements Ref {
        private final Object o;
        private final long expiration;

        StrongRef(Object o) {
            this.o = o;
            expiration = System.currentTimeMillis() + EXPIRATION_TIME;
        }

        @Override
        public Object get() {
            return o;
        }

        @Override
        public boolean valid() {
            return /* compatibility */ expiration == 0 || System.currentTimeMillis() < expiration;
        }

        private Object writeReplace() {
            if (o instanceof Serializable) {
                return this;
            } else {
                LOGGER.fine(() -> "Refusing to serialize " + o);
                return new StrongRef(null);
            }
        }
    }

    /**
     * How long (in milliseconds) a {@link StrongRef} remains valid for creation if it is not retrieved.
     */
    private static final long EXPIRATION_TIME = Long.getLong(
            BoundObjectTable.class.getName() + ".EXPIRATION_TIME",
            Duration.ofDays(1).toMillis());

    public static final String PREFIX = "/$stapler/bound/";

    static final String SCRIPT_PREFIX = "/$stapler/bound/script";

    private static final Logger LOGGER = Logger.getLogger(BoundObjectTable.class.getName());

    /**
     * The characters a server id may use: they need no escaping in a URL path, an HTML attribute or a JavaScript string.
     */
    private static final Pattern VALID_SERVER_ID = Pattern.compile("[A-Za-z0-9_.-]{1,64}");

    /**
     * Length of the random part of a bound id, a {@link UUID} in its canonical form.
     */
    private static final int UUID_LENGTH = 36;

    /**
     * The server id prepended (with a {@code -}) to the ids of bound objects, or empty (the default) for bare UUIDs.
     * Read once, when this class loads, from the system property {@code org.kohsuke.stapler.bind.BoundObjectTable.idPrefix}.
     */
    private static String idPrefix =
            initialIdPrefix(System.getProperty(BoundObjectTable.class.getName() + ".idPrefix"));

    private static String initialIdPrefix(@CheckForNull String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        if (!isValidServerId(value)) {
            LOGGER.warning(() -> "Ignoring invalid " + BoundObjectTable.class.getName() + ".idPrefix " + value
                    + ": it must match " + VALID_SERVER_ID.pattern());
            return "";
        }
        return value;
    }

    private static boolean isValidServerId(String value) {
        return VALID_SERVER_ID.matcher(value).matches();
    }

    /**
     * The server id this server prepends to the ids of the objects it binds.
     *
     * <p>Bound objects live in the session of the server that bound them and cannot be serialized.
     * When several servers sit behind one load balancer and share their users,
     * each can be started with a different prefix
     * (system property {@code org.kohsuke.stapler.bind.BoundObjectTable.idPrefix},
     * up to 64 characters from {@code [A-Za-z0-9_.-]}),
     * so that a request for {@code /$stapler/bound/<id>} which reaches another server
     * can be routed (by a filter or a proxy) to the one holding the object; see {@link #serverIdOf(String)}.
     * The property is read once, when this class loads; an invalid value is logged and ignored.
     *
     * <p>The prefix is sent to browsers as part of every bound URL, so it should not be anything secret
     * such as an internal host name you do not want to disclose.
     *
     * @return the prefix, or an empty string if ids are not prefixed (the default)
     */
    @NonNull
    public static String getIdPrefix() {
        return idPrefix;
    }

    /**
     * For tests.
     */
    static void setIdPrefix(@NonNull String prefix) {
        if (!prefix.isEmpty() && !isValidServerId(prefix)) {
            throw new IllegalArgumentException("Invalid server id: " + prefix);
        }
        idPrefix = prefix;
    }

    /**
     * Parses the server id out of the id of a bound object, the inverse of how this class makes ids:
     * a prefixed id is {@code <server id>-<UUID>}, and the server id may itself contain {@code -}.
     *
     * <p>The id comes from the request URL, so the result is an untrusted routing hint chosen by the client.
     * It must never be used to decide whether a request is authorized:
     * a bound object can only be reached from the session that bound it, on the server that holds it.
     *
     * @param id the id of a bound object, the path segment after {@link #PREFIX}
     * @return the server id, or {@code null} if the id has no (valid) server id
     */
    @CheckForNull
    public static String serverIdOf(@NonNull String id) {
        int dash = id.length() - UUID_LENGTH - 1;
        if (dash <= 0 || id.charAt(dash) != '-') {
            return null;
        }
        try {
            UUID.fromString(id.substring(dash + 1));
        } catch (IllegalArgumentException e) {
            return null;
        }
        String serverId = id.substring(0, dash);
        return isValidServerId(serverId) ? serverId : null;
    }

    static String newId() {
        String uuid = UUID.randomUUID().toString();
        return idPrefix.isEmpty() ? uuid : idPrefix + "-" + uuid;
    }
}
