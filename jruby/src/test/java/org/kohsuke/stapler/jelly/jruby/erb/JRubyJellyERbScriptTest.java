package org.kohsuke.stapler.jelly.jruby.erb;

import org.apache.commons.jelly.*;
import org.kohsuke.stapler.MetaClass;
import org.kohsuke.stapler.jelly.JellyClassLoaderTearOff;
import org.kohsuke.stapler.jelly.jruby.StaplerJRubyTestCase;

import java.io.IOException;
import java.io.StringWriter;

/**
 * @author Hiroshi Nakamura
 */
public class JRubyJellyERbScriptTest extends StaplerJRubyTestCase {
    private JellyContext context;

    public JRubyJellyERbScriptTest() {
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        MetaClass mc = webApp.getMetaClass(JRubyJellyERbScriptTest.class);
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

    // TODO https://jenkins.ci.cloudbees.com/job/core/job/stapler/org.kohsuke.stapler$stapler-jruby/1/testReport/junit/org.kohsuke.stapler.jelly.jruby.erb/JRubyJellyERbScriptTest/testThreadSafety/ fails with
    // org.jruby.exceptions.RaiseException: (LoadError) load error: haml/buffer -- java.lang.InternalError: convert=Lambda(a0:L,a1:L,a2:L,a3:L,a4:L,a5:L)=>{ t6:L=Species_L.argL0(a0:L); t7:I=InvocationLinker.testMetaclass(t6:L,a3:L);t7:I}
    // (not locally reproducible); should use @Ignore when switching to JUnit 4
    public void do_not_testThreadSafety() throws Exception {
        Script script = getScript("test_taglib.erb");
        int num = 100;
        EvaluatorThread[] threads = new EvaluatorThread[num];
        for (int idx = 0; idx < num; ++idx) {
            threads[idx] = new EvaluatorThread(script, idx);
            threads[idx].start();
        }
        for (int idx = 0; idx < num; ++idx) {
            threads[idx].join();
            assertEquals("<b>Hello from Jelly to ERB" + idx + "</b><i>\n  47\n</i>", threads[idx].result);
        }
    }

    private class EvaluatorThread extends Thread {
        private final Script script;
        private final int idx;
        private String result = null;

        private EvaluatorThread(Script script, int idx) {
            this.script = script;
            this.idx = idx;
        }

        public void run() {
            try {
                MetaClass mc = webApp.getMetaClass(JRubyJellyERbScriptTest.class);
                JellyContext context = mc.classLoader.getTearOff(JellyClassLoaderTearOff.class).createContext();
                context.setVariable("name", "ERB" + idx);
                StringWriter out = new StringWriter();
                script.run(context, XMLOutput.createXMLOutput(out));
                result = out.toString();
            } catch (Exception e) {
                result = e.getMessage();
            }
        }
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
        return facet.parseScript(getClass().getResource(fixture));
    }
}
