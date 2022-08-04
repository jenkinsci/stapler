/*
 * Copyright (c) 1997-2018 Oracle and/or its affiliates and others.
 * All rights reserved.
 * Copyright 2004 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package javax.servlet.http;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletResponse;

@SuppressFBWarnings(
        value = {"UNVALIDATED_REDIRECT", "URL_REWRITING", "XSS_SERVLET"},
        justification = "TODO needs triage")
public interface HttpServletResponse extends ServletResponse {
    void addCookie(Cookie cookie);

    boolean containsHeader(String name);

    String encodeURL(String url);

    String encodeRedirectURL(String url);

    @Deprecated
    String encodeUrl(String url);

    @Deprecated
    String encodeRedirectUrl(String url);

    void sendError(int sc, String msg) throws IOException;

    void sendError(int sc) throws IOException;

    void sendRedirect(String location) throws IOException;

    void setDateHeader(String name, long date);

    void addDateHeader(String name, long date);

    void setHeader(String name, String value);

    void addHeader(String name, String value);

    void setIntHeader(String name, int value);

    void addIntHeader(String name, int value);

    void setStatus(int sc);

    @Deprecated
    void setStatus(int sc, String sm);

    int getStatus();

    String getHeader(String name);

    Collection<String> getHeaders(String name);

    Collection<String> getHeaderNames();

    default void setTrailerFields(Supplier<Map<String, String>> supplier) {}

    default Supplier<Map<String, String>> getTrailerFields() {
        return null;
    }

    /*
     * Server status codes; see RFC 2068.
     */

    int SC_CONTINUE = 100;

    int SC_SWITCHING_PROTOCOLS = 101;

    int SC_OK = 200;

    int SC_CREATED = 201;

    int SC_ACCEPTED = 202;

    int SC_NON_AUTHORITATIVE_INFORMATION = 203;

    int SC_NO_CONTENT = 204;

    int SC_RESET_CONTENT = 205;

    int SC_PARTIAL_CONTENT = 206;

    int SC_MULTIPLE_CHOICES = 300;

    int SC_MOVED_PERMANENTLY = 301;

    int SC_MOVED_TEMPORARILY = 302;

    int SC_FOUND = 302;

    int SC_SEE_OTHER = 303;

    int SC_NOT_MODIFIED = 304;

    int SC_USE_PROXY = 305;

    int SC_TEMPORARY_REDIRECT = 307;

    int SC_BAD_REQUEST = 400;

    int SC_UNAUTHORIZED = 401;

    int SC_PAYMENT_REQUIRED = 402;

    int SC_FORBIDDEN = 403;

    int SC_NOT_FOUND = 404;

    int SC_METHOD_NOT_ALLOWED = 405;

    int SC_NOT_ACCEPTABLE = 406;

    int SC_PROXY_AUTHENTICATION_REQUIRED = 407;

    int SC_REQUEST_TIMEOUT = 408;

    int SC_CONFLICT = 409;

    int SC_GONE = 410;

    int SC_LENGTH_REQUIRED = 411;

    int SC_PRECONDITION_FAILED = 412;

    int SC_REQUEST_ENTITY_TOO_LARGE = 413;

    int SC_REQUEST_URI_TOO_LONG = 414;

    int SC_UNSUPPORTED_MEDIA_TYPE = 415;

    int SC_REQUESTED_RANGE_NOT_SATISFIABLE = 416;

    int SC_EXPECTATION_FAILED = 417;

    int SC_INTERNAL_SERVER_ERROR = 500;

    int SC_NOT_IMPLEMENTED = 501;

    int SC_BAD_GATEWAY = 502;

    int SC_SERVICE_UNAVAILABLE = 503;

    int SC_GATEWAY_TIMEOUT = 504;

    int SC_HTTP_VERSION_NOT_SUPPORTED = 505;

    default jakarta.servlet.http.HttpServletResponse toJakartaHttpServletResponse() {
        return new jakarta.servlet.http.HttpServletResponse() {
            @Override
            public String getCharacterEncoding() {
                return HttpServletResponse.this.getCharacterEncoding();
            }

            @Override
            public String getContentType() {
                return HttpServletResponse.this.getContentType();
            }

            @Override
            public jakarta.servlet.ServletOutputStream getOutputStream() throws IOException {
                return HttpServletResponse.this.getOutputStream();
            }

            @Override
            public PrintWriter getWriter() throws IOException {
                return HttpServletResponse.this.getWriter();
            }

            @Override
            public void setCharacterEncoding(String s) {
                HttpServletResponse.this.setCharacterEncoding(s);
            }

            @Override
            public void setContentLength(int i) {
                HttpServletResponse.this.setContentLength(i);
            }

            @Override
            public void setContentLengthLong(long l) {
                HttpServletResponse.this.setContentLengthLong(l);
            }

            @Override
            public void setContentType(String s) {
                HttpServletResponse.this.setContentType(s);
            }

            @Override
            public void setBufferSize(int i) {
                HttpServletResponse.this.setBufferSize(i);
            }

            @Override
            public int getBufferSize() {
                return HttpServletResponse.this.getBufferSize();
            }

            @Override
            public void flushBuffer() throws IOException {
                HttpServletResponse.this.flushBuffer();
            }

            @Override
            public void resetBuffer() {
                HttpServletResponse.this.resetBuffer();
            }

            @Override
            public boolean isCommitted() {
                return HttpServletResponse.this.isCommitted();
            }

            @Override
            public void reset() {
                HttpServletResponse.this.reset();
            }

            @Override
            public void setLocale(Locale locale) {
                HttpServletResponse.this.setLocale(locale);
            }

            @Override
            public Locale getLocale() {
                return HttpServletResponse.this.getLocale();
            }

            @Override
            public void addCookie(jakarta.servlet.http.Cookie cookie) {
                // TODO
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean containsHeader(String s) {
                return HttpServletResponse.this.containsHeader(s);
            }

            @Override
            public String encodeURL(String s) {
                return HttpServletResponse.this.encodeURL(s);
            }

            @Override
            public String encodeRedirectURL(String s) {
                return HttpServletResponse.this.encodeRedirectURL(s);
            }

            @Override
            public String encodeUrl(String s) {
                return HttpServletResponse.this.encodeUrl(s);
            }

            @Override
            public String encodeRedirectUrl(String s) {
                return HttpServletResponse.this.encodeRedirectUrl(s);
            }

            @Override
            public void sendError(int i, String s) throws IOException {
                HttpServletResponse.this.sendError(i, s);
            }

            @Override
            public void sendError(int i) throws IOException {
                HttpServletResponse.this.sendError(i);
            }

            @Override
            public void sendRedirect(String s) throws IOException {
                HttpServletResponse.this.sendRedirect(s);
            }

            @Override
            public void setDateHeader(String s, long l) {
                HttpServletResponse.this.setDateHeader(s, l);
            }

            @Override
            public void addDateHeader(String s, long l) {
                HttpServletResponse.this.addDateHeader(s, l);
            }

            @Override
            public void setHeader(String s, String s1) {
                HttpServletResponse.this.setHeader(s, s1);
            }

            @Override
            public void addHeader(String s, String s1) {
                HttpServletResponse.this.addHeader(s, s1);
            }

            @Override
            public void setIntHeader(String s, int i) {
                HttpServletResponse.this.setIntHeader(s, i);
            }

            @Override
            public void addIntHeader(String s, int i) {
                HttpServletResponse.this.addIntHeader(s, i);
            }

            @Override
            public void setStatus(int i) {
                HttpServletResponse.this.setStatus(i);
            }

            @Override
            public void setStatus(int i, String s) {
                HttpServletResponse.this.setStatus(i, s);
            }

            @Override
            public int getStatus() {
                return HttpServletResponse.this.getStatus();
            }

            @Override
            public String getHeader(String s) {
                return HttpServletResponse.this.getHeader(s);
            }

            @Override
            public Collection<String> getHeaders(String s) {
                return HttpServletResponse.this.getHeaders(s);
            }

            @Override
            public Collection<String> getHeaderNames() {
                return HttpServletResponse.this.getHeaderNames();
            }

            @Override
            public void setTrailerFields(Supplier<Map<String, String>> supplier) {
                HttpServletResponse.this.setTrailerFields(supplier);
            }

            @Override
            public Supplier<Map<String, String>> getTrailerFields() {
                return HttpServletResponse.this.getTrailerFields();
            }
        };
    }

    static HttpServletResponse fromJakartaHttpServletResponse(
            jakarta.servlet.http.HttpServletResponse from) {
        return new HttpServletResponse() {
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
                // TODO
                throw new UnsupportedOperationException();
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
                // TODO
                throw new UnsupportedOperationException();
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
        };
    }
}
