package org.kohsuke.stapler.compression;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import javax.servlet.DispatcherType;
import org.apache.commons.io.IOUtils;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.test.JettyTestCase;

public class CompressionFilterTest extends JettyTestCase {

    @Override
    protected void configure(ServletContextHandler context) {
        super.configure(context);
        context.addFilter(CompressionFilter.class, "/*", EnumSet.of(DispatcherType.REQUEST));
    }

    public void testDoubleCompression() throws Exception {
        for (String endpoint : Arrays.asList("autoZip", "ownZip")) {
            HttpRequest httpRequest = HttpRequest.newBuilder(this.url.toURI().resolve(endpoint))
                    .GET()
                    .header("Accept-Encoding", "gzip")
                    .build();
            HttpClient httpClient = HttpClient.newHttpClient();
            HttpResponse<byte[]> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofByteArray());
            assertEquals(200, response.statusCode());
            byte[] data = IOUtils.toByteArray(new GZIPInputStream(new ByteArrayInputStream(response.body())));
            assertEquals(new String(data), CONTENT);
        }
    }

    /**
     * Turns out {@link #testDoubleCompression()} was insufficient to properly test the case,
     * as HttpURLConnection appears to ignores the content-length header and read the response to the end.
     */
    public void testDoubleCompression2() throws Exception {
        for (String endpoint : Arrays.asList("autoZip", "ownZip")) {
            HttpRequest httpRequest = HttpRequest.newBuilder(this.url.toURI().resolve(endpoint))
                    .GET()
                    .header("Accept-Encoding", "gzip")
                    .build();
            HttpClient httpClient = HttpClient.newHttpClient();
            HttpResponse<byte[]> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofByteArray());
            assertEquals(200, response.statusCode());
            byte[] data = IOUtils.toByteArray(new GZIPInputStream(new ByteArrayInputStream(response.body())));
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
