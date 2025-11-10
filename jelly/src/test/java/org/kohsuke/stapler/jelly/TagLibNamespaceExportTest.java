package org.kohsuke.stapler.jelly;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URL;
import org.htmlunit.WebClient;
import org.htmlunit.html.HtmlPage;
import org.junit.jupiter.api.Test;
import org.kohsuke.stapler.test.JettyTestCase;

/**
 * @author Kohsuke Kawaguchi
 */
class TagLibNamespaceExportTest extends JettyTestCase {

    @Test
    void test1() throws Exception {
        WebClient wc = createWebClient();
        HtmlPage page = wc.getPage(new URL(url, "/"));
        String content = page.getWebResponse().getContentAsString();
        System.out.println(content);
        assertTrue(content.contains("foo"));
        assertFalse(content.contains("bar"));
        assertFalse(content.contains("zot"));
    }
}
