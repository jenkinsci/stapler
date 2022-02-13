package org.kohsuke.stapler;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Serve {@code index.html} from {@code WEB-INF/side-files} if that exists.
 *
 * @author Kohsuke Kawaguchi
 */
class IndexHtmlDispatcher extends Dispatcher {
    private final URL html;

    IndexHtmlDispatcher(URL html) {
        this.html = html;
    }

    @Override
    public boolean dispatch(RequestImpl req, ResponseImpl rsp, Object node) throws IOException, ServletException {
        if (!req.tokens.hasMore()) {
            rsp.serveFile(req, html, 0);
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return "index.html for url=/";
    }

    /**
     * Returns a {@link IndexHtmlDispatcher} if and only if the said class has {@code index.html} as a side-file.
     * @param c Class from where to start the index.html inspection. Will go through class hierarchy (unless interface)
     * @return Index dispatcher or {@code null} if it cannot be found
     */
    static Dispatcher make(ServletContext context, Class c) {
        for (; c != null && c != Object.class; c = c.getSuperclass()) {
            String name = "/WEB-INF/side-files/" + c.getName().replace('.', '/') + "/index.html";
            try {
                URL url = context.getResource(name);
                if (url != null)
                    return new IndexHtmlDispatcher(url);
            } catch (MalformedURLException e) {
                throw new IllegalArgumentException(e);
            }
        }
        return null;
    }
}
