package org.kohsuke.stapler.jelly.jruby;

import org.apache.commons.io.IOUtils;
import org.apache.commons.jelly.Script;
import org.jruby.RubyClass;
import org.jruby.RubyObject;
import org.jruby.embed.ScriptingContainer;
import org.kohsuke.stapler.MetaClassLoader;

import java.io.IOException;
import java.net.URL;

/**
 * @author Kohsuke Kawaguchi
 */
public class JRubyClassLoaderTearOff {
    private final MetaClassLoader owner;
    // TODO: is this scoping right?
    private final ScriptingContainer jruby;
    private final RubyClass scriptImpl;

    public JRubyClassLoaderTearOff(MetaClassLoader owner) {
        this.owner = owner;
        this.jruby = new ScriptingContainer();
        jruby.setClassLoader(owner.loader);
        scriptImpl = (RubyClass)jruby.runScriptlet("require 'org/kohsuke/stapler/jelly/jruby/JRubyJellyScriptImpl'; JRubyJellyScriptImpl");

    }

    public Script parse(URL script) throws IOException {
        try {
            String erb = IOUtils.toString(script.openStream(), "UTF-8");
            Object o = jruby.callMethod(scriptImpl, "new", erb);
            return (Script) o;
        } catch (Exception e) {
            // TODO: better error handling
            throw new Error(e);
        }
    }
}
