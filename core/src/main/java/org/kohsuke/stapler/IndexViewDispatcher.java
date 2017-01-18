package org.kohsuke.stapler;

import javax.servlet.ServletException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;

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

    IndexViewDispatcher(MetaClass metaClass) {
        this.metaClass = metaClass;
    }

    @Override
    public boolean dispatch(RequestImpl req, ResponseImpl rsp, Object node) throws IOException, ServletException, IllegalAccessException, InvocationTargetException {
        if (req.tokens.hasMore())
            return false;

        for (Facet f : metaClass.webApp.facets) {
            if (f.handleIndexRequest(req, rsp, node, metaClass))
                return true;
        }

        URL indexHtml = getSideFileURL(req.stapler, node, "index.html");
        if (indexHtml != null) {
            rsp.serveFile(req, indexHtml, 0);
            return true; // done
        }

        return false;
    }

    private URL getSideFileURL(Stapler stapler, Object node, String fileName) throws MalformedURLException {
        for (Class c = node.getClass(); c != Object.class; c = c.getSuperclass()) {
            String name = "/WEB-INF/side-files/" + c.getName().replace('.', '/') + '/' + fileName;
            URL url = stapler.getResource(name);
            if (url != null) return url;
        }
        return null;
    }

    @Override
    public String toString() {
        return "index views for url=/";
    }
}
