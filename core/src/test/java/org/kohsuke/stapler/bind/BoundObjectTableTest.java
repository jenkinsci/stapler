package org.kohsuke.stapler.bind;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.UUID;
import org.htmlunit.TextPage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.StaplerResponse2;
import org.kohsuke.stapler.test.JettyTestCase;

/**
 * @author Kohsuke Kawaguchi
 */
class BoundObjectTableTest extends JettyTestCase {
    /**
     * Exports an object and see if it can be reached.
     */
    @Test
    void testExport() throws Exception {
        TextPage page = createWebClient().getPage(new URL(url, "/bind"));
        assertEquals("hello world", page.getContent());
    }

    public HttpResponse doBind() throws IOException {
        Bound h = webApp.boundObjectTable.bind(new HelloWorld("hello world"));
        System.out.println(h.getURL());
        return h;
    }

    public static class HelloWorld {
        private final String message;

        @SuppressWarnings("checkstyle:redundantmodifier")
        public HelloWorld(String message) {
            this.message = message;
        }

        public void doIndex(StaplerResponse2 rsp) throws IOException {
            rsp.setContentType("text/plain;charset=UTF-8");
            rsp.getWriter().write(message);
        }
    }

    @AfterEach
    void resetIdPrefix() {
        BoundObjectTable.setIdPrefix("");
    }

    @Test
    void idsAreBareUuidsByDefault() {
        String id = BoundObjectTable.newId();
        assertEquals(id, UUID.fromString(id).toString());
        assertNull(BoundObjectTable.serverIdOf(id));
    }

    @Test
    void idsCarryTheConfiguredPrefix() {
        BoundObjectTable.setIdPrefix("ctl-a.1_x");
        String id = BoundObjectTable.newId();
        assertTrue(id.startsWith("ctl-a.1_x-"), id);
        UUID.fromString(id.substring("ctl-a.1_x-".length()));
        assertEquals("ctl-a.1_x", BoundObjectTable.serverIdOf(id));
        assertNotEquals(id, BoundObjectTable.newId());
    }

    @Test
    void prefixedObjectIsReachable() throws Exception {
        BoundObjectTable.setIdPrefix("ctl-a");
        TextPage page = createWebClient().getPage(new URL(url, "/bind"));
        assertEquals("hello world", page.getContent());
        String path = page.getUrl().getPath();
        assertTrue(path.startsWith(BoundObjectTable.PREFIX + "ctl-a-"), path);
        String id = path.substring(BoundObjectTable.PREFIX.length()).replaceAll("/.*", "");
        assertEquals("ctl-a", BoundObjectTable.serverIdOf(id));
    }

    @Test
    void invalidPrefixesAreRejected() {
        for (String bad : new String[] {"a/b", "a?b", "a\"b", "a%20", "a b", "x".repeat(65)}) {
            assertThrows(IllegalArgumentException.class, () -> BoundObjectTable.setIdPrefix(bad), bad);
        }
        assertEquals("", BoundObjectTable.getIdPrefix());
    }

    @Test
    void serverIdOfIgnoresIdsWithoutOne() {
        String uuid = UUID.randomUUID().toString();
        assertNull(BoundObjectTable.serverIdOf(uuid));
        assertNull(BoundObjectTable.serverIdOf("-" + uuid));
        assertNull(BoundObjectTable.serverIdOf("a/b-" + uuid));
        assertNull(BoundObjectTable.serverIdOf("ctl-a-not-a-uuid-at-all-not-a-uuid-at-a"));
        assertNull(BoundObjectTable.serverIdOf("short"));
        assertEquals("jenkins-a", BoundObjectTable.serverIdOf("jenkins-a-" + uuid));
    }

    @Test
    void thePrefixIsReadFromTheSystemPropertyWhenTheClassLoads() throws Exception {
        assertEquals("ctl-a", idPrefixLoadedWith("ctl-a"));
    }

    @Test
    void anInvalidPrefixInTheSystemPropertyIsIgnored() throws Exception {
        assertEquals("", idPrefixLoadedWith("a/b"));
        assertEquals("", idPrefixLoadedWith(""));
    }

    /** The prefix of {@link BoundObjectTable} loaded afresh, as at startup, with its system property set to this. */
    private static String idPrefixLoadedWith(String value) throws Exception {
        String property = BoundObjectTable.class.getName() + ".idPrefix";
        String was = System.getProperty(property);
        System.setProperty(property, value);
        try {
            Class<?> fresh = Class.forName(BoundObjectTable.class.getName(), true, new Isolating());
            assertNotEquals(BoundObjectTable.class, fresh);
            return (String) fresh.getMethod("getIdPrefix").invoke(null);
        } finally {
            if (was == null) {
                System.clearProperty(property);
            } else {
                System.setProperty(property, was);
            }
        }
    }

    /** Loads {@link BoundObjectTable} (and its nested classes) itself, and everything else from the test's loader. */
    private static final class Isolating extends ClassLoader {
        Isolating() {
            super(BoundObjectTableTest.class.getClassLoader());
        }

        @Override
        protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
            if (!name.equals(BoundObjectTable.class.getName())
                    && !name.startsWith(BoundObjectTable.class.getName() + "$")) {
                return super.loadClass(name, resolve);
            }
            synchronized (getClassLoadingLock(name)) {
                Class<?> c = findLoadedClass(name);
                if (c == null) {
                    try (InputStream in = getParent().getResourceAsStream(name.replace('.', '/') + ".class")) {
                        if (in == null) {
                            throw new ClassNotFoundException(name);
                        }
                        byte[] bytes = in.readAllBytes();
                        c = defineClass(name, bytes, 0, bytes.length);
                    } catch (IOException e) {
                        throw new ClassNotFoundException(name, e);
                    }
                }
                return c;
            }
        }
    }
}
