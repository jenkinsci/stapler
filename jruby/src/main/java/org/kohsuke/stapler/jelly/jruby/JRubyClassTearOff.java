package org.kohsuke.stapler.jelly.jruby;

import org.apache.commons.jelly.JellyException;
import org.apache.commons.jelly.Script;
import org.kohsuke.stapler.AbstractTearOff;
import org.kohsuke.stapler.MetaClass;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.WebApp;
import org.kohsuke.stapler.jelly.JellyFacet;
import org.kohsuke.stapler.jelly.JellyRequestDispatcher;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Kohsuke Kawaguchi
 */
public class JRubyClassTearOff extends AbstractTearOff<JRubyClassLoaderTearOff,Script,IOException> {
    public JRubyClassTearOff(MetaClass owner) {
        super(owner,JRubyClassLoaderTearOff.class);
    }

    public Script parseScript(URL res) throws IOException {
        return classLoader.parse(res);
    }

    public boolean serveIndexErb(StaplerRequest req, StaplerResponse rsp, Object node) throws IOException, ServletException {
        try {
            Script script = findScript("index.erb");
            if(script!=null) {
                if(LOGGER.isLoggable(Level.FINE))
                    LOGGER.fine("Invoking index.erb on "+node);
                WebApp.getCurrent().getFacet(JellyFacet.class).scriptInvoker.invokeScript(req, rsp, script, node);
                return true;
            }
            return false;
        } catch (JellyException e) {
            throw new ServletException(e);
        }
    }

    /**
     * Creates a {@link RequestDispatcher} that forwards to the jelly view, if available.
     */
    public RequestDispatcher createDispatcher(Object it, String viewName) throws IOException {
        Script script = findScript(viewName+".erb");
        if(script!=null)
            return new JellyRequestDispatcher(it,script);
        return null;
    }

    private static final Logger LOGGER = Logger.getLogger(JRubyClassTearOff.class.getName());
}
