package org.kohsuke.stapler;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.TextPage;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequestSettings;
import org.apache.commons.io.IOUtils;
import org.kohsuke.stapler.interceptor.RequirePOST;
import org.kohsuke.stapler.json.JsonBody;
import org.kohsuke.stapler.test.JettyTestCase;
import org.kohsuke.stapler.verb.GET;
import org.kohsuke.stapler.verb.POST;
import org.kohsuke.stapler.verb.PUT;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
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


    //===================================================================


    public final ArbitraryWebMethodName arbitraryWebMethodName = new ArbitraryWebMethodName();

    public class ArbitraryWebMethodName {
        @WebMethod(name="")
        public HttpResponse notDoPrefix() {
            return HttpResponses.plainText("I'm index");
        }

        // this method is implicitly web method by its name
        public HttpResponse doTheNeedful() {
            return HttpResponses.plainText("DTN");
        }
    }


    public void testArbitraryWebMethodName() throws Exception {
        WebClient wc = new WebClient();
        TextPage p = wc.getPage(new URL(url, "arbitraryWebMethodName"));
        assertEquals("I'm index\n", p.getContent());

        p = wc.getPage(new URL(url, "arbitraryWebMethodName/theNeedful"));
        assertEquals("DTN\n", p.getContent());

    }


    //===================================================================


    public final InterceptorStage interceptorStage = new InterceptorStage();

    public class InterceptorStage {
        @POST
        public HttpResponse doFoo(@JsonBody Point body) {
            return HttpResponses.plainText(body.x+","+body.y);
        }
    }

    public static class Point {
        public int x,y;
    }

    /**
     * POST annotation selection needs to happen before databinding of the parameter happens.
     */
    public void testInterceptorStage() throws Exception {
        WebClient wc = new WebClient();
        try {
            wc.getPage(new URL(url, "interceptorStage/foo"));
            fail("Expected 404");
        } catch (FailingHttpStatusCodeException e) {
            assertEquals(404, e.getStatusCode());
        }

        WebRequestSettings req = new WebRequestSettings(new URL(url, "interceptorStage/foo"), HttpMethod.POST);
        req.setAdditionalHeader("Content-Type","application/json");
        req.setRequestBody("{x:3,y:5}");
        TextPage p = wc.getPage(req);
        assertEquals("3,5\n",p.getContent());

    }


    //===================================================================


    public final Inheritance inheritance = new Inheritance2();
    public class Inheritance {
        @WebMethod(name="foo")
        public HttpResponse doBar(@QueryParameter String q) {
            return HttpResponses.plainText("base");
        }
    }

    public class Inheritance2 extends Inheritance {
        @Override
        public HttpResponse doBar(String q) {
            return HttpResponses.plainText(q);
        }
    }

    public void testInheritance() throws Exception {
        WebClient wc = new WebClient();

        // the request should get to the overriding method and it should still see all the annotations in the base type
        TextPage p = wc.getPage(new URL(url, "inheritance/foo?q=abc"));
        assertEquals("abc\n", p.getContent());

        // doBar is a web method for 'foo', so bar endpoint shouldn't respond
        try {
            wc.getPage(new URL(url, "inheritance/bar"));
            fail();
        } catch (FailingHttpStatusCodeException e) {
            assertEquals(404,e.getStatusCode());
        }
    }

    public final PutInheritance putInheritance = new PutInheritanceImpl();
    public abstract class PutInheritance {
        @WebMethod(name="foo") @PUT
        public abstract HttpResponse doBar(StaplerRequest req) throws IOException;

        @POST
        public HttpResponse doAcme(StaplerRequest req) throws IOException {
            return HttpResponses.plainText("POST: "+IOUtils.toString(req.getInputStream()));
        }
    }

    public class PutInheritanceImpl extends PutInheritance{
        @Override
        public HttpResponse doBar(StaplerRequest req) throws IOException {
            return HttpResponses.plainText(IOUtils.toString(req.getInputStream())+" World!");
        }
    }

    public void testPutInheritance() throws Exception {
        WebClient wc = new WebClient();

        // the request should get to the overriding method and it should still see all the annotations in the base type
        WebRequestSettings wrs = new WebRequestSettings(new URL(url, "putInheritance/foo"), HttpMethod.PUT);
        wrs.setRequestBody("Hello");
        TextPage p = wc.getPage(wrs);
        assertEquals("Hello World!\n", p.getContent());

        // doBar is a web method for 'foo', so bar endpoint shouldn't respond
        try {
            wc.getPage(new URL(url, "putInheritance/bar"));
            fail();
        } catch (FailingHttpStatusCodeException e) {
            assertEquals(404,e.getStatusCode());
        }

        //Invoke Post as well
        wrs = new WebRequestSettings(new URL(url, "putInheritance/acme"), HttpMethod.POST);
        wrs.setRequestBody("Hello");
        p = wc.getPage(wrs);
        assertEquals("POST: Hello\n", p.getContent());
    }



    //===================================================================


    public final RequirePostOnBase requirePostOnBase = new RequirePostOnBase2();
    public abstract class RequirePostOnBase {
        int hit;
        @RequirePOST
        public abstract void doSomething();
    }

    public class RequirePostOnBase2 extends RequirePostOnBase {
        @Override
        public void doSomething() {
            hit++;
        }
    }

    public void testRequirePostOnBase() throws Exception {
        WebClient wc = new WebClient();
        URL url = new URL(this.url, "requirePostOnBase/something");

        try {
            wc.getPage(url);
            fail();
        } catch (FailingHttpStatusCodeException e) {
            assertEquals(HttpServletResponse.SC_METHOD_NOT_ALLOWED, e.getStatusCode());
        }

        // POST should succeed
        wc.getPage(new WebRequestSettings(url, HttpMethod.POST));
        assertEquals(1, requirePostOnBase.hit);
    }
}