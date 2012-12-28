package org.kohsuke.stapler;

import org.kohsuke.stapler.test.AbstractStaplerTest;

import javax.servlet.ServletOutputStream;
import java.io.IOException;

import static javax.servlet.http.HttpServletResponse.*;
import static org.mockito.Mockito.*;

/**
 * @author Kohsuke Kawaguchi
 */
public class ResponseImplTest {
    public static class RedirectTest extends AbstractStaplerTest {
        @Override
        protected void setUp() throws Exception {
            super.setUp();
            when(rawRequest.getScheme()).thenReturn("http");
            when(rawRequest.getServerName()).thenReturn("example.com");
            when(rawRequest.getServerPort()).thenReturn(80);
            when(rawRequest.getRequestURI()).thenReturn("/foo/bar/zot");
            when(rawResponse.getOutputStream()).thenReturn(new ServletOutputStream() {
                public void write(int b) throws IOException {
                    throw new AssertionError();
                }
            });
        }

        public void testSendRedirectRelative() throws IOException {
            response.sendRedirect(SC_SEE_OTHER, "foobar");
            verify(rawResponse).setStatus(SC_SEE_OTHER);
            verify(rawResponse).setHeader("Location", "http://example.com/foo/bar/foobar");
        }

        public void testSendRedirectWithinHost() throws IOException {
            response.sendRedirect(SC_SEE_OTHER, "/foobar");
            verify(rawResponse).setStatus(SC_SEE_OTHER);
            verify(rawResponse).setHeader("Location", "http://example.com/foobar");
        }

        public void testSendRedirectAbsoluteURL() throws IOException {
            response.sendRedirect(SC_SEE_OTHER, "https://jenkins-ci.org/");
            verify(rawResponse).setStatus(SC_SEE_OTHER);
            verify(rawResponse).setHeader("Location", "https://jenkins-ci.org/");
        }
    }
}
