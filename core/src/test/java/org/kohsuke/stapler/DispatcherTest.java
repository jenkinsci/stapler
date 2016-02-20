package org.kohsuke.stapler;

import com.gargoylesoftware.htmlunit.TextPage;
import com.gargoylesoftware.htmlunit.WebClient;
import org.kohsuke.stapler.test.JettyTestCase;

import java.net.URL;

/**
 * @author Kohsuke Kawaguchi
 */
public class DispatcherTest extends JettyTestCase {

    public final IndexDispatchByName indexDispatchByName = new IndexDispatchByName();

    public class IndexDispatchByName {
        @WebMethod(name="")
        public HttpResponse doHelloWorld() {
            return HttpResponses.plainText("Hello world");
        }
    }

    /**
     * Makes sure @WebMethod(name="") has the intended effect of occupying the root of the object in the URL space.
     */
    public void testIndexDispatchByName() throws Exception {
        WebClient wc = new WebClient();
        TextPage p = wc.getPage(new URL(url, "indexDispatchByName"));
        assertEquals("Hello world\n", p.getContent());
    }
}