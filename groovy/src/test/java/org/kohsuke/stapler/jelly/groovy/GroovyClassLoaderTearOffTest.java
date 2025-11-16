package org.kohsuke.stapler.jelly.groovy;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.commons.jelly.JellyContext;
import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.XMLOutput;
import org.junit.jupiter.api.Test;
import org.kohsuke.stapler.MetaClassLoader;
import org.kohsuke.stapler.test.AbstractStaplerTest;

/**
 * @author Kohsuke Kawaguchi
 */
class GroovyClassLoaderTearOffTest extends AbstractStaplerTest {

    @Test
    void testFoo() throws IOException, JellyTagException {
        Path tmp = Files.createTempFile("groovy", "groovy");

        try {
            MetaClassLoader mcl = webApp.getMetaClass(Foo.class).classLoader;
            GroovyClassLoaderTearOff t = mcl.loadTearOff(GroovyClassLoaderTearOff.class);

            Files.writeString(tmp, "context.setVariable('x',1)", StandardCharsets.UTF_8);

            JellyContext context = new JellyContext();
            XMLOutput w = XMLOutput.createXMLOutput(System.out);
            t.parse(tmp.toUri().toURL()).run(context, w);
            assertEquals(1, context.getVariable("x"));

            // reload different content in the same URL, make sure new class gets loaded
            Files.writeString(tmp, "context.setVariable('x',2)", StandardCharsets.UTF_8);
            t.parse(tmp.toUri().toURL()).run(context, w);
            assertEquals(2, context.getVariable("x"));
        } finally {
            Files.delete(tmp);
        }
    }

    @Test
    void testGettext() throws Exception {
        Path tmp = Files.createTempFile("xxx", ".groovy");

        try {
            MetaClassLoader mcl = webApp.getMetaClass(Foo.class).classLoader;
            GroovyClassLoaderTearOff t = mcl.loadTearOff(GroovyClassLoaderTearOff.class);

            Files.writeString(tmp, "output.write(_('localizable'))", StandardCharsets.UTF_8);
            Files.writeString(
                    tmp.resolveSibling(tmp.getFileName().toString().replaceFirst("[.]groovy$", ".properties")),
                    "localizable=Localizable",
                    StandardCharsets.ISO_8859_1);

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

    @Test
    void testTimeZone() throws IOException, JellyTagException {
        Path tmp = Files.createTempFile("groovy", "groovy");

        try {
            MetaClassLoader mcl = webApp.getMetaClass(Foo.class).classLoader;
            GroovyClassLoaderTearOff t = mcl.loadTearOff(GroovyClassLoaderTearOff.class);

            Files.writeString(
                    tmp,
                    "def tz = java.util.TimeZone.getDefault()\ncontext.setVariable('x', (tz.rawOffset + tz.DSTSavings) / 3600000)",
                    StandardCharsets.UTF_8);

            JellyContext context = new JellyContext();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            XMLOutput w = XMLOutput.createXMLOutput(baos);
            try {
                t.parse(tmp.toUri().toURL()).run(context, w);
            } finally {
                w.close();
            }
            assertThat(baos.toString(), is(emptyString()));
            assertThat(context.getVariable("x"), notNullValue());
            assertThat(context.getVariable("x"), instanceOf(Number.class));
        } finally {
            Files.delete(tmp);
        }
    }

    public static class Foo {}
}
