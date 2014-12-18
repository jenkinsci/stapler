package org.kohsuke.stapler.compression;

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
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

public class CompressionFilterTest extends JettyTestCase {

    @Override
    protected void configure(Context context) {
        super.configure(context);
        context.addFilter(CompressionFilter.class,"/*", Handler.DEFAULT);
    }

    public void testDoubleCompression() throws Exception {
        HttpURLConnection con = (HttpURLConnection) new URL(this.url, "doubleGzip").openConnection();
        con.setRequestProperty("Accept-Encoding","gzip");
        byte[] data = IOUtils.toByteArray(con.getInputStream());
        data = IOUtils.toByteArray(new GZIPInputStream(new ByteArrayInputStream(data)));
        assertEquals(new String(data),CONTENT);
    }

    /**
     * Code that we run inside CompressionFilter might try to do its own gzip encoding,
     * and if so, we need to work correctly.
     */
    public void doDoubleGzip(StaplerRequest req, StaplerResponse rsp) throws IOException {
        if (req.getHeader("Accept-Encoding").contains("gzip")) {
            rsp.setHeader("Content-Encoding", "gzip");
            byte[] content = CONTENT.getBytes();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            GZIPOutputStream o = new GZIPOutputStream(baos);
            try {
                o.write(content);
            } finally {
                o.close();
            }

            rsp.setContentLength(baos.toByteArray().length);
            rsp.getOutputStream().write(baos.toByteArray());
        } else {
            byte[] content = CONTENT.getBytes();
            rsp.setContentLength(content.length);
            rsp.getOutputStream().write(content);
        }
    }

    private static final String CONTENT = "Hello World";
}