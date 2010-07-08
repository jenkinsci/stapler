package org.kohsuke.stapler.framework;

import org.kohsuke.stapler.Ancestor;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import javax.servlet.ServletException;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 *
 *
 * TODO: think about some kind of eviction strategy, beyond the session eviction.
 * Maybe it's not necessary, I don't know.
 *
 * @author Kohsuke Kawaguchi
 */
public class ExportedObjectTable {
    public Object getDynamic(String id) {
        Table t = resolve(false);
        if (t == null) return null;
        return t.resolve(id);
    }

    private ExportHandle export(Ref ref) {
        return resolve(true).add(ref);
    }

    /**
     * Exports an object temporarily and returns its URL.
     */
    public ExportHandle export(Object o) {
        return export(strongRef(o));
    }

    /**
     * Exports an object temporarily and returns its URL.
     */
    public ExportHandle exportWeak(Object o) {
        return export(new WeakRef(o));
    }

    /**
     * Called from within the request handling of an exported object, to release the object explicitly.
     */
    public void releaseMe() {
        Ancestor eot = Stapler.getCurrentRequest().findAncestor(ExportedObjectTable.class);
        if (eot==null)
            throw new IllegalStateException("The thread is not handling a request to an exported object");
        String id = eot.getNextToken(0);

        resolve(false).release(id); // resolve(false) can't fail because we are processing this request now.
    }

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
     * Per-session table that remembers all the exported instances.
     */
    private static class Table {
        private final Map<String,Ref> entries = new HashMap<String,Ref>();

        private synchronized ExportHandle add(Ref ref) {
            Object target = ref.get();
            if (target instanceof WithWellKnownURL) {
                WithWellKnownURL w = (WithWellKnownURL) target;
                return new WellKnownExportHandle(w.getWellKnownUrl());
            }

            final String id = UUID.randomUUID().toString();
            entries.put(id,ref);

            return new ExportHandle() {
                public void release() {
                   Table.this.release(id);
                }

                public String getURL() {
                    return Stapler.getCurrentRequest().getContextPath()+PREFIX+id;
                }

                public void generateResponse(StaplerRequest req, StaplerResponse rsp, Object node) throws IOException, ServletException {
                    rsp.sendRedirect2(getURL());
                }
            };
        }

        private synchronized Ref release(String id) {
            return entries.remove(id);
        }

        private synchronized Object resolve(String id) {
            Ref e = entries.get(id);
            if (e==null)    return null;
            Object v = e.get();
            if (v==null)
                entries.remove(id); // reference is already garbage collected.
            return v;
        }
    }

    private static final class WellKnownExportHandle implements ExportHandle {
        private final String url;

        public WellKnownExportHandle(String url) {
            this.url = url;
        }

        /**
         * Objects with well-known URLs cannot be released, as their URL bindings are controlled
         * implicitly by the application.
         */
        public void release() {
        }

        public String getURL() {
            return Stapler.getCurrentRequest().getContextPath()+url;
        }

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

    private static Ref strongRef(final Object o) {
        return new Ref() {
            public Object get() {
                return o;
            }
        };
    }
    
    private static class WeakRef extends WeakReference implements Ref {
        private WeakRef(Object referent) {
            super(referent);
        }
    }

    public static final String PREFIX = "/$stapler/exported/";
}
