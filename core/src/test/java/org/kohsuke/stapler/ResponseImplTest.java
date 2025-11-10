package org.kohsuke.stapler;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kohsuke.stapler.test.AbstractStaplerTest;

/**
 * @author Kohsuke Kawaguchi
 */
class ResponseImplTest extends AbstractStaplerTest {

    @Override
    @BeforeEach
    protected void beforeEach() throws Exception {
        super.beforeEach();
        when(rawRequest.getScheme()).thenReturn("http");
        when(rawRequest.getServerName()).thenReturn("example.com");
        when(rawRequest.getServerPort()).thenReturn(80);
        when(rawRequest.getRequestURI()).thenReturn("/foo/bar/zot");
        when(rawResponse.getOutputStream()).thenReturn(new ServletOutputStream() {
            @Override
            public void write(int b) throws IOException {
                throw new AssertionError();
            }

            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setWriteListener(WriteListener writeListener) {
                // ignores
            }
        });
    }

    @Test
    void testSendRedirectRelative() throws IOException {
        response.sendRedirect(HttpServletResponse.SC_SEE_OTHER, "foobar");
        verify(rawResponse).setStatus(HttpServletResponse.SC_SEE_OTHER);
        verify(rawResponse).setHeader("Location", "http://example.com/foo/bar/foobar");
    }

    @Test
    void testSendRedirectWithinHost() throws IOException {
        response.sendRedirect(HttpServletResponse.SC_SEE_OTHER, "/foobar");
        verify(rawResponse).setStatus(HttpServletResponse.SC_SEE_OTHER);
        verify(rawResponse).setHeader("Location", "http://example.com/foobar");
    }

    @Test
    void testSendRedirectAbsoluteURL() throws IOException {
        response.sendRedirect(HttpServletResponse.SC_SEE_OTHER, "https://jenkins-ci.org/");
        verify(rawResponse).setStatus(HttpServletResponse.SC_SEE_OTHER);
        verify(rawResponse).setHeader("Location", "https://jenkins-ci.org/");
    }
}
