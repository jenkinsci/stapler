package org.kohsuke.stapler;

import org.htmlunit.FailingHttpStatusCodeException;
import org.htmlunit.HttpMethod;
import org.htmlunit.Page;
import org.htmlunit.TextPage;
import org.htmlunit.WebClient;
import org.htmlunit.WebRequest;
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
import java.nio.charset.StandardCharsets;

/**
 * @author Kohsuke Kawaguchi
 */
public class DispatcherTest extends JettyTestCase {

    public final IndexDispatchByName indexDispatchByName = new IndexDispatchByName();

    @Override 
    protected void setUp() throws Exception {
        super.setUp();
        MetaClass.LEGACY_GETTER_MODE = true;
        MetaClass.LEGACY_WEB_METHOD_MODE = true;
    }

    public class IndexDispatchByName {
        @WebMethod(name="")
        public HttpResponse doHelloWorld() {
            return HttpResponses.text("Hello world");
        }
    }

    /**
     * Makes sure @WebMethod(name="") has the intended effect of occupying the root of the object in the URL space.
     */
    public void testIndexDispatchByName() throws Exception {
        WebClient wc = createWebClient();
        TextPage p = wc.getPage(new URL(url, "indexDispatchByName"));
        assertEquals("Hello world", p.getContent());
    }


    //===================================================================


    public final VerbMatch verbMatch = new VerbMatch();

    public class VerbMatch {
        @WebMethod(name="") @GET
        public HttpResponse doGet() {
            return HttpResponses.text("Got GET");
        }

        @WebMethod(name="") @POST
        public HttpResponse doPost() {
            return HttpResponses.text("Got POST");
        }
    }

    /**
     * Tests the dispatching of WebMethod based on verb
     */
    public void testVerbMatch() throws Exception {
        WebClient wc = createWebClient();

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
        TextPage p = wc.getPage(new WebRequest(new URL(url, "verbMatch/"), m));
        assertEquals("Got " + m.name(), p.getContent());
    }


    //===================================================================


    public final ArbitraryWebMethodName arbitraryWebMethodName = new ArbitraryWebMethodName();

    public class ArbitraryWebMethodName {
        @WebMethod(name="")
        public HttpResponse notDoPrefix() {
            return HttpResponses.text("I'm index");
        }

        // this method is implicitly web method by its name
        public HttpResponse doTheNeedful() {
            return HttpResponses.text("DTN");
        }
    }


    public void testArbitraryWebMethodName() throws Exception {
        WebClient wc = createWebClient();
        TextPage p = wc.getPage(new URL(url, "arbitraryWebMethodName"));
        assertEquals("I'm index", p.getContent());

        p = wc.getPage(new URL(url, "arbitraryWebMethodName/theNeedful"));
        assertEquals("DTN", p.getContent());

    }


    //===================================================================


    public final InterceptorStage interceptorStage = new InterceptorStage();

    public class InterceptorStage {
        @POST
        public HttpResponse doFoo(@JsonBody Point body) {
            return HttpResponses.text(body.x + "," + body.y);
        }
    }

    public static class Point {
        public int x,y;
    }

    /**
     * POST annotation selection needs to happen before databinding of the parameter happens.
     */
    public void testInterceptorStage() throws Exception {
        WebClient wc = createWebClient();
        try {
            wc.getPage(new URL(url, "interceptorStage/foo"));
            fail("Expected 404");
        } catch (FailingHttpStatusCodeException e) {
            assertEquals(404, e.getStatusCode());
        }

        WebRequest req = new WebRequest(new URL(url, "interceptorStage/foo"), HttpMethod.POST);
        req.setAdditionalHeader("Content-Type","application/json");
        req.setRequestBody("{x:3,y:5}");
        TextPage p = wc.getPage(req);
        assertEquals("3,5",p.getContent());

    }

    public void testInterceptorStageContentTypeWithCharset() throws Exception {
        WebClient wc = createWebClient();
        try {
            wc.getPage(new URL(url, "interceptorStage/foo"));
            fail("Expected 404");
        } catch (FailingHttpStatusCodeException e) {
            assertEquals(404, e.getStatusCode());
        }

        WebRequest req = new WebRequest(new URL(url, "interceptorStage/foo"), HttpMethod.POST);
        req.setAdditionalHeader("Content-Type","application/json; charset=utf-8");
        req.setRequestBody("{x:3,y:5}");
        TextPage p = wc.getPage(req);
        assertEquals("3,5",p.getContent());

    }

