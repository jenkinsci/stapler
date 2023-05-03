package org.kohsuke.stapler.json;

import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.WebResponse;
import java.net.URL;
import org.kohsuke.stapler.test.JettyTestCase;

public class JsonBodyTest extends JettyTestCase {

    public void testSmokes() throws Exception {
        WebRequest req = new WebRequest(new URL(url, "double"), HttpMethod.POST);
        req.setAdditionalHeader("Content-Type", "application/json");
        req.setRequestBody("{\"x\":10,\"y\":5}");
        WebResponse response = createWebClient().getPage(req).getWebResponse();
        assertEquals("application/json", response.getContentType());
        assertEquals("{\"x\":20,\"y\":10}", response.getContentAsString());
        // and then with compression:
        req.setAdditionalHeader("Accept-Encoding", "gzip");
        response = createWebClient().getPage(req).getWebResponse();
        assertEquals("application/json", response.getContentType());
        assertEquals("{\"x\":20,\"y\":10}", response.getContentAsString());
    }

    @JsonResponse
    public Point doDouble(@JsonBody Point p) {
        Point pt = new Point();
        pt.x = p.x * 2;
        pt.y = p.y * 2;
        return pt;
    }

    public static class Point {
        public int x, y;
    }

}
