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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;

import static javax.servlet.http.HttpServletResponse.SC_MOVED_TEMPORARILY;

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
     * Sends an error with a stack trace.
     * @see #errorWithoutStack
     */
    @SuppressWarnings({"ThrowableInstanceNeverThrown"})
    public static HttpResponseException error(int code, String errorMessage) {
        return error(code,new Exception(errorMessage));
    }

    public static HttpResponseException error(Throwable cause) {
        return error(500,cause);
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
     * Sends an error without a stack trace.
     * @since 1.215
     * @see #error(int, String)
     */
    public static HttpResponseException errorWithoutStack(final int code, final String errorMessage) {
        return new HttpResponseException() {
            public void generateResponse(StaplerRequest req, StaplerResponse rsp, Object node) throws IOException, ServletException {
                rsp.sendError(code, errorMessage);
            }
        };
    }

    public static HttpResponseException redirectViaContextPath(String relative) {
        return redirectViaContextPath(SC_MOVED_TEMPORARILY,relative);
    }

    /**
     * @param relative
     *      The path relative to the context path. The context path + this value
     *      is sent to the user.
     */
    public static HttpResponseException redirectViaContextPath(final int statusCode, final String relative) {
        return new HttpResponseException() {
            public void generateResponse(StaplerRequest req, StaplerResponse rsp, Object node) throws IOException, ServletException {
                StringBuilder sb = new StringBuilder(req.getContextPath());
                if (!relative.startsWith("/"))  sb.append('/');
                sb.append(relative);

                rsp.sendRedirect(statusCode,sb.toString());
            }
        };
    }

    /**
     * @param url
     *      The URL to redirect to. If relative, relative to the page currently being served.
     */
    public static HttpRedirect redirectTo(String url) {
        return new HttpRedirect(url);
    }

    public static HttpRedirect redirectTo(int statusCode, String url) {
        return new HttpRedirect(statusCode,url);
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

    /**
     * Serves a static resource specified by the URL.
     * Short for {@code staticResource(resource,0)}
     */
    public static HttpResponse staticResource(URL resource) {
        return staticResource(resource,0);
    }

    /**
     * Serves a static resource specified by the URL.
     *
     * @param resource
     *      The static resource to be served.
     * @param expiration
     *      The number of milliseconds until the resource will "expire".
     *      Until it expires the browser will be allowed to cache it
     *      and serve it without checking back with the server.
     *      After it expires, the client will send conditional GET to
     *      check if the resource is actually modified or not.
     *      If 0, it will immediately expire.
     */
    public static HttpResponse staticResource(final URL resource, final long expiration) {
        return new HttpResponse() {
            public void generateResponse(StaplerRequest req, StaplerResponse rsp, Object node) throws IOException, ServletException {
                rsp.serveFile(req,resource,expiration);
            }
        };
    }

    /**
     * Serves the literal HTML.
     */
    public static HttpResponse html(final String literalHtml) {
        return new HttpResponse() {
            public void generateResponse(StaplerRequest req, StaplerResponse rsp, Object node) throws IOException, ServletException {
                rsp.setContentType("text/html;charset=UTF-8");
                rsp.getWriter().println(literalHtml);
            }
        };
    }

    /**
     * Serves the plain text.
     */
    public static HttpResponse plainText(final String plainText) {
        return new HttpResponse() {
            public void generateResponse(StaplerRequest req, StaplerResponse rsp, Object node) throws IOException, ServletException {
                rsp.setContentType("text/plain;charset=UTF-8");
                rsp.getWriter().println(plainText);
            }
        };
    }

    public static ForwardToView forwardToView(Object it, String view) {
        return new ForwardToView(it,view);
    }

    public static ForwardToView forwardToView(Class clazz, String view) {
        return new ForwardToView(clazz,view);
    }
}
