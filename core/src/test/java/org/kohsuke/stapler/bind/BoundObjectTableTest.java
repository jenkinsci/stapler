package org.kohsuke.stapler.bind;

import java.io.IOException;
import java.net.URL;
import org.htmlunit.TextPage;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.StaplerResponse2;
import org.kohsuke.stapler.test.JettyTestCase;

/**
 * @author Kohsuke Kawaguchi
 */
public class BoundObjectTableTest extends JettyTestCase {
    /**
     * Exports an object and see if it can be reached.
     */
    public void testExport() throws Exception {
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

        public HelloWorld(String message) {
            this.message = message;
        }

        public void doIndex(StaplerResponse2 rsp) throws IOException {
            rsp.setContentType("text/plain;charset=UTF-8");
            rsp.getWriter().write(message);
        }
    }
}
