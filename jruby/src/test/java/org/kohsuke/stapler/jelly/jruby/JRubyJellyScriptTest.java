package org.kohsuke.stapler.jelly.jruby;

import org.apache.commons.io.IOUtils;
import org.apache.commons.jelly.*;
import org.jruby.embed.LocalContextScope;
import org.jruby.embed.LocalVariableBehavior;
import org.jruby.embed.ScriptingContainer;
import org.jruby.runtime.backtrace.TraceType;
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
        ruby.getProvider().getRubyInstanceConfig().setTraceType(TraceType.traceTypeFor("raw"));
        ruby.setOutput(System.out);
        ruby.setError(System.err);
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

    public void testNoSuchTaglib() throws Exception {
        Script script = getScript("test_nosuch_taglib.erb");
        StringWriter out = new StringWriter();
        try {
            script.run(context, XMLOutput.createXMLOutput(out));
            fail("should raise JellyTagException");
        } catch (JellyTagException jte) {
            assertTrue(true);
        }
    }

    public void testNoSuchTagscript() throws Exception {
        Script script = getScript("test_nosuch_tagscript.erb");
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
                "require 'org/kohsuke/stapler/jelly/jruby/JRubyJellyScriptImpl'\n" +
                        "JRubyJellyScriptImpl::JRubyJellyERbScript.new(template)");

    }

    private String getTemplate(String fixture) throws IOException {
        return IOUtils.toString(getClass().getResource(fixture).openStream(), "UTF-8");
    }
}
