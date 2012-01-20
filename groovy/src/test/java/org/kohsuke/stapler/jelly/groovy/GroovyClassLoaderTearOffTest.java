package org.kohsuke.stapler.jelly.groovy;

import org.apache.commons.io.FileUtils;
import org.apache.commons.jelly.JellyContext;
import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.XMLOutput;
import org.kohsuke.stapler.MetaClassLoader;
import org.kohsuke.stapler.test.AbstractStaplerTest;

import java.io.File;
import java.io.IOException;

/**
 * @author Kohsuke Kawaguchi
 */
public class GroovyClassLoaderTearOffTest extends AbstractStaplerTest {
    public void testFoo() throws IOException, JellyTagException {
        File f = File.createTempFile("groovy","groovy");

        try {
            MetaClassLoader mcl = webApp.getMetaClass(Foo.class).classLoader;
            GroovyClassLoaderTearOff t = mcl.getTearOff(GroovyClassLoaderTearOff.class);
            
            FileUtils.writeStringToFile(f,"context.setVariable('x',1)");

            JellyContext context = new JellyContext();
            XMLOutput w = XMLOutput.createXMLOutput(System.out);
            t.parse(f.toURL()).run(context, w);
            assertEquals(1,context.getVariable("x"));

            // reload different content in the same URL, make sure new class gets loaded
            FileUtils.writeStringToFile(f,"context.setVariable('x',2)");
            t.parse(f.toURL()).run(context, w);
            assertEquals(2, context.getVariable("x"));
        } finally {
            f.delete();
        }
    }
    
    public static class Foo {}
}
