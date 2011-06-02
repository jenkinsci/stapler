package org.kohsuke.stapler.jelly.jruby;

import org.apache.commons.io.IOUtils;
import org.apache.commons.jelly.Script;
import org.jruby.embed.LocalContextScope;
import org.jruby.embed.LocalVariableBehavior;
import org.jruby.embed.ScriptingContainer;
import org.kohsuke.stapler.WebApp;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Hiroshi Nakamura
 */
public class JRubyScriptProvider {
    private ScriptingContainer jruby = null;

    private Map<String, Object> scriptClasses = new HashMap<String, Object>();

    private Object defaultScriptClass = null;

    JRubyScriptProvider() {
    }

    public Script getScript(URL path) throws IOException {
        ScriptingContainer sc = getScriptingContainer();
        Object scriptClass = getScriptClass(path.getPath());
        try {
            String template = IOUtils.toString(path.openStream(), "UTF-8");
            return (Script) sc.callMethod(scriptClass, "new", template);
        } catch (Exception e) {
            throw (IOException) new IOException().initCause(e);
        }
    }

    private Object getScriptClass(String path) {
        Object script = null;
        int lastIndex = path.lastIndexOf('.');
        if (lastIndex >= 0) {
            script = scriptClasses.get(path.substring(lastIndex + 1));
        }
        if (script != null) return script;
        return defaultScriptClass;
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
            Object erbScriptClass = jruby.runScriptlet("JRubyJellyScriptImpl::JRubyJellyERbScript");
            Object hamlScriptClass = jruby.runScriptlet("JRubyJellyScriptImpl::JRubyJellyHamlScript");
            scriptClasses.put("erb", erbScriptClass);
            scriptClasses.put("haml", hamlScriptClass);
            defaultScriptClass = scriptClasses.get("erb");
        }
        return jruby;
    }
}
