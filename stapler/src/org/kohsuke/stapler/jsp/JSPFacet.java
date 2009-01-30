package org.kohsuke.stapler.jsp;

import org.kohsuke.stapler.Facet;
import org.kohsuke.stapler.MetaClass;
import org.kohsuke.stapler.Dispatcher;
import org.kohsuke.stapler.RequestImpl;
import org.kohsuke.stapler.ResponseImpl;
import org.kohsuke.stapler.Stapler;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import java.util.List;
import java.util.logging.Level;
import java.io.IOException;

/**
 * {@link Facet} that adds JSP file support.
 *
 * @author Kohsuke Kawaguchi
 */
public class JSPFacet extends Facet {
    public void buildViewDispatchers(MetaClass owner, List<Dispatcher> dispatchers) {
        dispatchers.add(new Dispatcher() {
            public boolean dispatch(RequestImpl req, ResponseImpl rsp, Object node) throws IOException, ServletException {
                String next = req.tokens.peek();
                if(next==null)  return false;

                Stapler stapler = req.getStapler();

                // check static resources
                RequestDispatcher disp = stapler.getResourceDispatcher(node,next);
                if(disp==null) {
                    // check JSP views
                    disp = stapler.getResourceDispatcher(node,next+".jsp");
                    if(disp==null)  return false;
                }

                req.tokens.next();

                if(traceable())
                    trace(req,rsp,"Invoking "+next+".jsp"+" on "+node+" for "+req.tokens);

                stapler.forward(disp,req,rsp);
                return true;
            }
            public String toString() {
                return "TOKEN.jsp for url=/TOKEN/...";
            }
        });
    }

    public RequestDispatcher createRequestDispatcher(RequestImpl request, Object it, String viewName) throws IOException {
        return request.stapler.getResourceDispatcher(it, viewName);
    }

    public boolean handleIndexRequest(RequestImpl req, ResponseImpl rsp, Object node, MetaClass nodeMetaClass) throws IOException, ServletException {
        Stapler stapler = req.stapler;
        
        // TODO: find the list of welcome pages for this class by reading web.xml
        RequestDispatcher indexJsp = stapler.getResourceDispatcher(node,"index.jsp");
        if(indexJsp!=null) {
            if(LOGGER.isLoggable(Level.FINE))
                LOGGER.fine("Invoking index.jsp on "+node);
            stapler.forward(indexJsp,req,rsp);
            return true;
        }
        return false;
    }
}
