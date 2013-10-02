package org.kohsuke.stapler.jelly;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.kohsuke.stapler.test.JettyTestCase;

import java.net.URL;

/**
 * @author Kohsuke Kawaguchi
 */
public class TagLibNamespaceExportTest extends JettyTestCase {
    public void test1() throws Exception {
        WebClient wc = new WebClient();
        HtmlPage page = wc.getPage(new URL(url, "/"));
        String content = page.getWebResponse().getContentAsString();
        System.out.println(content);
        assertTrue(content.contains("foo"));
        assertTrue(!content.contains("bar"));
        assertTrue(!content.contains("zot"));
    }
}
