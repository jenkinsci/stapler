package org.kohsuke.stapler.jelly;

import org.htmlunit.WebClient;
import org.htmlunit.html.HtmlPage;
import org.kohsuke.stapler.framework.adjunct.AdjunctManager;
import org.kohsuke.stapler.test.JettyTestCase;

import java.net.URL;

/**
 * @author Kohsuke Kawaguchi
 */
public class BindTagTest extends JettyTestCase {
    private String value;

    public AdjunctManager am;
    private int number;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        this.am = new AdjunctManager(servletContext,getClass().getClassLoader(),"am");
    }


    public void test1() throws Exception {
        WebClient wc = createWebClient();
        HtmlPage page = wc.getPage(new URL(url, "/"));
        String content = page.getWebResponse().getContentAsString();
        System.out.println(content);
        page.executeJavaScript("v.foo('hello world', 2);");
        wc.getJavaScriptEngine().processPostponedActions();
        wc.waitForBackgroundJavaScript(10000);
        assertEquals("hello world",value);
        assertEquals(2, number);
    }

    public void jsFoo(String arg, int arg2) {
        this.value = arg;
        this.number = arg2;
    }
}
