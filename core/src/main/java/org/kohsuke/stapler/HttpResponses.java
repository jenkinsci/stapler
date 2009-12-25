package org.kohsuke.stapler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

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

    public static HttpResponseException error(final int code, final Throwable cause) {
        return new HttpResponseException(cause) {
            public void generateResponse(StaplerRequest req, StaplerResponse rsp, Object node) throws IOException, ServletException {
                rsp.setStatus(code);

                rsp.setContentType("text/plain;charset=UTF-8");
                PrintWriter w = new PrintWriter(rsp.getWriter());
                cause.printStackTrace(w);
                w.close();
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

    /**
     * Redirects the user back to where he came from.
     */
    public static HttpResponseException forwardToPreviousPage() {
        return FORWARD_TO_PREVIOUS_PAGE;
    }

    private static final HttpResponseException FORWARD_TO_PREVIOUS_PAGE = new HttpResponseException() {
        public void generateResponse(StaplerRequest req, StaplerResponse rsp, Object node) throws IOException, ServletException {
            rsp.forwardToPreviousPage(req);
        }
    };
}
