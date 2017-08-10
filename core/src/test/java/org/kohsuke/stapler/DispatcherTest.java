package org.kohsuke.stapler;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.TextPage;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequestSettings;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Locale;
import javax.net.ssl.HttpsURLConnection;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.IOUtils;
import org.kohsuke.stapler.annotations.StaplerCONNECT;
import org.kohsuke.stapler.annotations.StaplerContent;
import org.kohsuke.stapler.annotations.StaplerDELETE;
import org.kohsuke.stapler.annotations.StaplerGET;
import org.kohsuke.stapler.annotations.StaplerHEAD;
import org.kohsuke.stapler.annotations.StaplerMethod;
import org.kohsuke.stapler.annotations.StaplerMethods;
import org.kohsuke.stapler.annotations.StaplerObject;
import org.kohsuke.stapler.annotations.StaplerPATCH;
import org.kohsuke.stapler.annotations.StaplerPOST;
import org.kohsuke.stapler.annotations.StaplerPUT;
import org.kohsuke.stapler.annotations.StaplerPath;
import org.kohsuke.stapler.annotations.StaplerPaths;
import org.kohsuke.stapler.interceptor.RequirePOST;
import org.kohsuke.stapler.json.JsonBody;
import org.kohsuke.stapler.test.JettyTestCase;
import org.kohsuke.stapler.verb.GET;
import org.kohsuke.stapler.verb.POST;
import org.kohsuke.stapler.verb.PUT;

/**
 * @author Kohsuke Kawaguchi
 */
public class DispatcherTest extends JettyTestCase {

    public final IndexDispatchByName indexDispatchByName = new IndexDispatchByName();

    public class IndexDispatchByName {
        @WebMethod(name = "")
        public HttpResponse doHelloWorld() {
            return HttpResponses.text("Hello world");
        }
    }

    /**
     * Makes sure @WebMethod(name="") has the intended effect of occupying the root of the object in the URL space.
     */
    public void testIndexDispatchByName() throws Exception {
        WebClient wc = new WebClient();
        TextPage p = wc.getPage(new URL(url, "indexDispatchByName"));
        assertEquals("Hello world", p.getContent());
    }


    //===================================================================


    public final VerbMatch verbMatch = new VerbMatch();

    public class VerbMatch {
        @WebMethod(name = "")
        @GET
        public HttpResponse doGet() {
            return HttpResponses.text("Got GET");
        }

        @WebMethod(name = "")
        @POST
        public HttpResponse doPost() {
            return HttpResponses.text("Got POST");
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
        assertEquals("Got " + m.name(), p.getContent());
    }


    //===================================================================

    public final ContentMatch contentMatch = new ContentMatch();

    @StaplerObject
    public class ContentMatch {

        @StaplerPaths({@StaplerPath(StaplerPath.INDEX), @StaplerPath("text")})
        @StaplerPOST
        @StaplerContent(StaplerContent.ANY_TEXT)
        public HttpResponse doText() {
            return HttpResponses.text("I got text");
        }

        @StaplerPaths({@StaplerPath(StaplerPath.INDEX), @StaplerPath("xml")})
        @StaplerPOST
        @StaplerContent("*/xml")
        public HttpResponse doXml() {
            return HttpResponses.text("I got xml");
        }

        @StaplerPaths({@StaplerPath(StaplerPath.INDEX), @StaplerPath("json")})
        @StaplerPOST
        @StaplerContent("application/json")
        public HttpResponse doJson() {
            return HttpResponses.text("I got json");
        }
    }

    /**
     * Tests the dispatching of WebMethod based on verb
     */
    public void testContentMatch() throws Exception {
        post("text/plain", "text", "I got text");
        post("text/html", "text", "I got text");
        postFail("application/xml", "text");
        post("application/xml", "xml", "I got xml");
        post("text/xml", "xml", "I got xml");
        postFail("text/html", "xml");
        post("application/json", "json", "I got json");
        postFail("text/html", "json");

        // FIXME uncomment once StaplerPath support added
        post("text/plain", "", "I got text");
        post("text/html", "", "I got text");
        post("application/xml", "", "I got xml");
        // post("text/xml", "", "I got xml"); // FIXME uncomment once matching conflict resolution added
        post("application/json", "", "I got json");

    }

