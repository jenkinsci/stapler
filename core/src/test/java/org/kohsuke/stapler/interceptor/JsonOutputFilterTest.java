package org.kohsuke.stapler.interceptor;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import net.sf.json.JSONArray;
import net.sf.json.JSONSerializer;
import org.htmlunit.AlertHandler;
import org.htmlunit.Page;
import org.htmlunit.WebClient;
import org.htmlunit.html.HtmlPage;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.HttpResponses;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.WebApp;
import org.kohsuke.stapler.bind.JavaScriptMethod;
import org.kohsuke.stapler.test.JettyTestCase;

/**
 * Tests {@link JsonOutputFilter}.
 *
 * @author Robert Sandell &lt;sandell.robert@gmail.com&gt;
 */
public class JsonOutputFilterTest extends JettyTestCase {

    public void testExclude() throws Exception {
        final String[] msg = new String[1];
        WebClient wc = createWebClient();
        wc.setAlertHandler(new AlertHandler() {
            @Override
            public void handleAlert(Page page, String message) {
                msg[0] = message;
            }
        });
        HtmlPage page = wc.getPage(new URL(url, "/"));

        page.executeJavaScript("v.getSomeExcludedData(callback);");
        wc.getJavaScriptEngine().processPostponedActions();
        wc.waitForBackgroundJavaScript(10000);

        Map json = (Map) JSONSerializer.toJSON(msg[0]);
        assertTrue(json.containsKey("name"));
        assertTrue(json.containsKey("description"));
        assertFalse(json.containsKey("secret"));
    }

    public void testInclude() throws Exception {
        final String[] msg = new String[1];
        WebClient wc = createWebClient();
        wc.setAlertHandler(new AlertHandler() {
            @Override
            public void handleAlert(Page page, String message) {
                msg[0] = message;
            }
        });
        HtmlPage page = wc.getPage(new URL(url, "/"));

        page.executeJavaScript("v.getSomeIncludedData(callback);");
        wc.getJavaScriptEngine().processPostponedActions();
        wc.waitForBackgroundJavaScript(10000);

        Map json = (Map) JSONSerializer.toJSON(msg[0]);
        assertTrue(json.containsKey("name"));
        assertFalse(json.containsKey("description"));
        assertFalse(json.containsKey("secret"));
    }

    public void testExcludeList() throws Exception {
        final String[] msg = new String[1];
        WebClient wc = createWebClient();
        wc.setAlertHandler(new AlertHandler() {
            @Override
            public void handleAlert(Page page, String message) {
                msg[0] = message;
            }
        });
        HtmlPage page = wc.getPage(new URL(url, "/"));

        page.executeJavaScript("v.getSomeExcludedList(callback);");
        wc.getJavaScriptEngine().processPostponedActions();
        wc.waitForBackgroundJavaScript(10000);

        JSONArray json = (JSONArray) JSONSerializer.toJSON(msg[0]);
        assertEquals(3, json.size());
        for (Object o : json) {
            Map map = (Map) o;
            assertTrue(map.containsKey("name"));
            assertTrue(map.containsKey("description"));
            assertFalse(map.containsKey("secret"));
        }
    }

    @JsonOutputFilter(excludes = {"secret"})
    @JavaScriptMethod
    public MyData getSomeExcludedData() {
        return new MyData("Bob", "the builder", "super secret value");
    }

    @JsonOutputFilter(excludes = {"secret"})
    @JavaScriptMethod
    public List<MyData> getSomeExcludedList() {
        return Arrays.asList(
                new MyData("Bob", "the builder", "super secret value"),
                new MyData("Lisa", "the coder", "even more super secret"),
                new MyData("Jenkins", "the butler", "really secret as well"));
    }

    @JsonOutputFilter(includes = {"name"})
    @JavaScriptMethod
    public MyData getSomeIncludedData() {
        return new MyData("Bob", "the builder", "super secret value");
    }

    public void doIndex(StaplerResponse rsp) throws IOException {
        rsp.setContentType("text/html");
        PrintWriter w = rsp.getWriter();
        w.println("<html><body><script src='script'></script>");
        w.println("<script>var v = "
                + WebApp.getCurrent().boundObjectTable.bind(this).getProxyScript() + ";</script>");
        w.println("<script>var callback = function(t){var x=t.responseObject(); alert(JSON.stringify(x)); };</script>");
        w.println("</body></html>");
    }

    public HttpResponse doScript() {
        return HttpResponses.staticResource(getClass().getResource("/org/kohsuke/stapler/bind.js"));
    }

    public static class MyData {
        private String name;
        private String description;
        private String secret;

        public MyData(String name, String description, String secret) {
            this.name = name;
            this.description = description;
            this.secret = secret;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getSecret() {
            return secret;
        }

        public void setSecret(String secret) {
            this.secret = secret;
        }
    }
}
