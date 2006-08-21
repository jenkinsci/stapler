package org.kohsuke.stapler.jelly;

import org.apache.commons.jelly.JellyContext;
import org.apache.commons.jelly.JellyException;
import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.Script;
import org.apache.commons.jelly.XMLOutput;
import org.kohsuke.stapler.MetaClass;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.RequestDispatcher;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Kohsuke Kawaguchi
 */
public class JellyClassTearOff {

    private final MetaClass owner;
    private final JellyClassLoaderTearOff classLoader;

    public JellyClassTearOff(MetaClass owner) {
        this.owner = owner;
        classLoader = owner.classLoader.loadTearOff(JellyClassLoaderTearOff.class);
    }

    /**
     * Compiled jelly script views of this class.
     * Access needs to be synchronized.
     */
    private final Map<String, Script> scripts = new HashMap<String,Script>();

    /**
     * Locates the Jelly script view of the given name.
     *
     * @param name
     *      if this is a relative path, such as "foo.jelly" or "foo/bar.jel",
     *      then it is assumed to be relative to this class, so
     *      "org/acme/MyClass/foo.jelly" or "org/acme/MyClass/foo/bar.jel"
     *      will be searched.
     *      <p>
     *      If this starts with "/", then it is assumed to be absolute,
     *      and that name is searched from the classloader. This is useful
     *      to do mix-in.
     */
    public Script findScript(String name) throws JellyException {
        Script script;

        synchronized(scripts) {
            script = scripts.get(name);
            if(script==null || MetaClass.NO_CACHE) {
                ClassLoader cl = owner.clazz.getClassLoader();
                if(cl!=null) {

                    URL res;

                    if(name.startsWith("/")) {
                        // try name as full path to the Jelly script
                        res = cl.getResource(name.substring(1));
                    } else {
                        // assume that it's a view of this class
                        res = cl.getResource(owner.clazz.getName().replace('.','/').replace('$','/')+'/'+name);
                    }

                    if(res!=null) {
                        script = classLoader.createContext().compileScript(res);
                        scripts.put(name,script);
                    }
                }
            }
        }
        if(script!=null)
            return script;

        // not found on this class, delegate to the parent
        if(owner.baseClass!=null)
            return owner.baseClass.loadTearOff(JellyClassTearOff.class).findScript(name);

        return null;
    }

    public void invokeScript(StaplerRequest req, StaplerResponse rsp, Script script, Object it) throws IOException, JellyTagException {
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
        output = new FilterOutputStream(output) {
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
}
