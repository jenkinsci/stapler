package org.kohsuke.stapler.jelly.jruby.haml;

import org.jruby.Ruby;
import org.jruby.RubyClass;
import org.jruby.embed.ScriptingContainer;
import org.kohsuke.MetaInfServices;
import org.kohsuke.stapler.jelly.jruby.RubyTemplateContainer;
import org.kohsuke.stapler.jelly.jruby.RubyTemplateLanguage;

/**
 * @author Hiroshi Nakamura
 */
@MetaInfServices
public class HamlLanguage extends RubyTemplateLanguage {
    @Override
    protected String getScriptExtension() {
        return ".haml";
    }

    @Override
    protected Class<HamlClassTearOff> getTearOffClass() {
        return HamlClassTearOff.class;
    }

    @Override
    protected RubyTemplateContainer createContainer(Ruby jruby) {
        String path = '\"'+getClass().getResource("/gem").getPath()+'\"';

        return new RubyTemplateContainer(
            (RubyClass)jruby.evalScriptlet("ENV['GEM_PATH'] = "+path+"\n" +
                "require 'rubygems'\n" +
                "require 'org/kohsuke/stapler/jelly/jruby/JRubyJellyScriptImpl'\n"+
                "require 'org/kohsuke/stapler/jelly/jruby/haml/JRubyJellyHamlScript'\n"+
                "JRubyJellyScriptImpl::JRubyJellyHamlScript"
            ),
            this, jruby);
    }
}
