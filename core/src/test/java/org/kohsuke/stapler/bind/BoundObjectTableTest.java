package org.kohsuke.stapler.bind;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
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
}
