package org.kohsuke.stapler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Factory for {@link HttpResponse}.
 *
 * @author Kohsuke Kawaguchi
 */
public class HttpResponses {
    public static abstract class HttpResponseException extends RuntimeException implements HttpResponse {
        public HttpResponseException() {
        }

        public HttpResponseException(String message) {
            super(message);
        }

        public HttpResponseException(String message, Throwable cause) {
            super(message, cause);
        }

        public HttpResponseException(Throwable cause) {
            super(cause);
        }
    }

    public static HttpResponseException ok() {
        return status(HttpServletResponse.SC_OK);
    }

    public static HttpResponseException notFound() {
        return status(HttpServletResponse.SC_NOT_FOUND);
    }

    public static HttpResponseException forbidden() {
        return status(HttpServletResponse.SC_FORBIDDEN);
    }

    public static HttpResponseException status(final int code) {
        return new HttpResponseException() {
            public void generateResponse(StaplerRequest req, StaplerResponse rsp, Object node) throws IOException, ServletException {
                rsp.setStatus(code);
            }
        };
    }

    /**
     * @param relative
     *      The path relative to the context path. The context path + this value
     *      is sent to the user.
     */
    public static HttpResponseException redirectViaContextPath(final String relative) {
        return new HttpResponseException() {
            public void generateResponse(StaplerRequest req, StaplerResponse rsp, Object node) throws IOException, ServletException {
                rsp.sendRedirect2(req.getContextPath()+relative);
            }
        };
    }

    /**
     * Redirect to "."
     */
    public static HttpResponse redirectToDot() {
        return HttpRedirect.DOT;
    }

    /**
     * Redirect to the context root
     */
    public static HttpResponseException redirectToContextRoot() {
        return redirectViaContextPath("");
    }
}
