package org.kohsuke.stapler;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.TextPage;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequestSettings;
import org.kohsuke.stapler.test.JettyTestCase;
import org.kohsuke.stapler.verb.GET;
import org.kohsuke.stapler.verb.POST;

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


    //===================================================================


    public final VerbMatch verbMatch = new VerbMatch();

    public class VerbMatch {
        @WebMethod(name="") @GET
        public HttpResponse doGet() {
            return HttpResponses.plainText("Got GET");
        }

        @WebMethod(name="") @POST
        public HttpResponse doPost() {
            return HttpResponses.plainText("Got POST");
        }
    }

    /**
     * Tests the dispatching of WebMethod based on verb
     */
    public void testVerbMatch() throws Exception {
        WebClient wc = new WebClient();

        check(wc, HttpMethod.GET);
        check(wc, HttpMethod.POST);
        try {
            check(wc, HttpMethod.DELETE);
            fail("There's no route for DELETE");
        } catch (FailingHttpStatusCodeException e) {
            assertEquals(404, e.getStatusCode());
        }
    }

    private void check(WebClient wc, HttpMethod m) throws java.io.IOException {
        TextPage p = wc.getPage(new WebRequestSettings(new URL(url, "verbMatch/"), m));
        assertEquals("Got "+m.name()+"\n", p.getContent());
    }
}