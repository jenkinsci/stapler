package org.kohsuke.stapler.jelly;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.lang.ref.WeakReference;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.commons.jelly.TagLibrary;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.kohsuke.stapler.MetaClassLoader;

class JellyClassLoaderTearOffTest {

    @TempDir
    Path dir;

    /**
     * Groovy views look tag libraries up on every call, so one must not go (with the tag files it compiled) as soon as
     * nothing else holds it.
     */
    @Test
    void aTagLibraryIsKeptThroughGarbageCollection() throws Exception {
        Path lib = Files.createDirectories(dir.resolve("lib/test"));
        Files.writeString(lib.resolve("taglib"), "", StandardCharsets.UTF_8);
        ClassLoader loader =
                new URLClassLoader(new URL[] {dir.toUri().toURL()}, getClass().getClassLoader());
        JellyClassLoaderTearOff tearOff = MetaClassLoader.get(loader).loadTearOff(JellyClassLoaderTearOff.class);

        WeakReference<TagLibrary> first = new WeakReference<>(tearOff.getTagLibrary("/lib/test"));
        assertNotNull(first.get());
        for (int i = 0; i < 5; i++) {
            System.gc();
        }
        assertSame(first.get(), tearOff.getTagLibrary("/lib/test"));
    }
}
