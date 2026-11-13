package org.kohsuke.stapler.jelly;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.StringWriter;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.jelly.JellyContext;
import org.apache.commons.jelly.Script;
import org.apache.commons.jelly.XMLOutput;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tag bodies kept after the page is rendered, as a body rendered on demand is.
 */
public class CallTagLibScriptTest {

    @TempDir
    Path dir;

    /** The body of {@code <t:keep>}, as the tag found it. */
    private static Script kept;

    @AfterEach
    void tearDown() {
        kept = null;
    }

    /** Called from {@code keep.jelly}. */
    public static void keep(JellyContext context) {
        kept = (Script) context.getVariable("org.apache.commons.jelly.body");
    }

    @Test
    void aDetachedBodyRunsLikeTheBody() throws Exception {
        render();
        Script detached = CallTagLibScript.detach(kept);
        assertNotSame(kept, detached, "its attrs came from a jelly:define tag");
        assertEquals("hello", run(kept));
        assertEquals("hello", run(detached));
    }

    @Test
    void aDetachedBodyKeepsNoContextOfThePage() throws Exception {
        WeakReference<JellyContext> page = render();
        kept = CallTagLibScript.detach(kept);
        assertCollected(page);
    }

    @Test
    void aBodyKeepsTheContextOfThePageThroughTheTagThatOwnsItsAttrs() throws Exception {
        WeakReference<JellyContext> page = render();
        collectGarbage(page);
        assertNotNull(page.get(), "kept through attrs.ownerTag; if this changes, detach may no longer be needed");
    }

    /** Renders a page whose {@code <t:keep>} is inside a {@code jelly:define} tag; returns the page's context. */
    private WeakReference<JellyContext> render() throws Exception {
        Path lib = Files.createDirectories(dir.resolve("lib/test"));
        Files.writeString(lib.resolve("taglib"), "", StandardCharsets.UTF_8);
        Files.writeString(
                lib.resolve("keep.jelly"),
                "<j:jelly xmlns:j=\"jelly:core\"><j:invokeStatic className=\"" + CallTagLibScriptTest.class.getName()
                        + "\" method=\"keep\"><j:arg value=\"${context}\" type=\"" + JellyContext.class.getName()
                        + "\"/></j:invokeStatic></j:jelly>",
                StandardCharsets.UTF_8);
        Path page = dir.resolve("page.jelly");
        Files.writeString(
                page,
                "<j:jelly xmlns:j=\"jelly:core\" xmlns:d=\"jelly:define\" xmlns:t=\"/lib/test\" xmlns:local=\"local\">"
                        + "<d:taglib uri=\"local\"><d:tag name=\"owner\"><d:invokeBody/></d:tag></d:taglib>"
                        + "<local:owner greeting=\"hello\"><t:keep>${attrs.greeting}</t:keep></local:owner>"
                        + "</j:jelly>",
                StandardCharsets.UTF_8);

        ClassLoader loader =
                new URLClassLoader(new URL[] {dir.toUri().toURL()}, getClass().getClassLoader());
        JellyContext compiling = new JellyContext();
        compiling.setClassLoader(loader);
        compiling.registerTagLibrary("/lib/test", new CustomTagLibrary(compiling, loader, "/lib/test", "lib/test"));
        Script script = compiling.compileScript(page.toUri().toURL());

        JellyContext context = new JellyContext(compiling);
        XMLOutput output = XMLOutput.createXMLOutput(new StringWriter());
        script.run(context, output);
        output.flush();
        assertNotNull(kept);
        return new WeakReference<>(context);
    }

    private static String run(Script body) throws Exception {
        StringWriter w = new StringWriter();
        XMLOutput output = XMLOutput.createXMLOutput(w);
        body.run(new JellyContext(), output);
        output.flush();
        return w.toString();
    }

    private static void assertCollected(WeakReference<?> ref) {
        collectGarbage(ref);
        assertNull(ref.get(), "still reachable");
    }

    /** Collects garbage until the reference is cleared, or gives up. */
    private static void collectGarbage(WeakReference<?> ref) {
        List<byte[]> pressure = new ArrayList<>();
        for (int i = 0; i < 20 && ref.get() != null; i++) {
            System.gc();
            pressure.add(new byte[1 << 20]);
        }
    }
}
