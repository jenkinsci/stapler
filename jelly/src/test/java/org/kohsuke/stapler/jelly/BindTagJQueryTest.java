package org.kohsuke.stapler.jelly;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.kohsuke.stapler.framework.adjunct.AdjunctManager;
import org.kohsuke.stapler.test.JettyTestCase;

import java.net.URL;

/**
 * Description
 *
 * @author Robert Sandell &lt;sandell.robert@gmail.com&gt;
 */
public class BindTagJQueryTest extends JettyTestCase {
    private String value;

    public AdjunctManager am;
    private int number;

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
        assertFalse(content.contains("/am/org/kohsuke/stapler/framework/prototype/prototype.js"));
        page.executeJavaScript("v.foo('hello world', 2);");
        assertEquals("hello world",value);
        assertEquals(2, number);
    }

    public void jsFoo(String arg, int arg2) {
        this.value = arg;
        this.number = arg2;
    }
}
