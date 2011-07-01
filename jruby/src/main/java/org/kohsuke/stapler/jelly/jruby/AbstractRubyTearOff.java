package org.kohsuke.stapler.jelly.jruby;

import org.apache.commons.jelly.Script;
import org.kohsuke.stapler.AbstractTearOff;
import org.kohsuke.stapler.MetaClass;
import org.kohsuke.stapler.WebApp;
import org.kohsuke.stapler.jelly.JellyRequestDispatcher;

import javax.servlet.RequestDispatcher;
import java.io.IOException;
import java.net.URL;

/**
 *
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class AbstractRubyTearOff extends AbstractTearOff<JRubyClassLoaderTearOff,Script,IOException> {
    protected AbstractRubyTearOff(MetaClass owner) {
        super(owner, JRubyClassLoaderTearOff.class);
    }

    /**
     * Defines the file extension, like ".erb", that designates this kind of view type.
     */
    @Override
    protected abstract String getDefaultScriptExtension();

    public Script parseScript(URL res) throws IOException {
        return WebApp.getCurrent().getFacet(JRubyFacet.class).parseScript(res);
    }

    /**
     * Creates a {@link RequestDispatcher} that forwards to the jelly view, if available.
     */
    public RequestDispatcher createDispatcher(Object it, String viewName) throws IOException {
        Script script = findScript(viewName+getDefaultScriptExtension());
        if(script!=null)
            return new JellyRequestDispatcher(it,script);
        return null;
    }
}
