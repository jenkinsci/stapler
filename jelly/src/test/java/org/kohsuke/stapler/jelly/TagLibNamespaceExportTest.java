package org.kohsuke.stapler.jelly;

import org.htmlunit.WebClient;
import org.htmlunit.html.HtmlPage;
import org.kohsuke.stapler.test.JettyTestCase;

import java.net.URL;

/**
 * @author Kohsuke Kawaguchi
 */
public class TagLibNamespaceExportTest extends JettyTestCase {
    public void test1() throws Exception {
        WebClient wc = createWebClient();
        HtmlPage page = wc.getPage(new URL(url, "/"));
        String content = page.getWebResponse().getContentAsString();
        System.out.println(content);
        assertTrue(content.contains("foo"));
        assertFalse(content.contains("bar"));
        assertFalse(content.contains("zot"));
    }
}
