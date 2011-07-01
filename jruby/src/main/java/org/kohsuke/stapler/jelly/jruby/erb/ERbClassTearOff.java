package org.kohsuke.stapler.jelly.jruby.erb;

import org.kohsuke.stapler.MetaClass;
import org.kohsuke.stapler.jelly.jruby.AbstractRubyTearOff;

/**
 * Tear off that manages Ruby ERB views of Java objects (and not ruby objects.)
 *
 * @author Kohsuke Kawaguchi
 */
public class ERbClassTearOff extends AbstractRubyTearOff {
    public ERbClassTearOff(MetaClass owner) {
        super(owner);
    }

    @Override
    protected String getDefaultScriptExtension() {
        return ".erb";
    }
}
