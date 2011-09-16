package org.kohsuke.stapler.jelly.jruby.erb;

import org.jruby.Ruby;
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
    protected RubyTemplateContainer createContainer(Ruby jruby) {
        jruby.getLoadService().require("org/kohsuke/stapler/jelly/jruby/erb/JRubyJellyERbScript");
        return new RubyTemplateContainer(
            jruby.getModule("JRubyJellyScriptImpl").getClass("JRubyJellyERbScript"),
            this, jruby);
    }
}
