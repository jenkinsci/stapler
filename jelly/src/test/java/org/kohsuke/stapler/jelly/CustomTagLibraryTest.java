package org.kohsuke.stapler.jelly;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.commons.jelly.JellyContext;
import org.apache.commons.jelly.Script;
import org.apache.commons.jelly.XMLOutput;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.kohsuke.stapler.MetaClass;

class CustomTagLibraryTest {

    @TempDir
    Path dir;

    private boolean noCache;

    @BeforeEach
    void setUp() {
        noCache = MetaClass.NO_CACHE;
    }

    @AfterEach
    void tearDown() {
        MetaClass.NO_CACHE = noCache;
    }

    /**
     * A page compiled once must pick up later edits to the tag files it uses when caching is disabled.
     */
    @Test
    void reloadsModifiedTagFileFromAlreadyCompiledPage() throws Exception {
        MetaClass.NO_CACHE = true;
        File tag = writeTag("first");
        Script page = compilePage();

        assertEquals("first", run(page));

        Files.writeString(tag.toPath(), tagContent("second"), StandardCharsets.UTF_8);
        // make sure the timestamp changes even on file systems with coarse resolution
        assertTrue(tag.setLastModified(tag.lastModified() + 2000));

        assertEquals("second", run(page));
    }

    @Test
    void keepsCompiledTagFileWhenCaching() throws Exception {
        MetaClass.NO_CACHE = false;
        File tag = writeTag("first");
        Script page = compilePage();

        assertEquals("first", run(page));

        Files.writeString(tag.toPath(), tagContent("second"), StandardCharsets.UTF_8);
        assertTrue(tag.setLastModified(tag.lastModified() + 2000));

        assertEquals("first", run(page));
    }

    private File writeTag(String text) throws Exception {
        Path lib = Files.createDirectories(dir.resolve("lib/test"));
        Files.writeString(lib.resolve("taglib"), "", StandardCharsets.UTF_8);
        Path tag = lib.resolve("hello.jelly");
        Files.writeString(tag, tagContent(text), StandardCharsets.UTF_8);
        return tag.toFile();
    }

    private static String tagContent(String text) {
        return "<j:jelly xmlns:j=\"jelly:core\">" + text + "</j:jelly>";
    }

    private Script compilePage() throws Exception {
        Path page = dir.resolve("page.jelly");
        Files.writeString(
                page,
                "<j:jelly xmlns:j=\"jelly:core\" xmlns:t=\"/lib/test\"><t:hello/></j:jelly>",
                StandardCharsets.UTF_8);

        ClassLoader loader =
                new URLClassLoader(new URL[] {dir.toUri().toURL()}, getClass().getClassLoader());
        JellyContext context = new JellyContext();
        context.setClassLoader(loader);
        context.registerTagLibrary("/lib/test", new CustomTagLibrary(context, loader, "/lib/test", "lib/test"));
        return context.compileScript(page.toUri().toURL());
    }

    private static String run(Script page) throws Exception {
        StringWriter w = new StringWriter();
        XMLOutput output = XMLOutput.createXMLOutput(w);
        page.run(new JellyContext(), output);
        output.flush();
        return w.toString();
    }
}
