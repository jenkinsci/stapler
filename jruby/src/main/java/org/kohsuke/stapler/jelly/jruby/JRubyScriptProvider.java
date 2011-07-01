package org.kohsuke.stapler.jelly.jruby;

import org.apache.commons.io.IOUtils;
import org.apache.commons.jelly.Script;
import org.jruby.RubyClass;
import org.jruby.anno.JRubyClass;
import org.jruby.embed.LocalContextScope;
import org.jruby.embed.LocalVariableBehavior;
import org.jruby.embed.ScriptingContainer;
import org.kohsuke.stapler.WebApp;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Hiroshi Nakamura
 */
public class JRubyScriptProvider {
    private ScriptingContainer jruby = null;

    private Map<String, RubyClass> scriptClasses = new LinkedHashMap<String, RubyClass>();

    JRubyScriptProvider() {
    }

    public Script parseScript(URL path) throws IOException {
        ScriptingContainer sc = getScriptingContainer();
        RubyClass scriptClass = getScriptClass(path.getPath());
        try {
            String template = IOUtils.toString(path.openStream(), "UTF-8");
            return (Script) sc.callMethod(scriptClass, "new", template);
        } catch (Exception e) {
            throw (IOException) new IOException().initCause(e);
        }
    }

    public Collection<String> getSupportedExtensions() {
        getScriptingContainer();
        return scriptClasses.keySet();
    }

    private RubyClass getScriptClass(String path) {
        RubyClass script = null;
        int lastIndex = path.lastIndexOf('.');
        if (lastIndex >= 0) {
            script = scriptClasses.get(path.substring(lastIndex + 1));
        }
        return script;
    }

    private synchronized ScriptingContainer getScriptingContainer() {
        if (jruby == null) {
            jruby = new ScriptingContainer(LocalContextScope.SINGLETHREAD, LocalVariableBehavior.TRANSIENT);
            ClassLoader cl = WebApp.getCurrent().getClassLoader();
            jruby.setClassLoader(cl);
            jruby.put("gem_path", cl.getResource("gem").getPath());
            jruby.runScriptlet("ENV['GEM_PATH'] = gem_path\n" +
                    "require 'rubygems'\n" +
                    "require 'org/kohsuke/stapler/jelly/jruby/JRubyJellyScriptImpl'");

            assignScriptClass("erb", "JRubyJellyScriptImpl::JRubyJellyERbScript");
            assignScriptClass("haml", "JRubyJellyScriptImpl::JRubyJellyHamlScript");
        }
        return jruby;
    }

    private void assignScriptClass(String extension, String className) {
        scriptClasses.put(extension, (RubyClass)jruby.runScriptlet(className));
    }
}
