package org.kohsuke.stapler.jelly.groovy;

import org.apache.commons.jelly.JellyException;
import org.apache.commons.jelly.Script;
import org.kohsuke.stapler.AbstractTearOff;
import org.kohsuke.stapler.MetaClass;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.WebApp;
import org.kohsuke.stapler.ServletException2;
import org.kohsuke.stapler.jelly.JellyRequestDispatcher;
import org.kohsuke.stapler.jelly.JellyFacet;

import javax.servlet.ServletException;
import javax.servlet.RequestDispatcher;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Kohsuke Kawaguchi
 */
public final class GroovyClassTearOff extends AbstractTearOff<GroovyClassLoaderTearOff,GroovierJellyScript,IOException> {
    public GroovyClassTearOff(MetaClass owner) {
        super(owner,GroovyClassLoaderTearOff.class);
    }

    public GroovierJellyScript parseScript(URL res) throws IOException {
        return classLoader.parse(res);
    }

    // TODO: code duplication between JellyClassTearOff

    public boolean serveIndexGroovy(StaplerRequest req, StaplerResponse rsp, Object node) throws IOException, ServletException {
        try {
            Script script = findScript("index.groovy");
            if(script!=null) {
                if(LOGGER.isLoggable(Level.FINE))
                    LOGGER.fine("Invoking index.jelly on "+node);
                WebApp.getCurrent().getFacet(JellyFacet.class).scriptInvoker.invokeScript(req, rsp, script, node);
                return true;
            }
            return false;
        } catch (JellyException e) {
            throw new ServletException2(e);
        }
    }

    /**
     * Creates a {@link RequestDispatcher} that forwards to the jelly view, if available.
     */
    public RequestDispatcher createDispatcher(Object it, String viewName) throws IOException {
        Script script = findScript(viewName+".groovy");
        if(script!=null)
            return new JellyRequestDispatcher(it,script);
        return null;
    }

    private static final Logger LOGGER = Logger.getLogger(GroovyClassTearOff.class.getName());
}
