package org.kohsuke.stapler.jelly.jruby.erb;

import org.jruby.RubyClass;
import org.jruby.embed.ScriptingContainer;
import org.kohsuke.MetaInfServices;
import org.kohsuke.stapler.jelly.jruby.RubyTemplateContainer;
import org.kohsuke.stapler.jelly.jruby.RubyTemplateLanguage;

/**
 * @author Kohsuke Kawaguchi
 */
@MetaInfServices
public class ERbLanguage extends RubyTemplateLanguage {
    @Override
    protected String getScriptExtension() {
        return ".erb";
    }

    @Override
    protected Class<ERbClassTearOff> getTearOffClass() {
        return ERbClassTearOff.class;
    }

    @Override
    protected RubyTemplateContainer createContainer(ScriptingContainer jruby) {
        return new RubyTemplateContainer(
            (RubyClass)jruby.runScriptlet(
                    "require 'org/kohsuke/stapler/jelly/jruby/erb/JRubyJellyERbScript'\n"+
                    "JRubyJellyScriptImpl::JRubyJellyERbScript"),
            this, jruby);
    }
}
