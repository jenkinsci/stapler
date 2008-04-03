package org.kohsuke.stapler.jelly.groovy;

import org.apache.commons.jelly.Script;
import org.kohsuke.stapler.AbstractTearOff;
import org.kohsuke.stapler.MetaClass;

import java.io.IOException;
import java.net.URL;

/**
 * @author Kohsuke Kawaguchi
 */
public class GroovyClassTearOff extends AbstractTearOff<GroovyClassLoaderTearOff,Script,IOException> {
    public GroovyClassTearOff(MetaClass owner) {
        super(owner,GroovyClassLoaderTearOff.class);
    }

    protected Script parseScript(URL res) throws IOException {
        Script script;
        script = classLoader.parse(res);
        return script;
    }
}
