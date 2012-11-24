package org.kohsuke.stapler.jelly.groovy;

import org.kohsuke.stapler.AbstractTearOff;
import org.kohsuke.stapler.MetaClass;

import javax.servlet.RequestDispatcher;
import java.io.IOException;
import java.net.URL;

/**
 * @author Kohsuke Kawaguchi
 */
public class GroovyServerPageTearOff extends AbstractTearOff<GroovyClassLoaderTearOff,GroovyServerPage,IOException> {
    public GroovyServerPageTearOff(MetaClass owner) {
        super(owner,GroovyClassLoaderTearOff.class);
    }

    @Override
    protected String getDefaultScriptExtension() {
        return ".gsp";
    }

    public GroovyServerPage parseScript(URL res) throws IOException {
        try {
            return new GroovyServerPage(getWebApp(),classLoader.parseGSP(res));
        } catch (ClassNotFoundException e) {
            throw (IOException)new IOException("Failed to compile "+res).initCause(e);
        }
    }

    /**
     * Creates a {@link RequestDispatcher} that forwards to the jelly view, if available.
     */
    public RequestDispatcher createDispatcher(Object it, String viewName) throws IOException {
        GroovyServerPage s = findScript(viewName + ".gsp");
        if (s!=null)    return s.asRequestDispatcher(it);
        return null;
    }
}
