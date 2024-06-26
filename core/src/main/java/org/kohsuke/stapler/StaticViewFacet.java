package org.kohsuke.stapler;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.kohsuke.stapler.lang.Klass;

/**
 * @author Kohsuke Kawaguchi
 */
// @MetaInfServices - this facet needs to be manually configured
public class StaticViewFacet extends Facet {
    private final List<String> allowedExtensions = new ArrayList<>();

    public StaticViewFacet(String... allowedExtensions) {
        this(Arrays.asList(allowedExtensions));
    }

    public StaticViewFacet(Collection<String> allowedExtensions) {
        for (String extension : allowedExtensions) {
            addExtension(extension);
        }
    }

    public void addExtension(String ext) {
        if (!ext.startsWith(".")) {
            ext = '.' + ext;
        }
        allowedExtensions.add(ext);
    }

    @Override
    public void buildViewDispatchers(final MetaClass owner, List<Dispatcher> dispatchers) {
        dispatchers.add(new Dispatcher() {
            @Override
            public boolean dispatch(RequestImpl req, ResponseImpl rsp, Object node)
                    throws IOException, ServletException {
                // check Jelly view
                String next = req.tokens.peek();
                if (next == null) {
                    return false;
                }

                // only match the end of the URL
                if (req.tokens.countRemainingTokens() > 1) {
                    return false;
                }
                // and avoid serving both "foo" and "foo/" as relative URL semantics are drastically different
                if (req.getRequestURI().endsWith("/")) {
                    return false;
                }

                URL res = findResource(owner.klass, next);
                if (res == null) {
                    return false; // no Jelly script found
                }

                req.tokens.next();

                Dispatcher.anonymizedTraceEval(req, rsp, node, "%s: Static view: %s", next);
                if (traceable()) {
                    // Null not expected here
                    trace(req, rsp, "-> %s on <%s>", next, node);
                }

                rsp.serveFile(req, res);

                return true;
            }

            @Override
            public String toString() {
                return "static file for url=/VIEW" + String.join("|", allowedExtensions);
            }
        });
    }

    /**
     * Determines if this resource can be served
     */
    protected URL findResource(Klass c, String fileName) {
        boolean ends = false;
        for (String ext : allowedExtensions) {
            if (fileName.endsWith(ext)) {
                ends = true;
                break;
            }
        }
        if (!ends) {
            return null;
        }

        for (; c != null; c = c.getSuperClass()) {
            URL res = c.getResource(fileName);
            if (res != null) {
                return res;
            }
        }
        return null;
    }

    @Override
    public RequestDispatcher createRequestDispatcher(RequestImpl request, Klass<?> type, Object it, String viewName)
            throws IOException {
        final Stapler stapler = request.getStapler();
        final URL res = findResource(type, viewName);
        if (res == null) {
            return null;
        }

        return new RequestDispatcher() {
            @Override
            public void forward(ServletRequest request, ServletResponse response) throws ServletException, IOException {
                stapler.serveStaticResource(
                        (HttpServletRequest) request,
                        new ResponseImpl(stapler, (HttpServletResponse) response),
                        res,
                        0);
            }

            @Override
            public void include(ServletRequest request, ServletResponse response) throws ServletException, IOException {
                throw new UnsupportedOperationException();
            }
        };
    }

    @Override
    public void buildIndexDispatchers(MetaClass owner, List<Dispatcher> dispatchers) {
        URL res = findResource(owner.klass, "index.html");
        if (res != null) {
            dispatchers.add(new IndexHtmlDispatcher(res));
        }
    }

    @Override
    public boolean handleIndexRequest(RequestImpl req, ResponseImpl rsp, Object node, MetaClass nodeMetaClass)
            throws IOException, ServletException {
        URL res = findResource(nodeMetaClass.klass, "index.html");
        if (res == null) {
            return false;
        }
        return new IndexHtmlDispatcher(res).dispatch(req, rsp, node);
    }
}
