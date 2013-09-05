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

import javax.annotation.Nonnull;
import javax.servlet.ServletException;
import java.io.IOException;

import static javax.servlet.http.HttpServletResponse.SC_MOVED_TEMPORARILY;

/**
 * {@link HttpResponse} that dose HTTP 302 redirect.
 * Extends from {@link RuntimeException} so that you can throw it.
 *
 * @author Kohsuke Kawaguchi
 */
public final class HttpRedirect extends RuntimeException implements HttpResponse {
    private final int statusCode;
    private final String url;

    public HttpRedirect(@Nonnull String url) {
        this(SC_MOVED_TEMPORARILY,url);
    }

    public HttpRedirect(int statusCode, @Nonnull String url) {
        this.statusCode = statusCode;
        if (url == null) {
            throw new NullPointerException();
        }
        this.url = url;
    }

    public void generateResponse(StaplerRequest req, StaplerResponse rsp, Object node) throws IOException, ServletException {
        rsp.sendRedirect(statusCode,url);
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
