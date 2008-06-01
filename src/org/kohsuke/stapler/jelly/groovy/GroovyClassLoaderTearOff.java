package org.kohsuke.stapler.jelly.groovy;

import groovy.lang.GroovyClassLoader;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.kohsuke.stapler.MetaClass;
import org.kohsuke.stapler.MetaClassLoader;

import java.io.IOException;
import java.net.URL;

/**
 * @author Kohsuke Kawaguchi
 */
public class GroovyClassLoaderTearOff {
    private final MetaClassLoader owner;

    private final GroovyClassLoader gcl;

    public GroovyClassLoaderTearOff(MetaClassLoader owner) {
        this.owner = owner;

        gcl = createGroovyClassLoader();
    }

    private GroovyClassLoader createGroovyClassLoader() {
        CompilerConfiguration cc = new CompilerConfiguration();
        // use GroovyClosureScript class as the base class of the compiled script,
        // so that we can set a delegate.
        cc.setScriptBaseClass(GroovyClosureScript.class.getName());

        // enable re-compilation support
        cc.setRecompileGroovySource(MetaClass.NO_CACHE);
        return new GroovyClassLoader(owner.loader,cc) {
            /**
             * Groovy calls this method to locate .groovy script files,
             * so during the development it's important to check the
             * resource path before target/classes.
             */
            @Override
            public URL getResource(String name) {
                // allow the resource path to take precedence when loading script
                if(MetaClassLoader.debugLoader!=null) {
                    URL res = MetaClassLoader.debugLoader.loader.getResource(name);
                    if(res!=null)
                        return res;
                }
                return super.getResource(name);
            }
        };
    }

    public GroovierJellyScript parse(URL script) throws IOException {
        return new GroovierJellyScript(gcl.parseClass(script.openStream(), script.toExternalForm()));
    }
}
