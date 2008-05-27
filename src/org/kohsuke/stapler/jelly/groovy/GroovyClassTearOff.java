package org.kohsuke.stapler.jelly.groovy;

import org.apache.commons.jelly.JellyException;
import org.apache.commons.jelly.Script;
import org.kohsuke.stapler.AbstractTearOff;
import org.kohsuke.stapler.MetaClass;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.jelly.JellyClassTearOff;

import javax.servlet.ServletException;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Kohsuke Kawaguchi
 */
public class GroovyClassTearOff extends AbstractTearOff<GroovyClassLoaderTearOff,Script,IOException> {
    public GroovyClassTearOff(MetaClass owner) {
        super(owner,GroovyClassLoaderTearOff.class);
    }

    protected Script parseScript(URL res) throws IOException {
        Script script;
        script = classLoader.parse(res);
        return script;
    }

    public boolean serveIndexGroovy(StaplerRequest req, StaplerResponse rsp, Object node) throws IOException, ServletException {
        try {
            Script script = findScript("index.groovy");
            if(script!=null) {
                if(LOGGER.isLoggable(Level.FINE))
                    LOGGER.fine("Invoking index.jelly on "+node);
                JellyClassTearOff.invokeScript(req,rsp,script,node);
                return true;
            }
            return false;
        } catch (JellyException e) {
            throw new ServletException(e);
        }
    }

    private static final Logger LOGGER = Logger.getLogger(GroovyClassTearOff.class.getName());
}
