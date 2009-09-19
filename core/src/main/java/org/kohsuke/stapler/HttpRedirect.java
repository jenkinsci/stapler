package org.kohsuke.stapler;

import javax.servlet.ServletException;
import java.io.IOException;

/**
 * {@link HttpResponse} that dose HTTP 302 redirect.
 *
 * @author Kohsuke Kawaguchi
 */
public final class HttpRedirect implements HttpResponse {
    private final String url;

    public HttpRedirect(String url) {
        this.url = url;
    }

    public void generateResponse(StaplerRequest req, StaplerResponse rsp, Object node) throws IOException, ServletException {
        rsp.sendRedirect2(url);
    }

    /**
     * @param relative
     *      The path relative to the context path. The context path + this value
     *      is sent to the user.
     * @deprecated
     *      Use {@link HttpResponses#redirectViaContextPath(String)}.
     */
    public static HttpResponse fromContextPath(final String relative) {
        return HttpResponses.redirectViaContextPath(relative);
    }

    /**
     * Redirect to "."
     */
    public static HttpRedirect DOT = new HttpRedirect(".");

    /**
     * Redirect to the context root
     */
    public static HttpResponse CONTEXT_ROOT = fromContextPath("");
}
