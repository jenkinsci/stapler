package org.kohsuke.stapler.jelly.jruby;

import org.apache.commons.io.IOUtils;
import org.apache.commons.jelly.JellyContext;
import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.Script;
import org.apache.commons.jelly.XMLOutput;
import org.jruby.embed.LocalContextScope;
import org.jruby.embed.LocalVariableBehavior;
import org.jruby.embed.ScriptingContainer;
import org.jruby.runtime.backtrace.TraceType;
import org.kohsuke.stapler.MetaClass;
import org.kohsuke.stapler.StaplerTestCase;
import org.kohsuke.stapler.jelly.JellyClassLoaderTearOff;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

/**
 * @author Hiroshi Nakamura
 */
public class JRubyJellyHamlScriptTest extends StaplerTestCase {
    private ScriptingContainer ruby;
    private JellyContext context;

    public JRubyJellyHamlScriptTest() {
        ruby = new ScriptingContainer(LocalContextScope.SINGLETHREAD, LocalVariableBehavior.TRANSIENT);
        ruby.setClassLoader(getClass().getClassLoader());
        ruby.put("gem_path", getClass().getClassLoader().getResource("gem").getPath());
        ruby.runScriptlet("ENV['GEM_PATH'] = gem_path\n" +
                "require 'rubygems'\n" +
                "require 'org/kohsuke/stapler/jelly/jruby/JRubyJellyScriptImpl'");
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        MetaClass mc = webApp.getMetaClass(JRubyJellyHamlScriptTest.class);
        context = mc.classLoader.getTearOff(JellyClassLoaderTearOff.class).createContext();
    }

    public void testContext() throws Exception {
        Script script = getScript("test_context.haml");
        context.setVariable("name", "HAML");
        StringWriter out = new StringWriter();
        script.run(context, XMLOutput.createXMLOutput(out));
        assertEquals("HAML\n", out.toString());
    }

    public void testTaglib() throws Exception {
        Script script = getScript("test_taglib.haml");
        context.setVariable("name", "ERB");
        StringWriter out = new StringWriter();
        script.run(context, XMLOutput.createXMLOutput(out));
        assertEquals("<b>Hello from Jelly to ERB</b><i>" +
                "47\n" +
                "</i>", out.toString());
    }

    public void testNoSuchTaglib() throws Exception {
        Script script = getScript("test_nosuch_taglib.haml");
        StringWriter out = new StringWriter();
        try {
            script.run(context, XMLOutput.createXMLOutput(out));
            fail("should raise JellyTagException");
        } catch (JellyTagException jte) {
            assertTrue(true);
        }
    }

    public void testNoSuchTagscript() throws Exception {
        Script script = getScript("test_nosuch_tagscript.haml");
        StringWriter out = new StringWriter();
        try {
            script.run(context, XMLOutput.createXMLOutput(out));
            fail("should raise JellyTagException");
        } catch (JellyTagException jte) {
            assertTrue(true);
        }
    }

    private Script getScript(String fixture) throws IOException {
        ruby.put("template", getTemplate(fixture));
        return (Script) ruby.runScriptlet(
                "JRubyJellyScriptImpl::JRubyJellyHamlScript.new(template)");

    }

    private String getTemplate(String fixture) throws IOException {
        return IOUtils.toString(getClass().getResource(fixture).openStream(), "UTF-8");
    }
}
