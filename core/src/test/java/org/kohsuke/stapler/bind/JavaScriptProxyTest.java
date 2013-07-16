package org.kohsuke.stapler.bind;

import com.gargoylesoftware.htmlunit.AlertHandler;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.HttpResponses;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.WebApp;
import org.kohsuke.stapler.test.JettyTestCase;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;

/**
 * @author Kohsuke Kawaguchi
 */
public class JavaScriptProxyTest extends JettyTestCase {
    private String anonymousValue;
    private Object anonymous = new MyObject();
    
    /**
     * Exports an object and see if it can be reached.
     */
    public void testBind() throws Exception {
        final String[] msg = new String[1];

        // for interactive debugging
//        System.out.println(url);
//        System.in.read();

        WebClient wc = new WebClient();
        wc.setAlertHandler(new AlertHandler() {
            public void handleAlert(Page page, String message) {
                msg[0] = message;
            }
        });
        HtmlPage page = wc.getPage(new URL(url, "/"));

        page.executeJavaScript("v.foo(3,'test',callback);");
        assertEquals("string:test3",msg[0]);
        msg[0] = null;

        // test null unmarshalling and marshalling
        page.executeJavaScript("v.foo(0,null,callback);");
        assertEquals("object:null",msg[0]);
    }

    /**
     * Tests that an anonymous object can be bound.
     */
    public void testAnonymousBind() throws Exception {
        WebClient wc = new WebClient();
        HtmlPage page = wc.getPage(new URL(url, "/bindAnonymous"));
        page.executeJavaScript("v.xyz('hello');");
        assertEquals("hello",anonymousValue);
    }

    public String jsFoo(int x, String y) {
        if (x==0)   return y;
        return y+x;
    }
    
    public void doIndex(StaplerRequest req,StaplerResponse rsp) throws IOException {
        rsp.setContentType("text/html");
        String crumb = req.getWebApp().getCrumbIssuer().issueCrumb();
        PrintWriter w = rsp.getWriter();
        w.println("<html><body><script src='prototype'></script><script src='script'></script>");
        w.println("<script>var v = makeStaplerProxy('/','"+crumb+"',['foo','bar']);var callback = function(t){var x=t.responseObject();alert(typeof(x)+':'+x)};</script>");
        w.println("</body></html>");
    }

    public void doBindAnonymous(StaplerResponse rsp) throws IOException {
        rsp.setContentType("text/html");
        PrintWriter w = rsp.getWriter();
        w.println("<html><body><script src='prototype'></script><script src='script'></script>");
        w.println("<script>var v = "+ WebApp.getCurrent().boundObjectTable.bind(anonymous).getProxyScript()+";</script>");
        w.println("</body></html>");
    }

    public HttpResponse doPrototype() {
        return HttpResponses.staticResource(getClass().getResource("/org/kohsuke/stapler/framework/prototype/prototype.js"));
    }

    public HttpResponse doScript() {
        return HttpResponses.staticResource(getClass().getResource("/org/kohsuke/stapler/bind.js"));
    }

    public class MyObject {
        public void jsXyz(String s) {
            anonymousValue = s;
        }
    }
}