    //===================================================================


    public final Inheritance inheritance = new Inheritance2();
    public class Inheritance {
        @WebMethod(name="foo")
        public HttpResponse doBar(@QueryParameter String q) {
            return HttpResponses.text("base");
        }
    }

    public class Inheritance2 extends Inheritance {
        @Override
        public HttpResponse doBar(String q) {
            return HttpResponses.text(q);
        }
    }

    public void testInheritance() throws Exception {
        WebClient wc = createWebClient();

        // the request should get to the overriding method and it should still see all the annotations in the base type
        TextPage p = wc.getPage(new URL(url, "inheritance/foo?q=abc"));
        assertEquals("abc", p.getContent());

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
            return HttpResponses.text("POST: " + IOUtils.toString(req.getInputStream(), StandardCharsets.UTF_8));
        }
    }

    public class PutInheritanceImpl extends PutInheritance{
        @Override
        public HttpResponse doBar(StaplerRequest req) throws IOException {
            return HttpResponses.text(IOUtils.toString(req.getInputStream(), StandardCharsets.UTF_8) + " World!");
        }
    }

    public void testPutInheritance() throws Exception {
        WebClient wc = createWebClient();

        // the request should get to the overriding method and it should still see all the annotations in the base type
        WebRequest wrs = new WebRequest(new URL(url, "putInheritance/foo"), HttpMethod.PUT);
        wrs.setRequestBody("Hello");
        TextPage p = wc.getPage(wrs);
        assertEquals("Hello World!", p.getContent());

        // doBar is a web method for 'foo', so bar endpoint shouldn't respond
        try {
            wc.getPage(new URL(url, "putInheritance/bar"));
            fail();
        } catch (FailingHttpStatusCodeException e) {
            assertEquals(404,e.getStatusCode());
        }

        //Invoke Post as well
        wrs = new WebRequest(new URL(url, "putInheritance/acme"), HttpMethod.POST);
        wrs.setRequestBody("Hello");
        p = wc.getPage(wrs);
        assertEquals("POST: Hello", p.getContent());
    }

    public void testInterfaceMethods() throws Exception {
        WebClient wc = createWebClient();
        try {
            wc.getPage(new URL(url, "usesInterfaceMethods/foo"));
            fail();
        } catch (FailingHttpStatusCodeException x) {
            assertEquals(HttpServletResponse.SC_METHOD_NOT_ALLOWED, x.getStatusCode());
        }
        assertEquals("default", wc.getPage(new WebRequest(new URL(url, "usesInterfaceMethods/foo"), HttpMethod.POST)).getWebResponse().getContentAsString().trim());
        try {
            wc.getPage(new URL(url, "overridesInterfaceMethods/foo"));
            fail();
        } catch (FailingHttpStatusCodeException x) {
            assertEquals(HttpServletResponse.SC_METHOD_NOT_ALLOWED, x.getStatusCode());
        }
        assertEquals("due to UnionAnnotatedElement it is even inherited", "overridden", wc.getPage(new WebRequest(new URL(url, "overridesInterfaceMethods/foo"), HttpMethod.POST)).getWebResponse().getContentAsString().trim());
    }
    public interface InterfaceWithWebMethods {
        @RequirePOST
        default HttpResponse doFoo() {
            return HttpResponses.text("default");
        }
    }
    public class UsesInterfaceMethods implements InterfaceWithWebMethods {}
    public class OverridesInterfaceMethods implements InterfaceWithWebMethods {
        @Override
        public HttpResponse doFoo() {
            return HttpResponses.text("overridden");
        }
    }
    public final UsesInterfaceMethods usesInterfaceMethods = new UsesInterfaceMethods();
    public final OverridesInterfaceMethods overridesInterfaceMethods = new OverridesInterfaceMethods();

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
        WebClient wc = createWebClient();
        URL url = new URL(this.url, "requirePostOnBase/something");

        try {
            wc.getPage(url);
            fail();
        } catch (FailingHttpStatusCodeException e) {
            assertEquals(HttpServletResponse.SC_METHOD_NOT_ALLOWED, e.getStatusCode());
        }

        // POST should succeed
        wc.getPage(new WebRequest(url, HttpMethod.POST));
        assertEquals(1, requirePostOnBase.hit);
    }

    public void testOverloads() throws Exception {
        TextPage p = createWebClient().getPage(new URL(url, "overloaded/x"));
        assertEquals("doX(StaplerRequest)", p.getContent());
    }
    public final Object overloaded = new Overloaded();
    public static class Overloaded {
        @Deprecated
        public HttpResponse doX() {
            return HttpResponses.text("doX()");
        }
        public HttpResponse doX(StaplerRequest req) {
            return HttpResponses.text("doX(StaplerRequest)");
        }
        public HttpResponse doX(StaplerResponse rsp) {
            return HttpResponses.text("doX(StaplerResponse)");
        }
        public HttpResponse doX(StaplerRequest req, StaplerResponse rsp) {
            return HttpResponses.text("doX(StaplerRequest, StaplerResponse)");
        }
        @WebMethod(name = "x")
        public HttpResponse x() {
            return HttpResponses.text("x()");
        }
    }

    public final TestWithPublicField testWithPublicField = new TestWithPublicField();

    public  class TestWithPublicField extends TestWithPublicFieldBase{

    }

    public  class TestWithPublicFieldBase{
        public TestClass testClass=new TestClass();
    }


    public  class TestClass{
//        @GET @WebMethod(name="")
        public String doValue(){
            return "hello";
        }
    }

    public void testPublicFieldDispatch() throws Exception {
        WebClient wc = createWebClient();
        URL url = new URL(this.url, "testWithPublicField/testClass/value/");

        try {
            wc.getPage(url);
        } catch (FailingHttpStatusCodeException e) {
            assertEquals(HttpServletResponse.SC_OK, e.getStatusCode());
        }
    }

    public void testProtectedMethodDispatch() throws Exception {
        WebClient wc = createWebClient();
        wc.getPage(new URL(url, "public/value"));
        try {
            wc.getPage(new URL(url, "protected/value"));
            fail("should not have allowed protected access");
        } catch (FailingHttpStatusCodeException x) {
            assertEquals(HttpServletResponse.SC_NOT_FOUND, x.getStatusCode());
        }
        try {
            wc.getPage(new URL(url, "private/value"));
            fail("should not have allowed private access");
        } catch (FailingHttpStatusCodeException x) {
            assertEquals(HttpServletResponse.SC_NOT_FOUND, x.getStatusCode());
        }
    }
    public TestClass getPublic() {return new TestClass();}
    protected TestClass getProtected() {return new TestClass();}
    private TestClass getPrivate() {return new TestClass();}

    //===================================================================

    public final StaplerProxyImpl staplerProxyOK = new StaplerProxyImpl(new IndexPage());
    public final StaplerProxyImpl staplerProxyFail = new StaplerProxyImpl(null);

    public class IndexPage {
        public void doIndex(StaplerResponse rsp) {
            throw HttpResponses.ok();
        }
    }

    public class StaplerProxyImpl implements StaplerProxy {
        private Object target;
        public int counter = 0;
        public StaplerProxyImpl(Object target) {
            this.target = target;
        }

        @Override
        public Object getTarget() {
            counter++;
            return target;
        }

        public void doIndex(StaplerResponse rsp) {
            if (target != this) {
                throw new IllegalStateException("should not be called");
            }
        }
    }


    public void testStaplerProxy() throws Exception {
        WebClient wc = createWebClient();
        Page p = wc.getPage(new URL(url, "staplerProxyOK"));
        assertEquals(200, p.getWebResponse().getStatusCode());

        try {
            p = wc.getPage(new URL(url, "staplerProxyFail"));
            fail("expected failure");
        } catch (FailingHttpStatusCodeException ex) {
            assertEquals(404, ex.getStatusCode());
        }
        assertTrue(staplerProxyOK.counter > 0);
        assertTrue(staplerProxyFail.counter > 0);
    }


}