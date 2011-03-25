package org.kohsuke.stapler.jelly.jruby;

import org.apache.commons.jelly.Script;
import org.kohsuke.MetaInfServices;
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
 * @author Kohsuke Kawaguchi
 */
@MetaInfServices
public class JRubyFacet extends Facet {
    public void buildViewDispatchers(final MetaClass owner, List<Dispatcher> dispatchers) {
        dispatchers.add(new Dispatcher() {
            final JRubyClassTearOff tearOff = owner.loadTearOff(JRubyClassTearOff.class);

            public boolean dispatch(RequestImpl req, ResponseImpl rsp, Object node) throws IOException, ServletException {
                // check Groovy view
                String next = req.tokens.peek();
                if(next==null)  return false;

                try {
                    Script script = tearOff.findScript(next+".erb");
                    if(script==null)        return false;   // no ERB script found

                    req.tokens.next();

                    if(traceable())
                        trace(req,rsp,"Invoking "+next+".erb"+" on "+node+" for "+req.tokens);

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
                return "TOKEN.erb for url=/TOKEN/...";
            }
        });
    }

    public RequestDispatcher createRequestDispatcher(RequestImpl request, Class type, Object it, String viewName) throws IOException {
        TearOffSupport mc = request.stapler.getWebApp().getMetaClass(type);
        return mc.loadTearOff(JRubyClassTearOff.class).createDispatcher(it,viewName);
    }

    public boolean handleIndexRequest(RequestImpl req, ResponseImpl rsp, Object node, MetaClass nodeMetaClass) throws IOException, ServletException {
        return nodeMetaClass.loadTearOff(JRubyClassTearOff.class).serveIndexErb(req, rsp, node);
    }
}

