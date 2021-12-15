package org.kohsuke.stapler.jelly;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.kohsuke.stapler.framework.adjunct.AdjunctManager;
import org.kohsuke.stapler.test.JettyTestCase;

import java.net.URL;

/**
 * @author Kohsuke Kawaguchi
 */
public class BindTagTest extends JettyTestCase {
    private String value;

    public AdjunctManager am;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        this.am = new AdjunctManager(servletContext,getClass().getClassLoader(),"am");
    }


    public void test1() throws Exception {
        WebClient wc = new WebClient();
        HtmlPage page = wc.getPage(new URL(url, "/"));
        String content = page.getWebResponse().getContentAsString();
        System.out.println(content);
        //Check that prototype is included in the page
        assertTrue(content.contains("/am/org/kohsuke/stapler/framework/prototype/prototype.js"));
        page.executeJavaScript("v.foo('hello world');");
        wc.getJavaScriptEngine().processPostponedActions();
        wc.waitForBackgroundJavaScript(10000);
        assertEquals("hello world",value);
    }

    public void jsFoo(String arg) {
        this.value = arg;
    }
}
