package org.kohsuke.stapler;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import javax.servlet.ServletException;

/**
 * {@link Dispatcher} that deals with the "index" view pages that are used when the request path doesn't contain
 * any token for the current object.
 *
 * <p>
 * It is analogous to Apache serving a directory index if a directory itself is requested, as opposed to a file in it.
 *
 * @author Kohsuke Kawaguchi
 */
class IndexViewDispatcher extends Dispatcher {
    private final MetaClass metaClass;
    private final Facet facet;

    IndexViewDispatcher(MetaClass metaClass, Facet facet) {
        this.metaClass = metaClass;
        this.facet = facet;
    }

    @Override
    public boolean dispatch(RequestImpl req, ResponseImpl rsp, Object node)
            throws IOException, ServletException, IllegalAccessException, InvocationTargetException {
        if (req.tokens.hasMore()) {
            return false;
        }

        // always allow index views to be dispatched
        req.getWebApp().getDispatchValidator().allowDispatch(req, rsp);
        return facet.handleIndexRequest(req, rsp, node, metaClass);
    }

    @Override
    public String toString() {
        return "index view of " + facet + " for url=/";
    }
}
