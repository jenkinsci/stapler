package org.kohsuke.stapler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.ServletContext;
import javax.servlet.RequestDispatcher;
import java.io.IOException;

/**
 * Defines additional parameters made available by the request dispatcher.
 *
 * @author Kohsuke Kawaguchi
 */
public interface StaplerRequest extends HttpServletRequest {
    /**
     * Returns the additional URL portion that wasn't used by the stapler,
     * excluding the query string.
     *
     * <p>
     * For example, if the requested URL is "foo/bar/zot/abc?def=ghi" and
     * "foo/bar" portion matched <tt>bar.jsp</tt>, this method returns
     * "/zot/abc".
     *
     * @return
     *      can be empty string, but never null.
     */
    String getRestOfPath();

    /**
     * Returns the {@link ServletContext} object given to the stapler
     * dispatcher servlet.
     */
    ServletContext getServletContext();

    /**
     * Gets the URL (e.g., "/WEB-INF/views/fully/qualified/class/name/jspName")
     * for the given object and the JSP name.
     *
     * @return null
     *      if the JSP is not found.
     */
    RequestDispatcher getView(Object it,String jspName) throws IOException;

    /**
     * Gets the part of the request URL from protocol up to the context path.
     * So typically it's something like <tt>http://foobar:8080/something</tt>
     */
    String getRootPath();
}
