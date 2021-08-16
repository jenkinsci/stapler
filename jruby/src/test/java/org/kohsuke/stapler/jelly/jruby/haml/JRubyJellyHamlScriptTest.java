package org.kohsuke.stapler.jelly.jruby.haml;

import org.apache.commons.jelly.JellyContext;
import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.Script;
import org.apache.commons.jelly.XMLOutput;
import org.kohsuke.stapler.MetaClass;
import org.kohsuke.stapler.jelly.JellyClassLoaderTearOff;
import org.kohsuke.stapler.jelly.jruby.StaplerJRubyTestCase;

import java.io.IOException;
import java.io.StringWriter;

/**
 * @author Hiroshi Nakamura
 */
public class JRubyJellyHamlScriptTest extends StaplerJRubyTestCase {

    private JellyContext context;

    public JRubyJellyHamlScriptTest() {
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

    /*
    public void testThreadSafety() throws Exception {
        Script script = getScript("test_taglib.haml");
        int num = 100;
        EvaluatorThread[] threads = new EvaluatorThread[num];
        for (int idx = 0; idx < num; ++idx) {
            threads[idx] = new EvaluatorThread(script, idx);
            threads[idx].start();
        }
        for (int idx = 0; idx < num; ++idx) {
            threads[idx].join();
            assertEquals("<b>Hello from Jelly to HAML" + idx + "</b><i>47\n</i>", threads[idx].result);
        }
    }*/

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
                MetaClass mc = webApp.getMetaClass(JRubyJellyHamlScriptTest.class);
                JellyContext context = mc.classLoader.getTearOff(JellyClassLoaderTearOff.class).createContext();
                context.setVariable("name", "HAML" + idx);
                StringWriter out = new StringWriter();
                script.run(context, XMLOutput.createXMLOutput(out));
                result = out.toString();
            } catch (Exception e) {
                result = e.getMessage();
            }
        }
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
        return facet.parseScript(getClass().getResource(fixture));
    }
}
