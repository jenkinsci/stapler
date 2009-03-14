package org.kohsuke.stapler;

import javax.servlet.http.HttpServletRequest;

/**
 * Information about ancestor of the "it" node.
 *
 * @author Kohsuke Kawaguchi
 */
public interface Ancestor {
    /**
     * Gets the model object of the application.
     */
    Object getObject();

    /**
     * Gets the URL to this ancestor.
     *
     * <p>
     * The returned string represents the portion of the request URL
     * that matches this object. It starts with
     * {@link HttpServletRequest#getContextPath() context path},
     * and it ends without '/'. So, for example, if your web app
     * is deployed as "mywebapp" and this ancestor object is
     * obtained from the app root object by <tt>getFoo().getBar(3)</tt>,
     * then this string will be <tt>/mywebapp/foo/bar/3</tt>
     *
     * <p>
     * Any ASCII-unsafe characters are escaped.
     *
     * @return
     *      never null.
     */
    String getUrl();

    /**
     * Gets the complete URL to this ancestor.
     *
     * <p>
     * This method works like {@link #getUrl()} except it contains
     * the host name and the port number.
     */
    String getFullUrl();

    /**
     * Gets the relative path from the current object to this ancestor.
     *
     * <p>
     * The returned string looks like "../.." (ends without '/')
     *
     * @return
     *      never null.
     */
    String getRelativePath();

    /**
     * Gets the previous ancestor, or null if none (meaning
     * this is the root object.)
     */
    Ancestor getPrev();

    /**
     * Gets the next ancestor, or null if none (meaning
     * this is the 'it' object.
     */
    Ancestor getNext();
}
