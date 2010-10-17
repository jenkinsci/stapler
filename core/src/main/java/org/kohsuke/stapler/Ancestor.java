/*
 * Copyright (c) 2004-2010, Kohsuke Kawaguchi
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided
 * that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice, this list of
 *       conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright notice, this list of
 *       conditions and the following disclaimer in the documentation and/or other materials
 *       provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS
 * OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
 * AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER
 * IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF
 * THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

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
     * Gets the remaining URL after this ancestor.
     *
     * <p>
     * The returned string represents the portion of the request URL
     * that follows this ancestor. It starts and ends without '/'.
     * So, for example, if the request URL is "foo/bar/3" and this ancestor object is
     * obtained from the app root object by <tt>getFoo()</tt>,
     * then this string will be <tt>bar/3</tt>
     */
    String getRestOfUrl();

    /**
     * Of the tokens that constitute {@link #getRestOfUrl()},
     * return the n-th token. So in the example described in {@link #getRestOfUrl()},
     * {@code getNextToken(0).equals("bar")} and
     * {@code getNextToken(1).equals("3")}
     */
    String getNextToken(int n);

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
