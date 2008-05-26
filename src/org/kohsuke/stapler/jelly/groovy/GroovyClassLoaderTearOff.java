package org.kohsuke.stapler.jelly.groovy;

import org.kohsuke.stapler.MetaClassLoader;
import org.apache.commons.jelly.Script;
import org.codehaus.groovy.control.CompilerConfiguration;
import groovy.lang.GroovyClassLoader;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;

import java.net.URL;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Kohsuke Kawaguchi
 */
public class GroovyClassLoaderTearOff {
    private final MetaClassLoader owner;

    private final GroovyClassLoader gcl;

    public GroovyClassLoaderTearOff(MetaClassLoader owner) {
        this.owner = owner;

        // use GroovyClosureScript class as the base class of the compiled script,
        // so that we can set a delegate.
        CompilerConfiguration cc = new CompilerConfiguration();
        cc.setScriptBaseClass(GroovyClosureScript.class.getName());
        this.gcl = new GroovyClassLoader(owner.loader,cc);
    }

    public Script parse(URL script) throws IOException {
        return new GroovyScript(gcl.parseClass(script.openStream(), script.toExternalForm()));
    }
}
