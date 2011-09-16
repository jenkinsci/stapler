package org.kohsuke.stapler.jelly.jruby;

import org.apache.commons.jelly.Script;
import org.kohsuke.stapler.Dispatcher;
import org.kohsuke.stapler.RequestImpl;
import org.kohsuke.stapler.ResponseImpl;
import org.kohsuke.stapler.WebApp;
import org.kohsuke.stapler.jelly.JellyFacet;

import javax.servlet.ServletException;
import java.io.IOException;

/**
 * {@link Dispatcher} that invokes view script.
 *
 * @author Kohsuke Kawaguchi
 */
abstract class ScriptInvokingDispatcher extends Dispatcher {
    protected boolean invokeScript(RequestImpl req, ResponseImpl rsp, Object node, String next, Script script) throws IOException, ServletException {
        try {
            if(script==null) return false;

            req.tokens.next();

            if(traceable())
                trace(req,rsp,"Invoking "+next+" on "+node+" for "+req.tokens);

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
        return "TOKEN for url=/TOKEN/...";
    }
}
