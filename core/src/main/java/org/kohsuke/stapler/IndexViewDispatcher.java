package org.kohsuke.stapler;

import org.kohsuke.stapler.lang.Klass;

import javax.servlet.ServletException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

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

    private final List<String> allowedExtensions = new ArrayList<>();
    private final Facet facet;

    IndexViewDispatcher(MetaClass metaClass, Facet facet) {
        this.metaClass = metaClass;
        this.facet = facet;
    }

    @Override
    public boolean dispatch(RequestImpl req, ResponseImpl rsp, Object node) throws IOException, ServletException, IllegalAccessException, InvocationTargetException {
        if (req.tokens.hasMore())
            return false;

        // always allow index views to be dispatched
        req.getWebApp().getDispatchValidator().allowDispatch(req, rsp);
        return handleIndexRequest(req, rsp, node, metaClass);
    }

    public boolean handleIndexRequest(RequestImpl req, ResponseImpl rsp, Object node, MetaClass nodeMetaClass) throws IOException, ServletException {
        URL res = findResource(nodeMetaClass.klass,"index.html");
        if (res==null)  return false;
        return new IndexHtmlDispatcher(res).dispatch(req,rsp,node);
    }

    protected URL findResource(Klass c, String fileName) {
        boolean ends = false;
        for (String ext : allowedExtensions) {
            if (fileName.endsWith(ext)) {
                ends = true;
                break;
            }
        }
        if (!ends)  return null;

        for ( ; c!=null; c=c.getSuperClass()) {
            URL res = c.getResource(fileName);
            if (res!=null)  return res;
        }
        return null;
    }

    @Override
    public String toString() {
        return "index view of "+facet+" for url=/";
    }
}
