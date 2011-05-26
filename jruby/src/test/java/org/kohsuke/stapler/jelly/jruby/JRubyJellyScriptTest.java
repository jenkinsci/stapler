package org.kohsuke.stapler.jelly.jruby;

import org.apache.commons.io.IOUtils;
import org.apache.commons.jelly.JellyContext;
import org.apache.commons.jelly.Script;
import org.apache.commons.jelly.XMLOutput;
import org.jruby.embed.LocalContextScope;
import org.jruby.embed.LocalVariableBehavior;
import org.jruby.embed.ScriptingContainer;
import org.kohsuke.stapler.MetaClass;
import org.kohsuke.stapler.StaplerTestCase;
import org.kohsuke.stapler.jelly.JellyClassLoaderTearOff;

import java.io.IOException;
import java.io.StringWriter;

/**
 * @author Hiroshi Nakamura
 */
public class JRubyJellyScriptTest extends StaplerTestCase {
    private ScriptingContainer ruby;
    private JellyContext context;

    public JRubyJellyScriptTest() {
        ruby = new ScriptingContainer(LocalContextScope.SINGLETHREAD, LocalVariableBehavior.TRANSIENT);
        ruby.setClassLoader(getClass().getClassLoader());
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        MetaClass mc = webApp.getMetaClass(SanityTest.class);
        context = mc.classLoader.getTearOff(JellyClassLoaderTearOff.class).createContext();
    }

    public void testContext() throws Exception {
        Script script = getScript("test_context.erb");
        context.setVariable("name", "ERB");
        StringWriter out = new StringWriter();
        script.run(context, XMLOutput.createXMLOutput(out));
        assertEquals("ERB", out.toString());
    }

    public void testTaglib() throws Exception {
        Script script = getScript("test_taglib.erb");
        context.setVariable("name", "ERB");
        StringWriter out = new StringWriter();
        script.run(context, XMLOutput.createXMLOutput(out));
        assertEquals("<b>Hello from Jelly to ERB</b><i>\n" +
                "  47\n" +
                "</i>", out.toString());
    }

    private Script getScript(String fixture) throws IOException {
        ruby.put("template", getTemplate(fixture));
        return (Script) ruby.runScriptlet(
                "require 'org/kohsuke/stapler/jelly/jruby/JRubyJellyScriptImpl'\n" +
                        "JRubyJellyScriptImpl.new(template)");

    }

    private String getTemplate(String fixture) throws IOException {
        return IOUtils.toString(getClass().getResource(fixture).openStream(), "UTF-8");
    }
}