    private void post(String contentType, String path, String expectedResponse) throws java.io.IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(url, "contentMatch/" + path).openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", contentType);
        connection.setDoOutput(true);
        try {
            connection.connect();
            try (OutputStream os = connection.getOutputStream()) {
                os.write("Dummy".getBytes(StandardCharsets.UTF_8));
            }
            assertEquals("Content-Type " + contentType + " on path " + path, 200, connection.getResponseCode());
            ByteArrayOutputStream in = new ByteArrayOutputStream();
            try (InputStream is = connection.getInputStream()) {
                IOUtils.copy(is, in);
            }
            assertEquals("Content-Type " + contentType + " on path " + path, expectedResponse,
                    new String(in.toByteArray(), StandardCharsets.UTF_8));
        } finally {
            connection.disconnect();
        }
    }

    private void postFail(String contentType, String path) throws java.io.IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(url, "contentMatch/" + path).openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", contentType);
        connection.setDoOutput(true);
        try {
            connection.connect();
            try (OutputStream os = connection.getOutputStream()) {
                os.write("Dummy".getBytes(StandardCharsets.UTF_8));
            }
            assertEquals("Content-Type " + contentType + " on path " + path, 404, connection.getResponseCode());
        } finally {
            connection.disconnect();
        }
    }
    //===================================================================

    public final MethodMatch methodMatch = new MethodMatch();

    public class MethodMatch {

        private HttpResponse response(final String probe) {
            return new HttpResponse() {
                @Override
                public void generateResponse(StaplerRequest req, StaplerResponse rsp, Object node)
                        throws IOException, ServletException {
                    rsp.addHeader("X-Probe", probe);
                    rsp.setStatus(200);
                }
            };
        }

        @StaplerPaths({@StaplerPath(StaplerPath.INDEX), @StaplerPath("connect")})
        @StaplerCONNECT
        public HttpResponse doConnect() {
            return response("connect");
        }

        @StaplerPaths({@StaplerPath(StaplerPath.INDEX), @StaplerPath("delete")})
        @StaplerDELETE
        public HttpResponse doDelete() {
            return response("delete");
        }

        @StaplerPaths({@StaplerPath(StaplerPath.INDEX), @StaplerPath("get")})
        @StaplerGET
        public HttpResponse doGet() {
            return response("get");
        }

        @StaplerPaths({@StaplerPath(StaplerPath.INDEX), @StaplerPath("head")})
        @StaplerHEAD
        public HttpResponse doHead() {
            return response("head");
        }

        @StaplerPaths({@StaplerPath(StaplerPath.INDEX), @StaplerPath("patch")})
        @StaplerPATCH
        public HttpResponse doPatch() {
            return response("patch");
        }

        @StaplerPaths({@StaplerPath(StaplerPath.INDEX), @StaplerPath("post")})
        @StaplerPOST
        public HttpResponse doPost() {
            return response("post");
        }

        @StaplerPaths({@StaplerPath(StaplerPath.INDEX), @StaplerPath("put")})
        @StaplerPUT
        public HttpResponse doPut() {
            return response("put");
        }

        @StaplerPaths({@StaplerPath(StaplerPath.INDEX), @StaplerPath("custom")})
        @StaplerMethod("CUSTOM")
        public HttpResponse doCustom() {
            return response("custom");
        }

        @StaplerMethods({
                                @StaplerMethod("CONNECT"), @StaplerMethod("DELETE"), @StaplerMethod("GET"),
                                @StaplerMethod("HEAD"), @StaplerMethod("PATCH"), @StaplerMethod("POST"),
                                @StaplerMethod("PUT"), @StaplerMethod("CUSTOM")
                        })
        public HttpResponse doAll() {
            return response(Stapler.getCurrentRequest().getMethod().toLowerCase(Locale.ENGLISH));
        }
    }

    /**
     * Tests the dispatching of StaplerPath based on method
     */
    public void testMethodMatch() throws Exception {
        String[] methods = {"CONNECT", "DELETE", "GET", "HEAD", "PATCH", "POST", "PUT", "CUSTOM"};
        for (String method1 : methods) {
            check(method1, "all", 200);
            // check(method1, "", 200); // FIXME uncomment once StaplerPath support added
            for (String method2 : methods) {
                check(method1, method2.toLowerCase(), method1.equals(method2) ? 200 : 404);
            }
        }
    }

    private void check(String m, String path, int expectedStatus) throws java.io.IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(url, "methodMatch/" + path).openConnection();
        setRequestMethodViaJreBugWorkaround(connection, m);
        try {
            connection.connect();
            assertEquals("Method " + m + " on path " + path, expectedStatus, connection.getResponseCode());
            if (expectedStatus == 200) {
                assertEquals("Method " + m + " on path " + path, m.toLowerCase(Locale.ENGLISH),
                        connection.getHeaderField("X-Probe"));
            } else {
                assertNull("Method " + m + " on path " + path, connection.getHeaderField("X-Probe"));
            }
        } finally {
            connection.disconnect();
        }
    }


    /**
     * Workaround for a bug in {@code HttpURLConnection.setRequestMethod(String)}
     * The implementation of Sun/Oracle is throwing a {@code ProtocolException}
     * when the method is other than the HTTP/1.1 default methods. So to use {@code PROPFIND}
     * and others, we must apply this workaround.
     */
    private static void setRequestMethodViaJreBugWorkaround(final HttpURLConnection httpURLConnection,
                                                            final String method) {
        try {
            httpURLConnection.setRequestMethod(method); // Check whether we are running on a buggy JRE
        } catch (final ProtocolException pe) {
            try {
                AccessController.doPrivileged(new PrivilegedExceptionAction<Object>() {
                    @Override
                    public Object run() throws NoSuchFieldException, IllegalAccessException {
                        final Object target;
                        if (httpURLConnection instanceof HttpsURLConnection) {
                            final Field delegate = httpURLConnection.getClass().getDeclaredField("delegate");
                            delegate.setAccessible(true);
                            target = delegate.get(httpURLConnection);
                        } else {
                            target = httpURLConnection;
                        }
                        final Field methodField = HttpURLConnection.class.getDeclaredField("method");
                        methodField.setAccessible(true);
                        methodField.set(target, method);
                        return null;
                    }
                });
            } catch (final PrivilegedActionException e) {
                final Throwable cause = e.getCause();
                if (cause instanceof RuntimeException) {
                    throw (RuntimeException) cause;
                } else {
                    throw new RuntimeException(cause);
                }
            }
        }
    }

    //===================================================================


    public final ArbitraryWebMethodName arbitraryWebMethodName = new ArbitraryWebMethodName();

    public class ArbitraryWebMethodName {
        @WebMethod(name = "")
        public HttpResponse notDoPrefix() {
            return HttpResponses.text("I'm index");
        }

        // this method is implicitly web method by its name
        public HttpResponse doTheNeedful() {
            return HttpResponses.text("DTN");
        }
    }


    public void testArbitraryWebMethodName() throws Exception {
        WebClient wc = new WebClient();
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
        public int x, y;
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
        req.setAdditionalHeader("Content-Type", "application/json");
        req.setRequestBody("{x:3,y:5}");
        TextPage p = wc.getPage(req);
        assertEquals("3,5", p.getContent());

    }

    public void testInterceptorStageContentTypeWithCharset() throws Exception {
        WebClient wc = new WebClient();
        try {
            wc.getPage(new URL(url, "interceptorStage/foo"));
            fail("Expected 404");
        } catch (FailingHttpStatusCodeException e) {
            assertEquals(404, e.getStatusCode());
        }

        WebRequestSettings req = new WebRequestSettings(new URL(url, "interceptorStage/foo"), HttpMethod.POST);
        req.setAdditionalHeader("Content-Type", "application/json; charset=utf-8");
        req.setRequestBody("{x:3,y:5}");
        TextPage p = wc.getPage(req);
        assertEquals("3,5", p.getContent());

    }

    //===================================================================


    public final Inheritance inheritance = new Inheritance2();

    public class Inheritance {
        @WebMethod(name = "foo")
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
        WebClient wc = new WebClient();

        // the request should get to the overriding method and it should still see all the annotations in the base type
        TextPage p = wc.getPage(new URL(url, "inheritance/foo?q=abc"));
        assertEquals("abc", p.getContent());

        // doBar is a web method for 'foo', so bar endpoint shouldn't respond
        try {
            wc.getPage(new URL(url, "inheritance/bar"));
            fail();
        } catch (FailingHttpStatusCodeException e) {
            assertEquals(404, e.getStatusCode());
        }
    }

    public final PutInheritance putInheritance = new PutInheritanceImpl();

    public abstract class PutInheritance {
        @WebMethod(name = "foo")
        @PUT
        public abstract HttpResponse doBar(StaplerRequest req) throws IOException;

        @POST
        public HttpResponse doAcme(StaplerRequest req) throws IOException {
            return HttpResponses.text("POST: " + IOUtils.toString(req.getInputStream()));
        }
    }

    public class PutInheritanceImpl extends PutInheritance {
        @Override
        public HttpResponse doBar(StaplerRequest req) throws IOException {
            return HttpResponses.text(IOUtils.toString(req.getInputStream()) + " World!");
        }
    }

    public void testPutInheritance() throws Exception {
        WebClient wc = new WebClient();

        // the request should get to the overriding method and it should still see all the annotations in the base type
        WebRequestSettings wrs = new WebRequestSettings(new URL(url, "putInheritance/foo"), HttpMethod.PUT);
        wrs.setRequestBody("Hello");
        TextPage p = wc.getPage(wrs);
        assertEquals("Hello World!", p.getContent());

        // doBar is a web method for 'foo', so bar endpoint shouldn't respond
        try {
            wc.getPage(new URL(url, "putInheritance/bar"));
            fail();
        } catch (FailingHttpStatusCodeException e) {
            assertEquals(404, e.getStatusCode());
        }

        //Invoke Post as well
        wrs = new WebRequestSettings(new URL(url, "putInheritance/acme"), HttpMethod.POST);
        wrs.setRequestBody("Hello");
        p = wc.getPage(wrs);
        assertEquals("POST: Hello", p.getContent());
    }

    public class AnnotationInheritance {
        public final HttpResponse doMagicFinal(@QueryParameter String q) {
            return HttpResponses.text("root: " + q);
        }

        @StaplerGET
        @StaplerPath("annotated-final")
        public final HttpResponse doAnnotatedFinal(@QueryParameter String q) {
            return HttpResponses.text("root: " + q);
        }

        public HttpResponse doMagicBase(@QueryParameter String q) {
            return HttpResponses.text("root: " + q);
        }

        @StaplerGET
        @StaplerPath("annotated-base")
        public HttpResponse doAnnotatedBase(@QueryParameter String q) {
            return HttpResponses.text("root: " + q);
        }

        public HttpResponse doMagicBaseOverrideAnnotated(@QueryParameter String q) {
            return HttpResponses.text("root: " + q);
        }

        @StaplerGET
        @StaplerPath("annotated-base-override-annotated")
        public HttpResponse doAnnotatedBaseOverrideAnnotated(@QueryParameter String q) {
            return HttpResponses.text("root: " + q);
        }

        public HttpResponse doMagicBaseOverrideNoAnnotation(@QueryParameter String q) {
            return HttpResponses.text("root: " + q);
        }

        @StaplerGET
        @StaplerPath("annotated-base-override-no-annotation")
        public HttpResponse doAnnotatedBaseOverrideNoAnnotation(@QueryParameter String q) {
            return HttpResponses.text("root: " + q);
        }
    }

    @StaplerObject
    public class AnnotationInheritance2 extends AnnotationInheritance {

        @StaplerGET
        public HttpResponse doMagicBaseOverrideAnnotated(@QueryParameter String q) {
            return HttpResponses.text("child: " + q);
        }

        @StaplerGET
        @StaplerPath("annotated-base-override-annotated")
        public HttpResponse doAnnotatedBaseOverrideAnnotated(@QueryParameter String q) {
            return HttpResponses.text("child: " + q);
        }

        public HttpResponse doMagicBaseOverrideNoAnnotation(String q) {
            return HttpResponses.text("child: " + q);
        }

        public HttpResponse doAnnotatedBaseOverrideNoAnnotation(String q) {
            return HttpResponses.text("child: " + q);
        }


        public final HttpResponse doMagicFinal2(@QueryParameter String q) {
            return HttpResponses.text("child: " + q);
        }

        @StaplerGET
        @StaplerPath("annotated-final2")
        public final HttpResponse doAnnotatedFinal2(@QueryParameter String q) {
            return HttpResponses.text("child: " + q);
        }

        public HttpResponse doMagicBase2(@QueryParameter String q) {
            return HttpResponses.text("child: " + q);
        }

        @StaplerGET
        @StaplerPath("annotated-base2")
        public HttpResponse doAnnotatedBase2(@QueryParameter String q) {
            return HttpResponses.text("child: " + q);
        }

        public HttpResponse doMagicBaseOverrideAnnotated2(@QueryParameter String q) {
            return HttpResponses.text("child: " + q);
        }

        @StaplerGET
        @StaplerPath("annotated-base-override-annotated2")
        public HttpResponse doAnnotatedBaseOverrideAnnotated2(@QueryParameter String q) {
            return HttpResponses.text("child: " + q);
        }

        public HttpResponse doMagicBaseOverrideNoAnnotation2(@QueryParameter String q) {
            return HttpResponses.text("child: " + q);
        }

        @StaplerGET
        @StaplerPath("annotated-base-override-no-annotation2")
        public HttpResponse doAnnotatedBaseOverrideNoAnnotation2(@QueryParameter String q) {
            return HttpResponses.text("child: " + q);
        }
    }

    public class AnnotationInheritance3 extends AnnotationInheritance2 {

        @StaplerGET
        public HttpResponse doMagicBaseOverrideAnnotated(@QueryParameter String q) {
            return HttpResponses.text("leaf: " + q);
        }

        @StaplerGET
        public HttpResponse doAnnotatedBaseOverrideAnnotated(@QueryParameter String q) {
            return HttpResponses.text("leaf: " + q);
        }

        public HttpResponse doMagicBaseOverrideNoAnnotation(String q) {
            return HttpResponses.text("leaf: " + q);
        }

        public HttpResponse doAnnotatedBaseOverrideNoAnnotation(String q) {
            return HttpResponses.text("leaf: " + q);
        }

        @StaplerGET
        public HttpResponse doMagicBaseOverrideAnnotated2(@QueryParameter String q) {
            return HttpResponses.text("leaf: " + q);
        }

        @StaplerGET
        public HttpResponse doAnnotatedBaseOverrideAnnotated2(@QueryParameter String q) {
            return HttpResponses.text("leaf: " + q);
        }

        public HttpResponse doMagicBaseOverrideNoAnnotation2(String q) {
            return HttpResponses.text("leaf: " + q);
        }

        public HttpResponse doAnnotatedBaseOverrideNoAnnotation2(String q) {
            return HttpResponses.text("leaf: " + q);
        }
    }

    public final AnnotationInheritance annotationInheritanceA = new AnnotationInheritance2();
    public final AnnotationInheritance annotationInheritanceB = new AnnotationInheritance3();

    public void testGiven__staplerObject_extends_magic__when__finalMethodInBaseClass__then__magicUsed()
            throws Exception {
        assertFound("annotationInheritanceA/magicFinal?q=123", "root: 123");
    }

    public void testGiven__staplerObject_extends_magic__when__annotatedFinalMethodInBaseClass__then__annotationUsed()
            throws Exception {
        assertFound("annotationInheritanceA/annotated-final?q=123", "root: 123");
    }

    public void testGiven__staplerObject_extends_magic__when__methodImplInBaseClass__then__magicUsed()
            throws Exception {
        assertFound("annotationInheritanceA/magicBase?q=123", "root: 123");
    }

    public void testGiven__staplerObject_extends_magic__when__annotatedMethodImplInBaseClass__then__annotationUsed()
            throws Exception {
        assertFound("annotationInheritanceA/annotated-base?q=123", "root: 123");
    }

    public void testGiven__staplerObject_extends_magic__when__magicInAnnotatedClass__then__404()
            throws Exception {
        assertNotFound("annotationInheritanceA/magicBaseOverrideAnnotated2?q=123");
    }

    public void testGiven__staplerObject_extends_magic__when__annotationsInAnnotatedClass__then__annotationUsed()
            throws Exception {
        assertFound("annotationInheritanceA/annotated-base-override-annotated2?q=123", "child: 123");
    }

    public void testGiven__staplerObject_extends_magic__when__magicInAnnotatedClass2__then__404()
            throws Exception {
        assertNotFound("annotationInheritanceA/magicBaseOverrideNoAnnotation2?q=123");
    }

    public void testGiven__staplerObject_extends_magic__when__annotationsInAnnotatedClass2__then__annotationUsed()
            throws Exception {
        assertFound("annotationInheritanceA/annotated-base-override-no-annotation2?q=123", "child: 123");
    }

    public void testGiven__staplerObject_extends_magic__when__magicFinalMethodInAnnotatedClass__then__404()
            throws Exception {
        assertNotFound("annotationInheritanceA/magicFinal2?q=123");
    }

    public void testGiven__staplerObject_extends_magic__when__annotatedFinalMethodInAnnotatedClass__then__annotationUsed()
            throws Exception {
        assertFound("annotationInheritanceA/annotated-final2?q=123", "child: 123");
    }

    public void testGiven__staplerObject_extends_magic__when__methodImplInAnnotatedClass__then_404()
            throws Exception {
        assertNotFound("annotationInheritanceA/magicBase2?q=123");
    }

    public void testGiven__staplerObject_extends_magic__when__annotatedMethodImplInAnnotatedClass__then__annotationUsed()
            throws Exception {
        assertFound("annotationInheritanceA/annotated-base2?q=123", "child: 123");
    }

    public void testGiven__staplerObject_extends_magic__when__overrideMagicWithAnnotations__then__annotationUsed()
            throws Exception {
        assertFound("annotationInheritanceA/magicBaseOverrideAnnotated?q=123", "child: 123");
    }

    public void testGiven__staplerObject_extends_magic__when__overrideAnnotationsWithAnnotations__then__annotationUsed()
            throws Exception {
        assertFound("annotationInheritanceA/annotated-base-override-annotated?q=123", "child: 123");
    }

    public void testGiven__staplerObject_extends_magic__when__overrideMagicWithoutAnnotations__then__404()
            throws Exception {
        assertNotFound("annotationInheritanceA/magicBaseOverrideNoAnnotation?q=123");
    }

    public void testGiven__staplerObject_extends_magic__when__overrideAnnotationsWithoutAnnotations__then__404()
            throws Exception {
        assertNotFound("annotationInheritanceA/annotated-base-override-no-annotation?q=123");
    }

    public void testGiven__magic_extends_staplerObject_extends_magic__when__finalMethodInBaseClass__then__magicUsed()
            throws Exception {
        assertFound("annotationInheritanceB/magicFinal?q=123", "root: 123");
    }

    public void testGiven__magic_extends_staplerObject_extends_magic__when__annotatedFinalMethodInBaseClass__then__annotationUsed()
            throws Exception {
        assertFound("annotationInheritanceB/annotated-final?q=123", "root: 123");
    }

    public void testGiven__magic_extends_staplerObject_extends_magic__when__methodImplInBaseClass__then__magicUsed()
            throws Exception {
        assertFound("annotationInheritanceB/magicBase?q=123", "root: 123");
    }

    public void testGiven__magic_extends_staplerObject_extends_magic__when__annotatedMethodImplInBaseClass__then__annotationUsed()
            throws Exception {
        assertFound("annotationInheritanceB/annotated-base?q=123", "root: 123");
    }

    public void testGiven__magic_extends_staplerObject_extends_magic__when__overrideMagicWithAnnotations__then__annotationUsed()
            throws Exception {
        assertFound("annotationInheritanceB/magicBaseOverrideAnnotated?q=123", "leaf: 123");
    }

    public void testGiven__magic_extends_staplerObject_extends_magic__when__overrideAnnotationsWithAnnotations__then__annotationUsed()
            throws Exception {
        assertFound("annotationInheritanceB/annotated-base-override-annotated?q=123", "leaf: 123");
    }

    public void testGiven__magic_extends_staplerObject_extends_magic__when__overrideMagicWithoutAnnotations__then__404()
            throws Exception {
        assertFound("annotationInheritanceB/magicBaseOverrideNoAnnotation?q=123", "leaf: 123");
    }

    public void testGiven__magic_extends_staplerObject_extends_magic__when__overrideAnnotationsWithoutAnnotations2__then__annotationUsed()
            throws Exception {
        assertFound("annotationInheritanceB/annotated-base-override-no-annotation?q=123", "leaf: 123");
    }

    public void testGiven__magic_extends_staplerObject_extends_magic__when__magicFinalMethodInAnnotatedClass__then__404()
            throws Exception {
        assertNotFound("annotationInheritanceB/magicFinal2?q=123");
    }

    public void testGiven__magic_extends_staplerObject_extends_magic__when__annotatedFinalMethodInAnnotatedClass__then__annotationUsed()
            throws Exception {
        assertFound("annotationInheritanceB/annotated-final2?q=123", "child: 123");
    }

    public void testGiven__magic_extends_staplerObject_extends_magic__when__magicMethodImplInAnnotatedClass__then__404()
            throws Exception {
        assertNotFound("annotationInheritanceB/magicBase2?q=123");
    }

    public void testGiven__magic_extends_staplerObject_extends_magic__when__annotatedMethodImplInAnnotatedClass__then__annotationUsed()
            throws Exception {
        assertFound("annotationInheritanceB/annotated-base2?q=123", "child: 123");
    }

    public void testGiven__magic_extends_staplerObject_extends_magic__when__overrideWithAnnotations1__then__annotationUsed()
            throws Exception {
        assertFound("annotationInheritanceB/magicBaseOverrideAnnotated2?q=123", "leaf: 123");
    }

    public void testGiven__magic_extends_staplerObject_extends_magic__when__overrideWithAnnotations2__then__annotationUsed()
            throws Exception {
        assertFound("annotationInheritanceB/annotated-base-override-annotated2?q=123", "leaf: 123");
    }

    public void testGiven__magic_extends_staplerObject_extends_magic__when__overrideMagicWithoutAnnotations__then__magicUsed()
            throws Exception {
        assertFound("annotationInheritanceB/magicBaseOverrideNoAnnotation2?q=123", "leaf: 123");
    }

    public void testGiven__magic_extends_staplerObject_extends_magic__when__overrideAnnotationsWithoutAnnotations__then__annotationUsed()
            throws Exception {
        assertFound("annotationInheritanceB/annotated-base-override-no-annotation2?q=123", "leaf: 123");
    }

    private void assertFound(String relativeUrl, String expectedContent) throws IOException {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url, relativeUrl).openConnection();
            int status = connection.getResponseCode();
            if (status/100 == 2) {
                String result;
                try (InputStream is = connection.getInputStream()) {
                    result = IOUtils.toString(is);
                } finally {
                    connection.disconnect();
                }
                assertEquals(expectedContent, result);
            } else {
                String result = "";
                try (InputStream is = connection.getInputStream()) {
                    result = IOUtils.toString(is);
                } catch (FileNotFoundException e) {
                    // ignore
                }
                fail("Expected HTTP/20x at " + relativeUrl + ", got HTTP/" + status + "\n" + result);
            }
        } catch (FileNotFoundException e) {
            fail("Expected HTTP/20x at " + relativeUrl);
        }
    }

    private void assertNotFound(String relativeUrl) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(url, relativeUrl).openConnection();
        try {
            int status = connection.getResponseCode();
            if (status == 404) {
                return;
            }
            String result = "";
            try (InputStream is = connection.getInputStream()) {
                result = IOUtils.toString(is);
            } catch (FileNotFoundException e) {
                // ignore
            }
            fail("Expected HTTP/404 at " + relativeUrl + ", got HTTP/" + status + "\n"+result);
        } finally {
            connection.disconnect();
        }
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

    public void testOverloads() throws Exception {
        TextPage p = new WebClient().getPage(new URL(url, "overloaded/x"));
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

    public class TestWithPublicField extends TestWithPublicFieldBase {

    }

    public class TestWithPublicFieldBase {
        public TestClass testClass = new TestClass();
    }


    public class TestClass {
        //        @GET @WebMethod(name="")
        public String doValue() {
            return "hello";
        }
    }

    public void testPublicFieldDispatch() throws Exception {
        WebClient wc = new WebClient();
        URL url = new URL(this.url, "testWithPublicField/testClass/value/");

        try {
            wc.getPage(url);
        } catch (FailingHttpStatusCodeException e) {
            assertEquals(HttpServletResponse.SC_OK, e.getStatusCode());
        }
    }


}
