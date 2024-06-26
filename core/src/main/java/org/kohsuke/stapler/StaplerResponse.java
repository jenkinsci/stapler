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

import edu.umd.cs.findbugs.annotations.NonNull;
import io.jenkins.servlet.ServletExceptionWrapper;
import io.jenkins.servlet.ServletOutputStreamWrapper;
import io.jenkins.servlet.ServletResponseWrapper;
import io.jenkins.servlet.http.CookieWrapper;
import io.jenkins.servlet.http.HttpServletRequestWrapper;
import io.jenkins.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.sf.json.JsonConfig;
import org.kohsuke.stapler.export.DataWriter;
import org.kohsuke.stapler.export.ExportConfig;
import org.kohsuke.stapler.export.Flavor;
import org.kohsuke.stapler.export.Model;
import org.kohsuke.stapler.export.NamedPathPruner;

/**
 * Defines additional operations made available by Stapler.
 *
 * @see Stapler#getCurrentResponse()
 * @author Kohsuke Kawaguchi
 * @deprecated use {@link StaplerResponse2}
 */
@Deprecated
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
    void sendRedirect2(@NonNull String url) throws IOException;

    /**
     * Works like {@link #sendRedirect2(String)} but allows the caller to specify the HTTP status code.
     */
    void sendRedirect(int statusCore, @NonNull String url) throws IOException;

    /**
     * Serves a static resource.
     *
     * <p>
     * This method sets content type, HTTP status code, sends the complete data
     * and closes the response. This method also handles cache-control HTTP headers
     * like "If-Modified-Since" and others.
     */
    void serveFile(StaplerRequest request, URL res) throws ServletException, IOException;

    void serveFile(StaplerRequest request, URL res, long expiration) throws ServletException, IOException;

    /**
     * Works like {@link #serveFile(StaplerRequest, URL)} but chooses the locale specific
     * version of the resource if it's available. The convention of "locale specific version"
     * is the same as that of property files.
     * So Japanese resource for {@code foo.html} would be named {@code foo_ja.html}.
     */
    void serveLocalizedFile(StaplerRequest request, URL res) throws ServletException, IOException;

    /**
     * Works like {@link #serveFile(StaplerRequest, URL, long)} but chooses the locale
     * specific version of the resource if it's available.
     *
     * See {@link #serveLocalizedFile(StaplerRequest, URL)} for more details.
     */
    void serveLocalizedFile(StaplerRequest request, URL res, long expiration) throws ServletException, IOException;

    /**
     * Serves a static resource.
     *
     * <p>
     * This method works like {@link #serveFile(StaplerRequest, URL)} but this version
     * allows the caller to fetch data from anywhere.
     *
     * @param data
     *      {@link InputStream} that contains the data of the static resource.
     * @param lastModified
     *      The timestamp when the resource was last modified. See {@link URLConnection#getLastModified()}
     *      for the meaning of the value. 0 if unknown, in which case "If-Modified-Since" handling
     *      will not be performed.
     * @param expiration
     *      The number of milliseconds until the resource will "expire".
     *      Until it expires the browser will be allowed to cache it
     *      and serve it without checking back with the server.
     *      After it expires, the client will send conditional GET to
     *      check if the resource is actually modified or not.
     *      If 0, it will immediately expire.
     * @param contentLength
     *      if the length of the input stream is known in advance, specify that value
     *      so that HTTP keep-alive works. Otherwise specify -1 to indicate that the length is unknown.
     * @param fileName
     *      file name of this resource. Used to determine the MIME type.
     *      Since the only important portion is the file extension, this could be just a file name,
     *      or a full path name, or even a pseudo file name that doesn't actually exist.
     *      It supports both '/' and '\\' as the path separator.
     *
     *      If this string starts with "mime-type:", like "mime-type:foo/bar", then "foo/bar" will
     *      be used as a MIME type without consulting the servlet container.
     */
    void serveFile(
            StaplerRequest req,
            InputStream data,
            long lastModified,
            long expiration,
            long contentLength,
            String fileName)
            throws ServletException, IOException;

    /**
     * @deprecated use form with long contentLength
     */
    @Deprecated
    void serveFile(
            StaplerRequest req,
            InputStream data,
            long lastModified,
            long expiration,
            int contentLength,
            String fileName)
            throws ServletException, IOException;

    /**
     * Serves a static resource.
     *
     * Expiration date is set to the value that forces browser to do conditional GET
     * for all resources.
     *
     * @see #serveFile(StaplerRequest, InputStream, long, long, int, String)
     */
    void serveFile(StaplerRequest req, InputStream data, long lastModified, long contentLength, String fileName)
            throws ServletException, IOException;

    /**
     * @deprecated use form with long contentLength
     */
    @Deprecated
    void serveFile(StaplerRequest req, InputStream data, long lastModified, int contentLength, String fileName)
            throws ServletException, IOException;

    /**
     * Serves the exposed bean in the specified flavor.
     *
     * <p>
     * This method performs the complete output from the header to the response body.
     * If the flavor is JSON, this method also supports JSONP via the {@code jsonp} query parameter.
     *
     * <p>The {@code depth} parameter may be used to specify a recursion depth
     * as in {@link Model#writeTo(Object,int,DataWriter)}.
     *
     * <p>As of 1.146, the {@code tree} parameter may be used to control the output
     * in detail; see {@link NamedPathPruner#NamedPathPruner(String)} for details.
     *
     * @deprecated Use {@link #serveExposedBean(StaplerRequest, Object, ExportConfig)}
     */
    @Deprecated
    void serveExposedBean(StaplerRequest req, Object exposedBean, Flavor flavor) throws ServletException, IOException;

    /**
     * Serves the exposed bean in the specified flavor.
     *
     * <p>
     * This method performs the complete output from the header to the response body.
     * If the flavor is JSON, this method also supports JSONP via the {@code jsonp} query parameter.
     *
     * <p>The {@code depth} parameter may be used to specify a recursion depth
     * as in {@link Model#writeTo(Object,int,DataWriter)}
     *
     * <p>As of 1.146, the {@code tree} parameter may be used to control the output
     * in detail; see {@link NamedPathPruner#NamedPathPruner(String)} for details.
     *
     * <p> {@link ExportConfig} is passed by the caller to control serialization behavior
     * @since 1.251
     */
    default void serveExposedBean(StaplerRequest req, Object exposedBean, ExportConfig exportConfig)
            throws ServletException, IOException {
        serveExposedBean(req, exposedBean, exportConfig.getFlavor());
    }

    /**
     * @deprecated use {@link #getOutputStream}
     */
    @Deprecated
    OutputStream getCompressedOutputStream(HttpServletRequest req) throws IOException;

    /**
     * @deprecated use {@link #getWriter}
     */
    @Deprecated
    Writer getCompressedWriter(HttpServletRequest req) throws IOException;

    /**
     * Performs the reverse proxy to the given URL.
     *
     * @return
     *      The status code of the response.
     */
    int reverseProxyTo(URL url, StaplerRequest req) throws IOException;

    /**
     * The JsonConfig to be used when serializing java beans from js bound methods to JSON.
     * Setting this to null will make the default config to be used.
     *
     * @param config the config
     */
    void setJsonConfig(JsonConfig config);

    /**
     * The JsonConfig to be used when serializing java beans to JSON previously set by {@link #setJsonConfig(JsonConfig)}.
     * Will return the default config if nothing has previously been set.
     *
     * @return the config
     */
    JsonConfig getJsonConfig();

    static StaplerResponse2 toStaplerResponse2(StaplerResponse from) {
        if (from instanceof StaplerResponseWrapper javax) {
            return javax.toStaplerResponse2();
        }
        return new StaplerResponse2WrapperImpl(from);
    }

    static StaplerResponse fromStaplerResponse2(StaplerResponse2 from) {
        if (from instanceof StaplerResponse2Wrapper jakarta) {
            return jakarta.toStaplerResponse();
        }
        return new StaplerResponseWrapperImpl(from);
    }

    interface StaplerResponse2Wrapper {
        StaplerResponse toStaplerResponse();
    }

    class StaplerResponse2WrapperImpl
            implements StaplerResponse2,
                    ServletResponseWrapper.JakartaServletResponseWrapper,
                    HttpServletResponseWrapper.JakartaHttpServletResponseWrapper,
                    StaplerResponse2Wrapper {
        private final StaplerResponse from;

        public StaplerResponse2WrapperImpl(StaplerResponse from) {
            this.from = Objects.requireNonNull(from);
        }

        @Override
        public void forward(Object it, String url, StaplerRequest2 request)
                throws jakarta.servlet.ServletException, IOException {
            try {
                from.forward(it, url, StaplerRequest.fromStaplerRequest2(request));
            } catch (ServletException e) {
                throw ServletExceptionWrapper.toJakartaServletException(e);
            }
        }

        @Override
        public void forwardToPreviousPage(StaplerRequest2 request)
                throws jakarta.servlet.ServletException, IOException {
            try {
                from.forwardToPreviousPage(StaplerRequest.fromStaplerRequest2(request));
            } catch (ServletException e) {
                throw ServletExceptionWrapper.toJakartaServletException(e);
            }
        }

        @Override
        public void sendRedirect2(@NonNull String url) throws IOException {
            from.sendRedirect2(url);
        }

        @Override
        public void sendRedirect(int statusCore, @NonNull String url) throws IOException {
            from.sendRedirect(statusCore, url);
        }

        @Override
        public void serveFile(StaplerRequest2 request, URL res) throws jakarta.servlet.ServletException, IOException {
            try {
                from.serveFile(StaplerRequest.fromStaplerRequest2(request), res);
            } catch (ServletException e) {
                throw ServletExceptionWrapper.toJakartaServletException(e);
            }
        }

        @Override
        public void serveFile(StaplerRequest2 request, URL res, long expiration)
                throws jakarta.servlet.ServletException, IOException {
            try {
                from.serveFile(StaplerRequest.fromStaplerRequest2(request), res, expiration);
            } catch (ServletException e) {
                throw ServletExceptionWrapper.toJakartaServletException(e);
            }
        }

        @Override
        public void serveLocalizedFile(StaplerRequest2 request, URL res)
                throws jakarta.servlet.ServletException, IOException {
            try {
                from.serveLocalizedFile(StaplerRequest.fromStaplerRequest2(request), res);
            } catch (ServletException e) {
                throw ServletExceptionWrapper.toJakartaServletException(e);
            }
        }

        @Override
        public void serveLocalizedFile(StaplerRequest2 request, URL res, long expiration)
                throws jakarta.servlet.ServletException, IOException {
            try {
                from.serveLocalizedFile(StaplerRequest.fromStaplerRequest2(request), res, expiration);
            } catch (ServletException e) {
                throw ServletExceptionWrapper.toJakartaServletException(e);
            }
        }

        @Override
        public void serveFile(
                StaplerRequest2 req,
                InputStream data,
                long lastModified,
                long expiration,
                long contentLength,
                String fileName)
                throws jakarta.servlet.ServletException, IOException {
            try {
                from.serveFile(
                        StaplerRequest.fromStaplerRequest2(req),
                        data,
                        lastModified,
                        expiration,
                        contentLength,
                        fileName);
            } catch (ServletException e) {
                throw ServletExceptionWrapper.toJakartaServletException(e);
            }
        }

        @Override
        public void serveFile(
                StaplerRequest2 req,
                InputStream data,
                long lastModified,
                long expiration,
                int contentLength,
                String fileName)
                throws jakarta.servlet.ServletException, IOException {
            try {
                from.serveFile(
                        StaplerRequest.fromStaplerRequest2(req),
                        data,
                        lastModified,
                        expiration,
                        contentLength,
                        fileName);
            } catch (ServletException e) {
                throw ServletExceptionWrapper.toJakartaServletException(e);
            }
        }

        @Override
        public void serveFile(
                StaplerRequest2 req, InputStream data, long lastModified, long contentLength, String fileName)
                throws jakarta.servlet.ServletException, IOException {
            try {
                from.serveFile(StaplerRequest.fromStaplerRequest2(req), data, lastModified, contentLength, fileName);
            } catch (ServletException e) {
                throw ServletExceptionWrapper.toJakartaServletException(e);
            }
        }

        @Override
        public void serveFile(
                StaplerRequest2 req, InputStream data, long lastModified, int contentLength, String fileName)
                throws jakarta.servlet.ServletException, IOException {
            try {
                from.serveFile(StaplerRequest.fromStaplerRequest2(req), data, lastModified, contentLength, fileName);
            } catch (ServletException e) {
                throw ServletExceptionWrapper.toJakartaServletException(e);
            }
        }

        @Override
        public void serveExposedBean(StaplerRequest2 req, Object exposedBean, Flavor flavor)
                throws jakarta.servlet.ServletException, IOException {
            try {
                from.serveExposedBean(StaplerRequest.fromStaplerRequest2(req), exposedBean, flavor);
            } catch (ServletException e) {
                throw ServletExceptionWrapper.toJakartaServletException(e);
            }
        }

        @Override
        public void serveExposedBean(StaplerRequest2 req, Object exposedBean, ExportConfig exportConfig)
                throws jakarta.servlet.ServletException, IOException {
            try {
                from.serveExposedBean(StaplerRequest.fromStaplerRequest2(req), exposedBean, exportConfig);
            } catch (ServletException e) {
                throw ServletExceptionWrapper.toJakartaServletException(e);
            }
        }

        @Override
        public OutputStream getCompressedOutputStream(jakarta.servlet.http.HttpServletRequest req) throws IOException {
            return from.getCompressedOutputStream(HttpServletRequestWrapper.fromJakartaHttpServletRequest(req));
        }

        @Override
        public Writer getCompressedWriter(jakarta.servlet.http.HttpServletRequest req) throws IOException {
            return from.getCompressedWriter(HttpServletRequestWrapper.fromJakartaHttpServletRequest(req));
        }

        @Override
        public int reverseProxyTo(URL url, StaplerRequest2 req) throws IOException {
            return from.reverseProxyTo(url, StaplerRequest.fromStaplerRequest2(req));
        }

        @Override
        public void setJsonConfig(JsonConfig config) {
            from.setJsonConfig(config);
        }

        @Override
        public JsonConfig getJsonConfig() {
            return from.getJsonConfig();
        }

        @Override
        public String getCharacterEncoding() {
            return from.getCharacterEncoding();
        }

        @Override
        public String getContentType() {
            return from.getContentType();
        }

        @Override
        public jakarta.servlet.ServletOutputStream getOutputStream() throws IOException {
            return ServletOutputStreamWrapper.toJakartaServletOutputStream(from.getOutputStream());
        }

        @Override
        public PrintWriter getWriter() throws IOException {
            return from.getWriter();
        }

        @Override
        public void setCharacterEncoding(String charset) {
            from.setCharacterEncoding(charset);
        }

        @Override
        public void setContentLength(int len) {
            from.setContentLength(len);
        }

        @Override
        public void setContentLengthLong(long len) {
            from.setContentLengthLong(len);
        }

        @Override
        public void setContentType(String type) {
            from.setContentType(type);
        }

        @Override
        public void setBufferSize(int size) {
            from.setBufferSize(size);
        }

        @Override
        public int getBufferSize() {
            return from.getBufferSize();
        }

        @Override
        public void flushBuffer() throws IOException {
            from.flushBuffer();
        }

        @Override
        public void resetBuffer() {
            from.resetBuffer();
        }

        @Override
        public boolean isCommitted() {
            return from.isCommitted();
        }

        @Override
        public void reset() {
            from.reset();
        }

        @Override
        public void setLocale(Locale loc) {
            from.setLocale(loc);
        }

        @Override
        public Locale getLocale() {
            return from.getLocale();
        }

        @Override
        public void addCookie(jakarta.servlet.http.Cookie cookie) {
            from.addCookie(CookieWrapper.fromJakartaServletHttpCookie(cookie));
        }

        @Override
        public boolean containsHeader(String name) {
            return from.containsHeader(name);
        }

        @Override
        public String encodeURL(String url) {
            return from.encodeURL(url);
        }

        @Override
        public String encodeRedirectURL(String url) {
            return from.encodeRedirectURL(url);
        }

        @Override
        public String encodeUrl(String url) {
            return from.encodeUrl(url);
        }

        @Override
        public String encodeRedirectUrl(String url) {
            return from.encodeRedirectUrl(url);
        }

        @Override
        public void sendError(int sc, String msg) throws IOException {
            from.sendError(sc, msg);
        }

        @Override
        public void sendError(int sc) throws IOException {
            from.sendError(sc);
        }

        @Override
        public void sendRedirect(String location) throws IOException {
            from.sendRedirect(location);
        }

        @Override
        public void setDateHeader(String name, long date) {
            from.setDateHeader(name, date);
        }

        @Override
        public void addDateHeader(String name, long date) {
            from.addDateHeader(name, date);
        }

        @Override
        public void setHeader(String name, String value) {
            from.setHeader(name, value);
        }

        @Override
        public void addHeader(String name, String value) {
            from.addHeader(name, value);
        }

        @Override
        public void setIntHeader(String name, int value) {
            from.setIntHeader(name, value);
        }

        @Override
        public void addIntHeader(String name, int value) {
            from.addIntHeader(name, value);
        }

        @Override
        public void setStatus(int sc) {
            from.setStatus(sc);
        }

        @Override
        public void setStatus(int sc, String sm) {
            from.setStatus(sc, sm);
        }

        @Override
        public int getStatus() {
            return from.getStatus();
        }

        @Override
        public String getHeader(String name) {
            return from.getHeader(name);
        }

        @Override
        public Collection<String> getHeaders(String name) {
            return from.getHeaders(name);
        }

        @Override
        public Collection<String> getHeaderNames() {
            return from.getHeaderNames();
        }

        @Override
        public void setTrailerFields(Supplier<Map<String, String>> supplier) {
            from.setTrailerFields(supplier);
        }

        @Override
        public Supplier<Map<String, String>> getTrailerFields() {
            return from.getTrailerFields();
        }

        @Override
        public ServletResponse toJavaxServletResponse() {
            return from;
        }

        @Override
        public HttpServletResponse toJavaxHttpServletResponse() {
            return from;
        }

        @Override
        public StaplerResponse toStaplerResponse() {
            return from;
        }
    }

    interface StaplerResponseWrapper {
        StaplerResponse2 toStaplerResponse2();
    }

    class StaplerResponseWrapperImpl
            implements StaplerResponse,
                    ServletResponseWrapper.JavaxServletResponseWrapper,
                    HttpServletResponseWrapper.JavaxHttpServletResponseWrapper,
                    StaplerResponseWrapper {
        private final StaplerResponse2 from;

        public StaplerResponseWrapperImpl(StaplerResponse2 from) {
            this.from = Objects.requireNonNull(from);
        }

        @Override
        public void forward(Object it, String url, StaplerRequest request) throws ServletException, IOException {
            try {
                from.forward(it, url, StaplerRequest.toStaplerRequest2(request));
            } catch (jakarta.servlet.ServletException e) {
                throw ServletExceptionWrapper.fromJakartaServletException(e);
            }
        }

        @Override
        public void forwardToPreviousPage(StaplerRequest request) throws ServletException, IOException {
            try {
                from.forwardToPreviousPage(StaplerRequest.toStaplerRequest2(request));
            } catch (jakarta.servlet.ServletException e) {
                throw ServletExceptionWrapper.fromJakartaServletException(e);
            }
        }

        @Override
        public void sendRedirect2(@NonNull String url) throws IOException {
            from.sendRedirect2(url);
        }

        @Override
        public void sendRedirect(int statusCore, @NonNull String url) throws IOException {
            from.sendRedirect(statusCore, url);
        }

        @Override
        public void serveFile(StaplerRequest request, URL res) throws ServletException, IOException {
            try {
                from.serveFile(StaplerRequest.toStaplerRequest2(request), res);
            } catch (jakarta.servlet.ServletException e) {
                throw ServletExceptionWrapper.fromJakartaServletException(e);
            }
        }

        @Override
        public void serveFile(StaplerRequest request, URL res, long expiration) throws ServletException, IOException {
            try {
                from.serveFile(StaplerRequest.toStaplerRequest2(request), res, expiration);
            } catch (jakarta.servlet.ServletException e) {
                throw ServletExceptionWrapper.fromJakartaServletException(e);
            }
        }

        @Override
        public void serveLocalizedFile(StaplerRequest request, URL res) throws ServletException, IOException {
            try {
                from.serveLocalizedFile(StaplerRequest.toStaplerRequest2(request), res);
            } catch (jakarta.servlet.ServletException e) {
                throw ServletExceptionWrapper.fromJakartaServletException(e);
            }
        }

        @Override
        public void serveLocalizedFile(StaplerRequest request, URL res, long expiration)
                throws ServletException, IOException {
            try {
                from.serveLocalizedFile(StaplerRequest.toStaplerRequest2(request), res, expiration);
            } catch (jakarta.servlet.ServletException e) {
                throw ServletExceptionWrapper.fromJakartaServletException(e);
            }
        }

        @Override
        public void serveFile(
                StaplerRequest req,
                InputStream data,
                long lastModified,
                long expiration,
                long contentLength,
                String fileName)
                throws ServletException, IOException {
            try {
                from.serveFile(
                        StaplerRequest.toStaplerRequest2(req), data, lastModified, expiration, contentLength, fileName);
            } catch (jakarta.servlet.ServletException e) {
                throw ServletExceptionWrapper.fromJakartaServletException(e);
            }
        }

        @Override
        public void serveFile(
                StaplerRequest req,
                InputStream data,
                long lastModified,
                long expiration,
                int contentLength,
                String fileName)
                throws ServletException, IOException {
            try {
                from.serveFile(
                        StaplerRequest.toStaplerRequest2(req), data, lastModified, expiration, contentLength, fileName);
            } catch (jakarta.servlet.ServletException e) {
                throw ServletExceptionWrapper.fromJakartaServletException(e);
            }
        }

        @Override
        public void serveFile(
                StaplerRequest req, InputStream data, long lastModified, long contentLength, String fileName)
                throws ServletException, IOException {
            try {
                from.serveFile(StaplerRequest.toStaplerRequest2(req), data, lastModified, contentLength, fileName);
            } catch (jakarta.servlet.ServletException e) {
                throw ServletExceptionWrapper.fromJakartaServletException(e);
            }
        }

        @Override
        public void serveFile(
                StaplerRequest req, InputStream data, long lastModified, int contentLength, String fileName)
                throws ServletException, IOException {
            try {
                from.serveFile(StaplerRequest.toStaplerRequest2(req), data, lastModified, contentLength, fileName);
            } catch (jakarta.servlet.ServletException e) {
                throw ServletExceptionWrapper.fromJakartaServletException(e);
            }
        }

        @Override
        public void serveExposedBean(StaplerRequest req, Object exposedBean, Flavor flavor)
                throws ServletException, IOException {
            try {
                from.serveExposedBean(StaplerRequest.toStaplerRequest2(req), exposedBean, flavor);
            } catch (jakarta.servlet.ServletException e) {
                throw ServletExceptionWrapper.fromJakartaServletException(e);
            }
        }

        @Override
        public void serveExposedBean(StaplerRequest req, Object exposedBean, ExportConfig exportConfig)
                throws ServletException, IOException {
            try {
                from.serveExposedBean(StaplerRequest.toStaplerRequest2(req), exposedBean, exportConfig);
            } catch (jakarta.servlet.ServletException e) {
                throw ServletExceptionWrapper.fromJakartaServletException(e);
            }
        }

        @Override
        public OutputStream getCompressedOutputStream(HttpServletRequest req) throws IOException {
            return from.getCompressedOutputStream(HttpServletRequestWrapper.toJakartaHttpServletRequest(req));
        }

        @Override
        public Writer getCompressedWriter(HttpServletRequest req) throws IOException {
            return from.getCompressedWriter(HttpServletRequestWrapper.toJakartaHttpServletRequest(req));
        }

        @Override
        public int reverseProxyTo(URL url, StaplerRequest req) throws IOException {
            return from.reverseProxyTo(url, StaplerRequest.toStaplerRequest2(req));
        }

        @Override
        public void setJsonConfig(JsonConfig config) {
            from.setJsonConfig(config);
        }

        @Override
        public JsonConfig getJsonConfig() {
            return from.getJsonConfig();
        }

        @Override
        public String getCharacterEncoding() {
            return from.getCharacterEncoding();
        }

        @Override
        public String getContentType() {
            return from.getContentType();
        }

        @Override
        public ServletOutputStream getOutputStream() throws IOException {
            return ServletOutputStreamWrapper.fromJakartaServletOutputStream(from.getOutputStream());
        }

        @Override
        public PrintWriter getWriter() throws IOException {
            return from.getWriter();
        }

        @Override
        public void setCharacterEncoding(String charset) {
            from.setCharacterEncoding(charset);
        }

        @Override
        public void setContentLength(int len) {
            from.setContentLength(len);
        }

        @Override
        public void setContentLengthLong(long len) {
            from.setContentLengthLong(len);
        }

        @Override
        public void setContentType(String type) {
            from.setContentType(type);
        }

        @Override
        public void setBufferSize(int size) {
            from.setBufferSize(size);
        }

        @Override
        public int getBufferSize() {
            return from.getBufferSize();
        }

        @Override
        public void flushBuffer() throws IOException {
            from.flushBuffer();
        }

        @Override
        public void resetBuffer() {
            from.resetBuffer();
        }

        @Override
        public boolean isCommitted() {
            return from.isCommitted();
        }

        @Override
        public void reset() {
            from.reset();
        }

        @Override
        public void setLocale(Locale loc) {
            from.setLocale(loc);
        }

        @Override
        public Locale getLocale() {
            return from.getLocale();
        }

        @Override
        public void addCookie(Cookie cookie) {
            from.addCookie(CookieWrapper.toJakartaServletHttpCookie(cookie));
        }

        @Override
        public boolean containsHeader(String name) {
            return from.containsHeader(name);
        }

        @Override
        public String encodeURL(String url) {
            return from.encodeURL(url);
        }

        @Override
        public String encodeRedirectURL(String url) {
            return from.encodeRedirectURL(url);
        }

        @Override
        public String encodeUrl(String url) {
            return from.encodeUrl(url);
        }

        @Override
        public String encodeRedirectUrl(String url) {
            return from.encodeRedirectUrl(url);
        }

        @Override
        public void sendError(int sc, String msg) throws IOException {
            from.sendError(sc, msg);
        }

        @Override
        public void sendError(int sc) throws IOException {
            from.sendError(sc);
        }

        @Override
        public void sendRedirect(String location) throws IOException {
            from.sendRedirect(location);
        }

        @Override
        public void setDateHeader(String name, long date) {
            from.setDateHeader(name, date);
        }

        @Override
        public void addDateHeader(String name, long date) {
            from.addDateHeader(name, date);
        }

        @Override
        public void setHeader(String name, String value) {
            from.setHeader(name, value);
        }

        @Override
        public void addHeader(String name, String value) {
            from.addHeader(name, value);
        }

        @Override
        public void setIntHeader(String name, int value) {
            from.setIntHeader(name, value);
        }

        @Override
        public void addIntHeader(String name, int value) {
            from.addIntHeader(name, value);
        }

        @Override
        public void setStatus(int sc) {
            from.setStatus(sc);
        }

        @Override
        public void setStatus(int sc, String sm) {
            from.setStatus(sc, sm);
        }

        @Override
        public int getStatus() {
            return from.getStatus();
        }

        @Override
        public String getHeader(String name) {
            return from.getHeader(name);
        }

        @Override
        public Collection<String> getHeaders(String name) {
            return from.getHeaders(name);
        }

        @Override
        public Collection<String> getHeaderNames() {
            return from.getHeaderNames();
        }

        @Override
        public void setTrailerFields(Supplier<Map<String, String>> supplier) {
            from.setTrailerFields(supplier);
        }

        @Override
        public Supplier<Map<String, String>> getTrailerFields() {
            return from.getTrailerFields();
        }

        @Override
        public jakarta.servlet.ServletResponse toJakartaServletResponse() {
            return from;
        }

        @Override
        public jakarta.servlet.http.HttpServletResponse toJakartaHttpServletResponse() {
            return from;
        }

        @Override
        public StaplerResponse2 toStaplerResponse2() {
            return from;
        }
    }
}
