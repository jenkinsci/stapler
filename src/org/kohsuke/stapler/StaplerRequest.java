package org.kohsuke.stapler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.ServletContext;
import javax.servlet.RequestDispatcher;
import java.io.IOException;
import java.util.List;

/**
 * Defines additional parameters/operations made available by Stapler.
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

    /**
     * Returns a list of ancestor objects that lead to the "it" object.
     * The returned list contains {@link Ancestor} objects sorted in the
     * order from root to the "it" object.
     *
     * <p>
     * For example, if the URL was "foo/bar/zot" and the "it" object
     * was determined as <code>root.getFoo().getBar("zot")</code>,
     * then this list will contain the following 3 objects in this order:
     * <ol>
     *  <li>the root object
     *  <li>root.getFoo() object
     *  <li>root.getFoo().getBar("zot") object (the "it" object)
     * </ol>
     * <p>
     * 
     *
     * @return
     *      list of {@link Ancestor}s. Can be empty, but always non-null.
     */
    List getAncestors();
}
