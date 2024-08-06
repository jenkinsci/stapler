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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.Ancestor;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.HttpResponses;
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
        if (name == null || StringUtils.isBlank(name)) {
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

    private Bound bind(Ref ref) {
        return resolve(true).add(ref);
    }

    /**
     * Binds an object temporarily and returns its URL.
     */
    public Bound bind(Object o) {
        return bind(new StrongRef(o));
    }

    /**
     * Binds an object temporarily and returns its URL.
     */
    public Bound bindWeak(Object o) {
        return bind(new WeakRef(o));
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
        private boolean logging;

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

            final String id = UUID.randomUUID().toString();
            entries.put(id, ref);
            if (logging) {
                LOGGER.info(String.format("%s binding %s for %s", toString(), target, id));
            }

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

        private synchronized Ref release(String id) {
            return entries.remove(id);
        }

        private synchronized Object resolve(String id) {
            Ref e = entries.get(id);
            if (e == null) {
                if (logging) {
                    LOGGER.info(toString() + " doesn't have binding for " + id);
                }
                return null;
            }
            Object v = e.get();
            if (v == null) {
                if (logging) {
                    LOGGER.warning(toString() + " had binding for " + id + " but it got garbage collected");
                }
                entries.remove(id); // reference is already garbage collected.
            }
            return v;
        }

        @SuppressFBWarnings(
                value = "IS2_INCONSISTENT_SYNC",
                justification = "This usage does not create synchronization problems.")
        public HttpResponse doEnableLogging() {
            if (DEBUG_LOGGING) {
                this.logging = true;
                return HttpResponses.text("Logging enabled for this session: " + this + "\n");
            } else {
                return HttpResponses.forbidden();
            }
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
    }

    private static class StrongRef implements Ref {
        private final Object o;

        StrongRef(Object o) {
            this.o = o;
        }

        @Override
        public Object get() {
            return o;
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

    private static class WeakRef extends WeakReference implements Ref {
        private WeakRef(Object referent) {
            super(referent);
        }

        private Object writeReplace() {
            Object o = get();
            if (o instanceof Serializable) {
                return this;
            } else {
                LOGGER.fine(() -> "Refusing to serialize " + o);
                return new WeakRef(null);
            }
        }
    }

    public static final String PREFIX = "/$stapler/bound/";
    static final String SCRIPT_PREFIX = "/$stapler/bound/script";

    /**
     * True to activate debug logging of session fragments.
     */
    @SuppressFBWarnings(value = "MS_SHOULD_BE_FINAL", justification = "Legacy switch.")
    public static boolean DEBUG_LOGGING = Boolean.getBoolean(BoundObjectTable.class.getName() + ".debugLog");

    private static final Logger LOGGER = Logger.getLogger(BoundObjectTable.class.getName());
}
