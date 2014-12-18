package org.kohsuke.stapler.compression;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.io.IOUtils;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.test.JettyTestCase;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.servlet.Context;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class CompressionFilterTest extends JettyTestCase {

    @Override
    protected void configure(Context context) {
        super.configure(context);
        context.addFilter(CompressionFilter.class,"/*", Handler.DEFAULT);
    }

    public void testDoubleCompression() throws Exception {
        for (String endpoint : Arrays.asList("autoZip","ownZip")) {
            HttpURLConnection con = (HttpURLConnection) new URL(this.url, endpoint).openConnection();
            con.setRequestProperty("Accept-Encoding","gzip");
            byte[] data = IOUtils.toByteArray(con.getInputStream());
            data = IOUtils.toByteArray(new GZIPInputStream(new ByteArrayInputStream(data)));
            assertEquals(new String(data), CONTENT);
        }
    }

    /**
     * Turns out {@link #testDoubleCompression()} was insufficient to properly test the case,
     * as HttpURLConnection appears to ignores the content-length header and read the response to the end.
     */
    public void testDoubleCompression2() throws Exception {
        for (String endpoint : Arrays.asList("autoZip","ownZip")) {
            HttpClient hc = new HttpClient();
            HttpMethod m = new GetMethod(this.url + "/"+endpoint);
            m.setRequestHeader("Accept-Encoding", "gzip");
            assertEquals(200, hc.executeMethod(m));
            byte[] data = m.getResponseBody();

            data = IOUtils.toByteArray(new GZIPInputStream(new ByteArrayInputStream(data)));
            assertEquals(new String(data), CONTENT);
        }
    }

    /**
     * Simulate servlets that tries to handle Content-Encoding on its own
     */
    public void doOwnZip(StaplerResponse rsp) throws IOException {
        rsp.setHeader("Content-Encoding", "gzip");
        byte[] content = CONTENT.getBytes();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        GZIPOutputStream o = new GZIPOutputStream(baos);
        o.write(content);
        o.close();

        rsp.setContentLength(baos.size());
        rsp.getOutputStream().write(baos.toByteArray());
    }

    public void doAutoZip(StaplerRequest req, StaplerResponse rsp) throws IOException {
        byte[] content = CONTENT.getBytes();
        rsp.getCompressedOutputStream(req).write(content);
    }


    private static final String CONTENT = "Hello World";
}