package org.kohsuke.stapler.jelly.groovy;

import org.kohsuke.stapler.MetaClassLoader;
import org.apache.commons.jelly.Script;
import groovy.lang.GroovyClassLoader;

import java.net.URL;
import java.io.IOException;

/**
 * @author Kohsuke Kawaguchi
 */
public class GroovyClassLoaderTearOff {
    private final MetaClassLoader owner;

    private final GroovyClassLoader gcl;

    public GroovyClassLoaderTearOff(MetaClassLoader owner) {
        this.owner = owner;
        this.gcl = new GroovyClassLoader(owner.loader);
    }

    public Script parse(URL script) throws IOException {
        return new GroovyScript(gcl.parseClass(script.openStream(), script.toExternalForm()));
    }
}
