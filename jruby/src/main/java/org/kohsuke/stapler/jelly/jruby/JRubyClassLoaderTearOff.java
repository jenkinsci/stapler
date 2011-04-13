package org.kohsuke.stapler.jelly.jruby;

import org.kohsuke.stapler.MetaClassLoader;

/**
 * @author Kohsuke Kawaguchi
 */
public class JRubyClassLoaderTearOff {
    private final MetaClassLoader owner;

    public JRubyClassLoaderTearOff(MetaClassLoader owner) {
        this.owner = owner;
    }
}
