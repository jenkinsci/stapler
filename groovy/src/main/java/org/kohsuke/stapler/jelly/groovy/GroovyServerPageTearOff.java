package org.kohsuke.stapler.jelly.groovy;

import java.io.IOException;
import java.net.URL;
import javax.servlet.RequestDispatcher;
import org.kohsuke.stapler.AbstractTearOff;
import org.kohsuke.stapler.MetaClass;
import org.kohsuke.stapler.jelly.JellyRequestDispatcher;

/**
 * @author Kohsuke Kawaguchi
 */
public class GroovyServerPageTearOff
        extends AbstractTearOff<GroovyClassLoaderTearOff, GroovierJellyScript, IOException> {
    public GroovyServerPageTearOff(MetaClass owner) {
        super(owner, GroovyClassLoaderTearOff.class);
    }

    @Override
    protected String getDefaultScriptExtension() {
        return ".gsp";
    }

    @Override
    public GroovierJellyScript parseScript(URL res) throws IOException {
        try {
            return classLoader.parseGSP(res);
        } catch (ClassNotFoundException e) {
            throw new IOException("Failed to compile " + res, e);
        }
    }

    /**
     * Creates a {@link RequestDispatcher} that forwards to the jelly view, if available.
     *
     * @deprecated removed without replacement
     */
    @Deprecated
    public RequestDispatcher createDispatcher(Object it, String viewName) throws IOException {
        GroovierJellyScript s = findScript(viewName);
        if (s != null) {
            return new JellyRequestDispatcher(it, s);
        }
        return null;
    }
}
