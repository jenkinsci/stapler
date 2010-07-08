package org.kohsuke.stapler.framework;

import com.gargoylesoftware.htmlunit.TextPage;
import com.gargoylesoftware.htmlunit.WebClient;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.test.JettyTestCase;

import java.io.IOException;
import java.net.URL;

/**
 * @author Kohsuke Kawaguchi
 */
public class ExportedObjectTableTest extends JettyTestCase {
    /**
     * Exports an object and see if it can be reached.
     */
    public void testExport() throws Exception {
        TextPage page = new WebClient().getPage(new URL(url, "/export"));
        assertEquals("hello world",page.getContent());
    }

    public HttpResponse doExport() throws IOException {
        ExportHandle h = webApp.exportedObjectTable.export(new HelloWorld("hello world"));
        System.out.println(h.getURL());
        return h;
    }

    public static class HelloWorld {
        private final String message;

        public HelloWorld(String message) {
            this.message = message;
        }

        public void doIndex(StaplerResponse rsp) throws IOException {
            rsp.setContentType("text/plain;charset=UTF-8");
            rsp.getWriter().write(message);
        }
    }
}
