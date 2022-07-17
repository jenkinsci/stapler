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
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.PrintWriter;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.Ancestor;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.HttpResponses;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerFallback;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import javax.servlet.ServletException;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Objects exported and bound by JavaScript proxies.
 *
 * TODO: think about some kind of eviction strategy, beyond the session eviction.
 * Maybe it's not necessary, I don't know.
 *
 * @author Kohsuke Kawaguchi
 */
public class BoundObjectTable implements StaplerFallback {

    /**
     * Represents a script that can be rendered
     */
    private final class RenderableScript {
        private final String variableName;

        public RenderableScript(String variableName) {
            this.variableName = variableName;
        }

        public void doDynamic(StaplerRequest req, StaplerResponse rsp) throws IOException {
            final String id = StringUtils.removeStart(req.getRestOfPath(), "/");
            rsp.setContentType("application/javascript");
            final PrintWriter writer = rsp.getWriter();
            final Table table = resolve(false);
            if (table == null) {
                rsp.sendError(404);
                return;
            }
            Object object = table.resolve(id);
            if (object == null) {
                /* Support null bound objects */
                writer.append(variableName).append(" = null;");
                return;
            }
            final String script = Bound.getProxyScript(Stapler.getCurrentRequest().getContextPath() + PREFIX + id, object.getClass());
            writer.append(variableName).append(" = ").append(script).append(";");
        }
    }

    private final class ScriptRenderer {
        private boolean isValidIdentifier(String variableName) {
            // Ultimately this will be used as a JS identifier, so we need (a subset of) what's valid there.
            // The primary purpose of this check however is to prevent injection attacks.
            return variableName.matches("^[a-zA-Z_][a-zA-Z0-9_]*$");
        }
        public RenderableScript getDynamic(String variableName) {
            if (isValidIdentifier(variableName)) {
                return new RenderableScript(variableName);
            } else {
                return null;
            }
        }
    }
    public ScriptRenderer getScript() {
        return new ScriptRenderer();
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
        Ancestor eot = Stapler.getCurrentRequest().findAncestor(BoundObjectTable.class);
        if (eot==null)
            throw new IllegalStateException("The thread is not handling a request to a abound object");
        String id = eot.getNextToken(0);

        resolve(false).release(id); // resolve(false) can't fail because we are processing this request now.
    }

    /**
     * Obtains a {@link Table} associated with this session.
     */
    private Table resolve(boolean createIfNotExist) {
        HttpSession session = Stapler.getCurrentRequest().getSession(createIfNotExist);
        if (session==null) return null;

        Table t = (Table) session.getAttribute(Table.class.getName());
        if (t==null) {
            if (createIfNotExist)
                session.setAttribute(Table.class.getName(), t=new Table());
            else
                return null;
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
    public static class Table {
        private final Map<String,Ref> entries = new HashMap<>();
        private boolean logging;

        private synchronized Bound add(Ref ref) {
            final Object target = ref.get();
            if (target instanceof WithWellKnownURL) {
                WithWellKnownURL w = (WithWellKnownURL) target;
                String url = w.getWellKnownUrl();
                if (!url.startsWith("/")) {
                    LOGGER.warning("WithWellKnownURL.getWellKnownUrl must start with a slash. But we got " + url + " from "+w);
                }
                return new WellKnownObjectHandle(url, w);
            }

            final String id = UUID.randomUUID().toString();
            entries.put(id,ref);
            if (logging)    LOGGER.info(String.format("%s binding %s for %s", toString(), target, id));

            return new Bound() {
                @Override
                public void release() {
                   Table.this.release(id);
                }

                @Override
                public String getURL() {
                    return Stapler.getCurrentRequest().getContextPath()+PREFIX+id;
                }

                @Override
                public Object getTarget() {
                    return target;
                }

                @Override
                public void generateResponse(StaplerRequest req, StaplerResponse rsp, Object node) throws IOException, ServletException {
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
            if (e==null) {
                if (logging)    LOGGER.info(toString()+" doesn't have binding for "+id);
                return null;
            }
            Object v = e.get();
            if (v==null) {
                if (logging)    LOGGER.warning(toString() + " had binding for " + id + " but it got garbage collected");
                entries.remove(id); // reference is already garbage collected.
            }
            return v;
        }

        @SuppressFBWarnings(value = "IS2_INCONSISTENT_SYNC", justification = "This usage does not create synchronization problems.")
        public HttpResponse doEnableLogging() {
            if (DEBUG_LOGGING) {
                this.logging = true;
                return HttpResponses.text("Logging enabled for this session: " + this + "\n");
            } else {
                return HttpResponses.forbidden();
            }
        }
    }

    /**
     * Supports
     */
    public static final class BindScript {

        private final Bound bound;
        private final String variableName;

        public BindScript(@CheckForNull Bound bound, @NonNull String variableName) {
            this.bound = bound;
            this.variableName = variableName;
        }

        public void doIndex(StaplerResponse rsp) throws IOException, ServletException {
            rsp.setContentType("application/javascript");
            final PrintWriter writer = rsp.getWriter();
            final String script = bound == null ? "null" : bound.getProxyScript();
            writer.append(variableName).append(" = ").append(script).append(";");
        }
    }

    private static final class WellKnownObjectHandle extends Bound {
        private final String url;
        private final Object target;

        public WellKnownObjectHandle(String url, Object target) {
            this.url = url;
            this.target = target;
        }

        /**
         * Objects with well-known URLs cannot be released, as their URL bindings are controlled
         * implicitly by the application.
         */
        @Override
        public void release() {
        }

        @Override
        public String getURL() {
            return Stapler.getCurrentRequest().getContextPath()+url;
        }

        @Override
        public Object getTarget() {
            return target;
        }

        @Override
        public void generateResponse(StaplerRequest req, StaplerResponse rsp, Object node) throws IOException, ServletException {
            rsp.sendRedirect2(getURL());
        }
    }


    /**
     * Reference that resolves to an object.
     */
    interface Ref {
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
    }
    
    private static class WeakRef extends WeakReference implements Ref {
        private WeakRef(Object referent) {
            super(referent);
        }
    }

    public static final String PREFIX = "/$stapler/bound/";
    public static final String SCRIPT_PREFIX = "/$stapler/bound/script/";

    /**
     * True to activate debug logging of session fragments.
     */
    @SuppressFBWarnings(value = "MS_SHOULD_BE_FINAL", justification = "Legacy switch.")
    public static boolean DEBUG_LOGGING = Boolean.getBoolean(BoundObjectTable.class.getName()+".debugLog");

    private static final Logger LOGGER = Logger.getLogger(BoundObjectTable.class.getName());
}
