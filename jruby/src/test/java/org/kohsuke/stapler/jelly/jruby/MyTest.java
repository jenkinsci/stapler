package org.kohsuke.stapler.jelly.jruby;

import org.apache.commons.jelly.JellyContext;
import org.apache.commons.jelly.Script;
import org.apache.commons.jelly.XMLOutput;
import org.kohsuke.stapler.MetaClass;
import org.kohsuke.stapler.MetaClassLoader;
import org.kohsuke.stapler.StaplerTestCase;
import org.kohsuke.stapler.jelly.DefaultScriptInvoker;
import org.kohsuke.stapler.jelly.JellyClassLoaderTearOff;

import java.io.StringWriter;

/**
 * @author Kohsuke Kawaguchi
 */
public class MyTest extends StaplerTestCase {
    public void testSanity() throws Exception {

        MetaClass mc = webApp.getMetaClass(MyTest.class);
        JRubyClassTearOff jr = mc.getTearOff(JRubyClassTearOff.class);
        Script s = jr.parseScript(getClass().getResource("testSanity.erb"));

        JellyContext context = mc.classLoader.getTearOff(JellyClassLoaderTearOff.class).createContext();
        context.setVariable("name","ERB");

        StringWriter out = new StringWriter();
        s.run(context, XMLOutput.createXMLOutput(out));

        System.out.println(out);
        assertTrue(out.toString().contains("Hello ERB!"));
        assertTrue(out.toString().contains("Hello from Jelly to ERB"));
        assertTrue(out.toString().contains("<i>Nested in ERB (47)</i>"));
    }
}
