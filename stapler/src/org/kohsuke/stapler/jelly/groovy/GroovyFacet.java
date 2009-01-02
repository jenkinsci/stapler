package org.kohsuke.stapler.jelly.groovy;

import org.apache.commons.jelly.Script;
import org.kohsuke.stapler.Dispatcher;
import org.kohsuke.stapler.Facet;
import org.kohsuke.stapler.MetaClass;
import org.kohsuke.stapler.RequestImpl;
import org.kohsuke.stapler.ResponseImpl;
import org.kohsuke.stapler.TearOffSupport;
import org.kohsuke.stapler.WebApp;
import org.kohsuke.stapler.jelly.JellyFacet;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.List;

/**
 * {@link Facet} that brings in Groovy support on top of Jelly.
 * 
 * @author Kohsuke Kawaguchi
 */
public class GroovyFacet extends Facet {
    public void buildViewDispatchers(final MetaClass owner, List<Dispatcher> dispatchers) {
        dispatchers.add(new Dispatcher() {
            final GroovyClassTearOff tearOff = owner.loadTearOff(GroovyClassTearOff.class);

            public boolean dispatch(RequestImpl req, ResponseImpl rsp, Object node) throws IOException, ServletException {
                // check Groovy view
                String next = req.tokens.peek();
                if(next==null)  return false;

                try {
                    Script script = tearOff.findScript(next+".groovy");
                    if(script==null)        return false;   // no Groovy script found

                    req.tokens.next();

                    if(traceable())
                        trace(req,rsp,"Invoking "+next+".groovy"+" on "+node+" for "+req.tokens);

                    WebApp.getCurrent().getFacet(JellyFacet.class).scriptInvoker.invokeScript(req, rsp, script, node);

                    return true;
                } catch (RuntimeException e) {
                    throw e;
                } catch (IOException e) {
                    throw e;
                } catch (Exception e) {
                    throw new ServletException(e);
                }
            }
            public String toString() {
                return "TOKEN.groovy for url=/TOKEN/...";
            }
        });
    }

    public RequestDispatcher createRequestDispatcher(RequestImpl request, Object it, String viewName) throws IOException {
        TearOffSupport mc = request.stapler.getWebApp().getMetaClass(it.getClass());
        return mc.loadTearOff(GroovyClassTearOff.class).createDispatcher(it,viewName);
    }

    public boolean handleIndexRequest(RequestImpl req, ResponseImpl rsp, Object node, MetaClass nodeMetaClass) throws IOException, ServletException {
        return nodeMetaClass.loadTearOff(GroovyClassTearOff.class).serveIndexGroovy(req, rsp, node);
    }
}
