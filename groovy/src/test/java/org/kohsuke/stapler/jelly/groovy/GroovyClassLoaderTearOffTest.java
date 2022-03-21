package org.kohsuke.stapler.jelly.groovy;

import java.io.ByteArrayOutputStream;
import org.apache.commons.jelly.JellyContext;
import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.XMLOutput;
import org.kohsuke.stapler.MetaClassLoader;
import org.kohsuke.stapler.test.AbstractStaplerTest;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author Kohsuke Kawaguchi
 */
public class GroovyClassLoaderTearOffTest extends AbstractStaplerTest {
    public void testFoo() throws IOException, JellyTagException {
        Path tmp = Files.createTempFile("groovy", "groovy");

        try {
            MetaClassLoader mcl = webApp.getMetaClass(Foo.class).classLoader;
            GroovyClassLoaderTearOff t = mcl.getTearOff(GroovyClassLoaderTearOff.class);
            
            Files.write(tmp, "context.setVariable('x',1)".getBytes(StandardCharsets.UTF_8));

            JellyContext context = new JellyContext();
            XMLOutput w = XMLOutput.createXMLOutput(System.out);
            t.parse(tmp.toUri().toURL()).run(context, w);
            assertEquals(1,context.getVariable("x"));

            // reload different content in the same URL, make sure new class gets loaded
            Files.write(tmp, "context.setVariable('x',2)".getBytes(StandardCharsets.UTF_8));
            t.parse(tmp.toUri().toURL()).run(context, w);
            assertEquals(2, context.getVariable("x"));
        } finally {
            Files.delete(tmp);
        }
    }

    public void testGettext() throws Exception {
        Path tmp = Files.createTempFile("xxx", ".groovy");

        try {
            MetaClassLoader mcl = webApp.getMetaClass(Foo.class).classLoader;
            GroovyClassLoaderTearOff t = mcl.getTearOff(GroovyClassLoaderTearOff.class);

            Files.write(tmp, "output.write(_('localizable'))".getBytes(StandardCharsets.UTF_8));
            Files.write(tmp.resolveSibling(tmp.getFileName().toString().replaceFirst("[.]groovy$", ".properties")), "localizable=Localizable".getBytes(StandardCharsets.ISO_8859_1));

            JellyContext context = new JellyContext();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            XMLOutput w = XMLOutput.createXMLOutput(baos);
            t.parse(tmp.toUri().toURL()).run(context, w);
            w.close();
            assertEquals("Localizable", baos.toString());
        } catch (Exception x) {
            x.printStackTrace();
            throw x;
        } finally {
            Files.delete(tmp);
        }
    }
    
    public static class Foo {}
}
