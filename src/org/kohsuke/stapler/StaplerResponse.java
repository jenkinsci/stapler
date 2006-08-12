package org.kohsuke.stapler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.File;
import java.net.URL;

/**
 * Defines additional operations made available by Stapler.
 *
 * <p>
 * Right now, there's none.
 *
 * @author Kohsuke Kawaguchi
 */
public interface StaplerResponse extends HttpServletResponse {
    /**
     * Evaluates the url against the given object and
     * forwards the request to the result.
     *
     * <p>
     * This can be used for example inside an action method
     * to forward a request to a JSP.
     *
     * @param it
     *      the URL is evaluated against this object. Must not be null.
     * @param url
     *      the relative URL (e.g., "foo" or "foo/bar") to resolve
     *      against the "it" object.
     * @param request
     *      Request to be forwarded.
     */
    void forward(Object it, String url, StaplerRequest request) throws ServletException, IOException;
    /**
     * Redirects the browser to where it came from (the referer.)
     */
    void forwardToPreviousPage(StaplerRequest request) throws ServletException, IOException;

    /**
     * Works like {@link #sendRedirect(String)} except that this method
     * escapes the URL.
     */
    void sendRedirect2(String url) throws IOException;

    /**
     * Serves a static resource.
     *
     * <p>
     * This method sets content type, HTTP status code, sends the complete data
     * and closes the response. This method also handles cache-control HTTP headers
     * like "If-Modified-Since" and others.
     */
    void serveFile(StaplerRequest request, URL res) throws ServletException, IOException;
}
