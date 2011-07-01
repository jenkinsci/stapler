package org.kohsuke.stapler.jelly.jruby.haml;

import org.kohsuke.stapler.MetaClass;
import org.kohsuke.stapler.jelly.jruby.AbstractRubyTearOff;

/**
 *
 * @author Kohsuke Kawaguchi
 */
public class HamlClassTearOff extends AbstractRubyTearOff {
    public HamlClassTearOff(MetaClass owner) {
        super(owner);
    }

    @Override
    protected String getDefaultScriptExtension() {
        return ".haml";
    }
}
