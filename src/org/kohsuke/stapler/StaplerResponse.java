package org.kohsuke.stapler;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import java.io.IOException;

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
}
