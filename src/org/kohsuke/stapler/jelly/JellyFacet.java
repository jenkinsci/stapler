package org.kohsuke.stapler.jelly;

import org.kohsuke.stapler.Facet;
import org.kohsuke.stapler.Dispatcher;
import org.kohsuke.stapler.RequestImpl;
import org.kohsuke.stapler.ResponseImpl;
import org.kohsuke.stapler.MetaClass;
import org.kohsuke.stapler.TearOffSupport;
import org.apache.commons.jelly.Script;

import javax.servlet.ServletException;
import javax.servlet.RequestDispatcher;
import java.util.List;
import java.util.logging.Level;
import java.io.IOException;

/**
 * {@link Facet} that adds Jelly as the view.
 * 
 * @author Kohsuke Kawaguchi
 */
public class JellyFacet extends Facet {
    public void buildViewDispatchers(final MetaClass owner, List<Dispatcher> dispatchers) {
        dispatchers.add(new Dispatcher() {
            final JellyClassTearOff tearOff = owner.loadTearOff(JellyClassTearOff.class);

            public boolean dispatch(RequestImpl req, ResponseImpl rsp, Object node) throws IOException, ServletException {
                // check Jelly view
                String next = req.tokens.peek();
                if(next==null)  return false;

                try {
                    Script script = tearOff.findScript(next+".jelly");

                    if(script==null)        return false;   // no Jelly script found

                    req.tokens.next();

                    if(LOGGER.isLoggable(Level.FINE))
                        LOGGER.fine("Invoking "+next+".jelly"+" on "+node+" for "+req.tokens);

                    JellyClassTearOff.invokeScript(req, rsp, script, node);

                    return true;
                } catch (RuntimeException e) {
                    throw e;
                } catch (IOException e) {
                    throw e;
                } catch (Exception e) {
                    throw new ServletException(e);
                }
            }
        });
    }

    public RequestDispatcher createRequestDispatcher(RequestImpl request, Object it, String viewName) throws IOException {
        TearOffSupport mc = request.stapler.getWebApp().getMetaClass(it.getClass());
        return mc.loadTearOff(JellyClassTearOff.class).createDispatcher(it,viewName);
    }

    public boolean handleIndexRequest(RequestImpl req, ResponseImpl rsp, Object node, MetaClass nodeMetaClass) throws IOException, ServletException {
        return nodeMetaClass.loadTearOff(JellyClassTearOff.class).serveIndexJelly(req,rsp,node);
    }
}
