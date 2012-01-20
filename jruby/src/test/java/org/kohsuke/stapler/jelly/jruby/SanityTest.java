package org.kohsuke.stapler.jelly.jruby;

import org.apache.commons.jelly.JellyContext;
import org.apache.commons.jelly.Script;
import org.apache.commons.jelly.XMLOutput;
import org.kohsuke.stapler.MetaClass;
import org.kohsuke.stapler.jelly.JellyClassLoaderTearOff;
import org.kohsuke.stapler.jelly.jruby.erb.ERbClassTearOff;
import org.kohsuke.stapler.test.AbstractStaplerTest;

import java.io.StringWriter;

/**
 * @author Kohsuke Kawaguchi
 */
public class SanityTest extends AbstractStaplerTest {
    public void testSanityERb() throws Exception {
        MetaClass mc = webApp.getMetaClass(SanityTest.class);
        ERbClassTearOff jr = mc.getTearOff(ERbClassTearOff.class);
        Script s = jr.parseScript(getClass().getResource("test_sanity.erb"));

        JellyContext context = mc.classLoader.getTearOff(JellyClassLoaderTearOff.class).createContext();
        context.setVariable("name","ERB");

        StringWriter out = new StringWriter();
        s.run(context, XMLOutput.createXMLOutput(out));

        assertTrue(out.toString().contains("Hello ERB!"));
        assertTrue(out.toString().contains("Hello from Jelly to ERB"));
        assertTrue(out.toString().contains("<i>Nested in ERB (47)</i>"));
    }

    public void testSanityHaml() throws Exception {
        MetaClass mc = webApp.getMetaClass(SanityTest.class);
        ERbClassTearOff jr = mc.getTearOff(ERbClassTearOff.class);
        Script s = jr.parseScript(getClass().getResource("test_sanity.haml"));

        JellyContext context = mc.classLoader.getTearOff(JellyClassLoaderTearOff.class).createContext();
        context.setVariable("name","Haml");

        StringWriter out = new StringWriter();
        s.run(context, XMLOutput.createXMLOutput(out));

        assertTrue(out.toString().contains("Hello Haml!"));
        assertTrue(out.toString().contains("Hello from Jelly to Haml"));
        assertTrue(out.toString().contains("Nested in Haml (47)"));
    }
}
