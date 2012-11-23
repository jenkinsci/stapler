package org.kohsuke.stapler;

import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.lang.Klass;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * @author Kohsuke Kawaguchi
 */
// @MetaInfServices - this facet needs to be manually configured
public class StaticViewFacet extends Facet {
    private final List<String> allowedExtensions = new ArrayList<String>();

    public StaticViewFacet(String... allowedExtensions) {
        this(Arrays.asList(allowedExtensions));
    }

    public StaticViewFacet(Collection<String> allowedExtensions) {
        for (String extension : allowedExtensions) {
            addExtension(extension);
        }
    }

    public void addExtension(String ext) {
        if (!ext.startsWith("."))   ext='.'+ext;
        allowedExtensions.add(ext);
    }

    public void buildViewDispatchers(final MetaClass owner, List<Dispatcher> dispatchers) {
        dispatchers.add(new Dispatcher() {
            public boolean dispatch(RequestImpl req, ResponseImpl rsp, Object node) throws IOException, ServletException {
                // check Jelly view
                String next = req.tokens.peek();
                if(next==null)  return false;

                // only match the end of the URL
                if (req.tokens.countRemainingTokens()>1)    return false;
                // and avoid serving both "foo" and "foo/" as relative URL semantics are drastically different
                if (req.getRequestURI().endsWith("/"))      return false;

                URL res = findResource(owner.klass,next);
                if(res==null)        return false;   // no Jelly script found

                req.tokens.next();

                if (traceable()) {
                    // Null not expected here
                    trace(req,rsp,"-> %s on <%s>", next, node);
                }

                rsp.serveFile(req, res);

                return true;
            }

            public String toString() {
                return "static file for url=/VIEW"+StringUtils.join(allowedExtensions,"|");
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
        if (!ends)  return null;

        for ( ; c!=null; c=c.getSuperClass()) {
            URL res = c.getResource(fileName);
            if (res!=null)  return res;
        }
        return null;
    }

    public RequestDispatcher createRequestDispatcher(RequestImpl request, Klass<?> type, Object it, String viewName) throws IOException {
        final Stapler stapler = request.getStapler();
        final URL res = findResource(type,viewName);
        if (res==null)      return null;

        return new RequestDispatcher() {
            public void forward(ServletRequest request, ServletResponse response) throws ServletException, IOException {
                stapler.serveStaticResource((HttpServletRequest)request, new ResponseImpl(stapler, (HttpServletResponse) response), res, 0);
            }

            public void include(ServletRequest request, ServletResponse response) throws ServletException, IOException {
                throw new UnsupportedOperationException();
            }
        };
    }

    public boolean handleIndexRequest(RequestImpl req, ResponseImpl rsp, Object node, MetaClass nodeMetaClass) throws IOException, ServletException {
        URL res = findResource(nodeMetaClass.klass,"index.html");
        if (res==null)  return false;
        rsp.serveFile(req,res);
        return true;
    }
}
