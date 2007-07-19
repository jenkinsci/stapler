package org.kohsuke.stapler.jelly;

import org.apache.commons.jelly.JellyContext;
import org.apache.commons.jelly.JellyException;
import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.Script;
import org.apache.commons.jelly.XMLOutput;
import org.kohsuke.stapler.AbstractTearOff;
import org.kohsuke.stapler.MetaClass;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.BufferedOutputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Kohsuke Kawaguchi
 */
public class JellyClassTearOff extends AbstractTearOff<JellyClassLoaderTearOff,Script,JellyException> {

    public JellyClassTearOff(MetaClass owner) {
        super(owner,JellyClassLoaderTearOff.class);
    }

    protected Script parseScript(URL res) throws JellyException {
        return classLoader.createContext().compileScript(res);
    }

    public static void invokeScript(StaplerRequest req, StaplerResponse rsp, Script script, Object it) throws IOException, JellyTagException {
        // invoke Jelly script to render result
        JellyContext context = new JellyContext();
        Enumeration en = req.getAttributeNames();

        // expose request attributes, just like JSP
        while (en.hasMoreElements()) {
            String name = (String) en.nextElement();
            context.setVariable(name,req.getAttribute(name));
        }
        context.setVariable("request",req);
        context.setVariable("response",rsp);
        context.setVariable("it",it);
        ServletContext servletContext = req.getServletContext();
        context.setVariable("servletContext",servletContext);
        context.setVariable("app",servletContext.getAttribute("app"));
        // property bag to store request scope variables
        context.setVariable("requestScope",context.getVariables());
        // this variable is needed to make "jelly:fmt" taglib work correctly
        context.setVariable("org.apache.commons.jelly.tags.fmt.locale",req.getLocale());

        OutputStream output = rsp.getOutputStream();
        output = new FilterOutputStream(new BufferedOutputStream(output)) {
            public void flush() {
                // flushing ServletOutputStream causes Tomcat to
                // send out headers, making it impossible to set contentType from the script.
                // so don't let Jelly flush.
            }
        };
        XMLOutput xmlOutput = XMLOutput.createXMLOutput(output);
        script.run(context,xmlOutput);
        xmlOutput.flush();
        xmlOutput.close();
        output.close();
    }

    /**
     * Serves <tt>indx.jelly</tt> if it's available, and returns true.
     */
    public boolean serveIndexJelly(StaplerRequest req, StaplerResponse rsp, Object node) throws ServletException, IOException {
        try {
            Script script = findScript("index.jelly");
            if(script!=null) {
                if(LOGGER.isLoggable(Level.FINE))
                    LOGGER.fine("Invoking index.jelly on "+node);
                invokeScript(req,rsp,script,node);
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
        try {
            Script script = findScript(viewName);
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
