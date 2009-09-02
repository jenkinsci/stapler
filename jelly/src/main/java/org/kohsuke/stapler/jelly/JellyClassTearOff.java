package org.kohsuke.stapler.jelly;

import org.apache.commons.jelly.JellyException;
import org.apache.commons.jelly.Script;
import org.kohsuke.stapler.AbstractTearOff;
import static org.kohsuke.stapler.Dispatcher.trace;
import static org.kohsuke.stapler.Dispatcher.traceable;
import org.kohsuke.stapler.MetaClass;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.WebApp;
import org.kohsuke.stapler.ServletException2;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Logger;

/**
 * @author Kohsuke Kawaguchi
 */
public class JellyClassTearOff extends AbstractTearOff<JellyClassLoaderTearOff,Script,JellyException> {
    private JellyFacet facet;

    public JellyClassTearOff(MetaClass owner) {
        super(owner,JellyClassLoaderTearOff.class);
        facet = WebApp.getCurrent().getFacet(JellyFacet.class);
    }

    protected Script parseScript(URL res) throws JellyException {
        return classLoader.createContext().compileScript(res);
    }

    /**
     * Serves <tt>indx.jelly</tt> if it's available, and returns true.
     */
    public boolean serveIndexJelly(StaplerRequest req, StaplerResponse rsp, Object node) throws ServletException, IOException {
        try {
            Script script = findScript("index.jelly");
            if(script!=null) {
                if(traceable())
                    trace(req,rsp,"-> index.jelly on <%s>",node);
                facet.scriptInvoker.invokeScript(req, rsp, script, node);
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
        try {
            // backward compatible behavior that expects full file name including ".jelly"
            Script script = findScript(viewName);
            if(script!=null)
                return new JellyRequestDispatcher(it,script);
            
            // this is what the look up was really supposed to be.
            script = findScript(viewName+".jelly");
            if(script!=null)
                return new JellyRequestDispatcher(it,script);
            return null;
        } catch (JellyException e) {
            IOException io = new IOException(e.getMessage());
            io.initCause(e);
            throw io;
        }
    }

    private static final Logger LOGGER = Logger.getLogger(JellyClassTearOff.class.getName());
}
