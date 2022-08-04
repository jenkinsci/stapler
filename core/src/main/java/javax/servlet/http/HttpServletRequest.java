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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;
import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

public interface HttpServletRequest extends ServletRequest {
    String BASIC_AUTH = "BASIC";

    String FORM_AUTH = "FORM";

    String CLIENT_CERT_AUTH = "CLIENT_CERT";

    String DIGEST_AUTH = "DIGEST";

    String getAuthType();

    Cookie[] getCookies();

    long getDateHeader(String name);

    String getHeader(String name);

    Enumeration<String> getHeaders(String name);

    Enumeration<String> getHeaderNames();

    int getIntHeader(String name);

    default HttpServletMapping getHttpServletMapping() {
        return new HttpServletMapping() {
            @Override
            public String getMatchValue() {
                return "";
            }

            @Override
            public String getPattern() {
                return "";
            }

            @Override
            public String getServletName() {
                return "";
            }

            @Override
            public MappingMatch getMappingMatch() {
                return null;
            }

            @Override
            public String toString() {
                return "MappingImpl{"
                        + "matchValue="
                        + getMatchValue()
                        + ", pattern="
                        + getPattern()
                        + ", servletName="
                        + getServletName()
                        + ", mappingMatch="
                        + getMappingMatch()
                        + "} HttpServletRequest {"
                        + HttpServletRequest.this.toString()
                        + '}';
            }
        };
    }

    String getMethod();

    String getPathInfo();

    String getPathTranslated();

    default PushBuilder newPushBuilder() {
        return null;
    }

    String getContextPath();

    String getQueryString();

    String getRemoteUser();

    boolean isUserInRole(String role);

    java.security.Principal getUserPrincipal();

    String getRequestedSessionId();

    String getRequestURI();

    StringBuffer getRequestURL();

    String getServletPath();

    HttpSession getSession(boolean create);

    HttpSession getSession();

    String changeSessionId();

    boolean isRequestedSessionIdValid();

    boolean isRequestedSessionIdFromCookie();

    boolean isRequestedSessionIdFromURL();

    @Deprecated
    boolean isRequestedSessionIdFromUrl();

    boolean authenticate(HttpServletResponse response) throws IOException, ServletException;

    void login(String username, String password) throws ServletException;

    void logout() throws ServletException;

    Collection<Part> getParts() throws IOException, ServletException;

    Part getPart(String name) throws IOException, ServletException;

    <T extends HttpUpgradeHandler> T upgrade(Class<T> handlerClass)
            throws IOException, ServletException;

    default Map<String, String> getTrailerFields() {
        return Collections.emptyMap();
    }

    default boolean isTrailerFieldsReady() {
        return true;
    }

    default jakarta.servlet.http.HttpServletRequest toJakartaHttpServletRequest() {
        return new jakarta.servlet.http.HttpServletRequest() {
            @Override
            public Object getAttribute(String s) {
                return HttpServletRequest.this.getAttribute(s);
            }

            @Override
            public Enumeration<String> getAttributeNames() {
                return HttpServletRequest.this.getAttributeNames();
            }

            @Override
            public String getCharacterEncoding() {
                return HttpServletRequest.this.getCharacterEncoding();
            }

            @Override
            public void setCharacterEncoding(String s) throws UnsupportedEncodingException {
                HttpServletRequest.this.setCharacterEncoding(s);
            }

            @Override
            public int getContentLength() {
                return HttpServletRequest.this.getContentLength();
            }

            @Override
            public long getContentLengthLong() {
                return HttpServletRequest.this.getContentLengthLong();
            }

            @Override
            public String getContentType() {
                return HttpServletRequest.this.getContentType();
            }

            @Override
            public jakarta.servlet.ServletInputStream getInputStream() throws IOException {
                return HttpServletRequest.this.getInputStream();
            }

            @Override
            public String getParameter(String s) {
                return HttpServletRequest.this.getParameter(s);
            }

            @Override
            public Enumeration<String> getParameterNames() {
                return HttpServletRequest.this.getParameterNames();
            }

            @Override
            public String[] getParameterValues(String s) {
                return HttpServletRequest.this.getParameterValues(s);
            }

            @Override
            public Map<String, String[]> getParameterMap() {
                return HttpServletRequest.this.getParameterMap();
            }

            @Override
            public String getProtocol() {
                return HttpServletRequest.this.getProtocol();
            }

            @Override
            public String getScheme() {
                return HttpServletRequest.this.getScheme();
            }

            @Override
            public String getServerName() {
                return HttpServletRequest.this.getServerName();
            }

            @Override
            public int getServerPort() {
                return HttpServletRequest.this.getServerPort();
            }

            @Override
            public BufferedReader getReader() throws IOException {
                return HttpServletRequest.this.getReader();
            }

            @Override
            public String getRemoteAddr() {
                return HttpServletRequest.this.getRemoteAddr();
            }

            @Override
            public String getRemoteHost() {
                return HttpServletRequest.this.getRemoteHost();
            }

            @Override
            public void setAttribute(String s, Object o) {
                HttpServletRequest.this.setAttribute(s, o);
            }

            @Override
            public void removeAttribute(String s) {
                HttpServletRequest.this.removeAttribute(s);
            }

            @Override
            public Locale getLocale() {
                return HttpServletRequest.this.getLocale();
            }

            @Override
            public Enumeration<Locale> getLocales() {
                return HttpServletRequest.this.getLocales();
            }

            @Override
            public boolean isSecure() {
                return HttpServletRequest.this.isSecure();
            }

            @Override
            public jakarta.servlet.RequestDispatcher getRequestDispatcher(String s) {
                return HttpServletRequest.this.getRequestDispatcher(s).toJakartaRequestDispatcher();
            }

            @Override
            public String getRealPath(String s) {
                return HttpServletRequest.this.getRealPath(s);
            }

            @Override
            public int getRemotePort() {
                return HttpServletRequest.this.getRemotePort();
            }

            @Override
            public String getLocalName() {
                return HttpServletRequest.this.getLocalName();
            }

            @Override
            public String getLocalAddr() {
                return HttpServletRequest.this.getLocalAddr();
            }

            @Override
            public int getLocalPort() {
                return HttpServletRequest.this.getLocalPort();
            }

            @Override
            public jakarta.servlet.ServletContext getServletContext() {
                return HttpServletRequest.this.getServletContext().toJakartaServletContext();
            }

            @Override
            public jakarta.servlet.AsyncContext startAsync() throws IllegalStateException {
                // TODO
                throw new UnsupportedOperationException();
            }

            @Override
            public jakarta.servlet.AsyncContext startAsync(
                    jakarta.servlet.ServletRequest servletRequest,
                    jakarta.servlet.ServletResponse servletResponse)
                    throws IllegalStateException {
                // TODO
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean isAsyncStarted() {
                return HttpServletRequest.this.isAsyncStarted();
            }

            @Override
            public boolean isAsyncSupported() {
                return HttpServletRequest.this.isAsyncSupported();
            }

            @Override
            public jakarta.servlet.AsyncContext getAsyncContext() {
                // TODO
                throw new UnsupportedOperationException();
            }

            @Override
            public jakarta.servlet.DispatcherType getDispatcherType() {
                return HttpServletRequest.this.getDispatcherType().toJakartaDispatcherType();
            }

            @Override
            public String getAuthType() {
                return HttpServletRequest.this.getAuthType();
            }

            @Override
            public jakarta.servlet.http.Cookie[] getCookies() {
                // TODO
                throw new UnsupportedOperationException();
            }

            @Override
            public long getDateHeader(String s) {
                return HttpServletRequest.this.getDateHeader(s);
            }

            @Override
            public String getHeader(String s) {
                return HttpServletRequest.this.getHeader(s);
            }

            @Override
            public Enumeration<String> getHeaders(String s) {
                return HttpServletRequest.this.getHeaders(s);
            }

            @Override
            public Enumeration<String> getHeaderNames() {
                return HttpServletRequest.this.getHeaderNames();
            }

            @Override
            public int getIntHeader(String s) {
                return HttpServletRequest.this.getIntHeader(s);
            }

            @Override
            public jakarta.servlet.http.HttpServletMapping getHttpServletMapping() {
                return HttpServletRequest.this
                        .getHttpServletMapping()
                        .toJakartaHttpServletMapping();
            }

            @Override
            public String getMethod() {
                return HttpServletRequest.this.getMethod();
            }

            @Override
            public String getPathInfo() {
                return HttpServletRequest.this.getPathInfo();
            }

            @Override
            public String getPathTranslated() {
                return HttpServletRequest.this.getPathTranslated();
            }

            @Override
            public jakarta.servlet.http.PushBuilder newPushBuilder() {
                // TODO
                throw new UnsupportedOperationException();
            }

            @Override
            public String getContextPath() {
                return HttpServletRequest.this.getContextPath();
            }

            @Override
            public String getQueryString() {
                return HttpServletRequest.this.getQueryString();
            }

            @Override
            public String getRemoteUser() {
                return HttpServletRequest.this.getRemoteUser();
            }

            @Override
            public boolean isUserInRole(String s) {
                return HttpServletRequest.this.isUserInRole(s);
            }

            @Override
            public Principal getUserPrincipal() {
                return HttpServletRequest.this.getUserPrincipal();
            }

            @Override
            public String getRequestedSessionId() {
                return HttpServletRequest.this.getRequestedSessionId();
            }

            @Override
            public String getRequestURI() {
                return HttpServletRequest.this.getRequestURI();
            }

            @Override
            public StringBuffer getRequestURL() {
                return HttpServletRequest.this.getRequestURL();
            }

            @Override
            public String getServletPath() {
                return HttpServletRequest.this.getServletPath();
            }

            @Override
            public jakarta.servlet.http.HttpSession getSession(boolean b) {
                return HttpServletRequest.this.getSession(b).toJakartaHttpSession();
            }

            @Override
            public jakarta.servlet.http.HttpSession getSession() {
                return HttpServletRequest.this.getSession().toJakartaHttpSession();
            }

            @Override
            public String changeSessionId() {
                return HttpServletRequest.this.changeSessionId();
            }

            @Override
            public boolean isRequestedSessionIdValid() {
                return HttpServletRequest.this.isRequestedSessionIdValid();
            }

            @Override
            public boolean isRequestedSessionIdFromCookie() {
                return HttpServletRequest.this.isRequestedSessionIdFromCookie();
            }

            @Override
            public boolean isRequestedSessionIdFromURL() {
                return HttpServletRequest.this.isRequestedSessionIdFromURL();
            }

            @Override
            public boolean isRequestedSessionIdFromUrl() {
                return HttpServletRequest.this.isRequestedSessionIdFromUrl();
            }

            @Override
            public boolean authenticate(
                    jakarta.servlet.http.HttpServletResponse httpServletResponse)
                    throws IOException, jakarta.servlet.ServletException {
                try {
                    return HttpServletRequest.this.authenticate(
                            HttpServletResponse.fromJakartaHttpServletResponse(
                                    httpServletResponse));
                } catch (ServletException e) {
                    throw new jakarta.servlet.ServletException(e);
                }
            }

            @Override
            public void login(String s, String s1) throws jakarta.servlet.ServletException {
                try {
                    HttpServletRequest.this.login(s, s1);
                } catch (ServletException e) {
                    throw new jakarta.servlet.ServletException(e);
                }
            }

            @Override
            public void logout() throws jakarta.servlet.ServletException {
                try {
                    HttpServletRequest.this.logout();
                } catch (ServletException e) {
                    throw new jakarta.servlet.ServletException(e);
                }
            }

            @Override
            public Collection<jakarta.servlet.http.Part> getParts()
                    throws IOException, jakarta.servlet.ServletException {
                // TODO
                throw new UnsupportedOperationException();
            }

            @Override
            public jakarta.servlet.http.Part getPart(String s)
                    throws IOException, jakarta.servlet.ServletException {
                // TODO
                throw new UnsupportedOperationException();
            }

            @Override
            public <T extends jakarta.servlet.http.HttpUpgradeHandler> T upgrade(Class<T> aClass)
                    throws IOException, jakarta.servlet.ServletException {
                // TODO
                throw new UnsupportedOperationException();
            }

            @Override
            public Map<String, String> getTrailerFields() {
                return HttpServletRequest.this.getTrailerFields();
            }

            @Override
            public boolean isTrailerFieldsReady() {
                return HttpServletRequest.this.isTrailerFieldsReady();
            }
        };
    }

    static HttpServletRequest fromJakartaHttpServletRequest(
            jakarta.servlet.http.HttpServletRequest from) {
        return new HttpServletRequest() {
            @Override
            public Object getAttribute(String name) {
                return from.getAttribute(name);
            }

            @Override
            public Enumeration<String> getAttributeNames() {
                return from.getAttributeNames();
            }

            @Override
            public String getCharacterEncoding() {
                return from.getCharacterEncoding();
            }

            @Override
            public void setCharacterEncoding(String env) throws UnsupportedEncodingException {
                from.setCharacterEncoding(env);
            }

            @Override
            public int getContentLength() {
                return from.getContentLength();
            }

            @Override
            public long getContentLengthLong() {
                return from.getContentLengthLong();
            }

            @Override
            public String getContentType() {
                return from.getContentType();
            }

            @Override
            public ServletInputStream getInputStream() throws IOException {
                // TODO
                throw new UnsupportedOperationException();
            }

            @Override
            public String getParameter(String name) {
                return from.getParameter(name);
            }

            @Override
            public Enumeration<String> getParameterNames() {
                return from.getParameterNames();
            }

            @Override
            public String[] getParameterValues(String name) {
                return from.getParameterValues(name);
            }

            @Override
            public Map<String, String[]> getParameterMap() {
                return from.getParameterMap();
            }

            @Override
            public String getProtocol() {
                return from.getProtocol();
            }

            @Override
            public String getScheme() {
                return from.getScheme();
            }

            @Override
            public String getServerName() {
                return from.getServerName();
            }

            @Override
            public int getServerPort() {
                return from.getServerPort();
            }

            @Override
            public BufferedReader getReader() throws IOException {
                return from.getReader();
            }

            @Override
            public String getRemoteAddr() {
                return from.getRemoteAddr();
            }

            @Override
            public String getRemoteHost() {
                return from.getRemoteHost();
            }

            @Override
            public void setAttribute(String name, Object o) {
                from.setAttribute(name, o);
            }

            @Override
            public void removeAttribute(String name) {
                from.removeAttribute(name);
            }

            @Override
            public Locale getLocale() {
                return from.getLocale();
            }

            @Override
            public Enumeration<Locale> getLocales() {
                return from.getLocales();
            }

            @Override
            public boolean isSecure() {
                return from.isSecure();
            }

            @Override
            public RequestDispatcher getRequestDispatcher(String path) {
                return RequestDispatcher.fromJakartaRequestDispatcher(
                        from.getRequestDispatcher(path));
            }

            @Override
            public String getRealPath(String path) {
                return from.getRealPath(path);
            }

            @Override
            public int getRemotePort() {
                return from.getRemotePort();
            }

            @Override
            public String getLocalName() {
                return from.getLocalName();
            }

            @Override
            public String getLocalAddr() {
                return from.getLocalAddr();
            }

            @Override
            public int getLocalPort() {
                return from.getLocalPort();
            }

            @Override
            public ServletContext getServletContext() {
                return ServletContext.fromJakartServletContext(from.getServletContext());
            }

            @Override
            public AsyncContext startAsync() throws IllegalStateException {
                // TODO
                throw new UnsupportedOperationException();
            }

            @Override
            public AsyncContext startAsync(
                    ServletRequest servletRequest, ServletResponse servletResponse)
                    throws IllegalStateException {
                // TODO
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean isAsyncStarted() {
                return from.isAsyncStarted();
            }

            @Override
            public boolean isAsyncSupported() {
                return from.isAsyncSupported();
            }

            @Override
            public AsyncContext getAsyncContext() {
                // TODO
                throw new UnsupportedOperationException();
            }

            @Override
            public DispatcherType getDispatcherType() {
                return DispatcherType.fromJakartaDispatcherType(from.getDispatcherType());
            }

            @Override
            public String getAuthType() {
                return from.getAuthType();
            }

            @Override
            public Cookie[] getCookies() {
                // TODO
                throw new UnsupportedOperationException();
            }

            @Override
            public long getDateHeader(String name) {
                return from.getDateHeader(name);
            }

            @Override
            public String getHeader(String name) {
                return from.getHeader(name);
            }

            @Override
            public Enumeration<String> getHeaders(String name) {
                return from.getHeaders(name);
            }

            @Override
            public Enumeration<String> getHeaderNames() {
                return from.getHeaderNames();
            }

            @Override
            public int getIntHeader(String name) {
                return from.getIntHeader(name);
            }

            @Override
            public HttpServletMapping getHttpServletMapping() {
                return HttpServletMapping.fromJakartaHttpServletMapping(
                        from.getHttpServletMapping());
            }

            @Override
            public String getMethod() {
                return from.getMethod();
            }

            @Override
            public String getPathInfo() {
                return from.getPathInfo();
            }

            @Override
            public String getPathTranslated() {
                return from.getPathTranslated();
            }

            @Override
            public PushBuilder newPushBuilder() {
                // TODO
                throw new UnsupportedOperationException();
            }

            @Override
            public String getContextPath() {
                return from.getContextPath();
            }

            @Override
            public String getQueryString() {
                return from.getQueryString();
            }

            @Override
            public String getRemoteUser() {
                return from.getRemoteUser();
            }

            @Override
            public boolean isUserInRole(String role) {
                return from.isUserInRole(role);
            }

            @Override
            public Principal getUserPrincipal() {
                return from.getUserPrincipal();
            }

            @Override
            public String getRequestedSessionId() {
                return from.getRequestedSessionId();
            }

            @Override
            public String getRequestURI() {
                return from.getRequestURI();
            }

            @Override
            public StringBuffer getRequestURL() {
                return from.getRequestURL();
            }

            @Override
            public String getServletPath() {
                return from.getServletPath();
            }

            @Override
            public HttpSession getSession(boolean create) {
                return HttpSession.fromJakartaHttpSession(from.getSession(create));
            }

            @Override
            public HttpSession getSession() {
                return HttpSession.fromJakartaHttpSession(from.getSession());
            }

            @Override
            public String changeSessionId() {
                return from.changeSessionId();
            }

            @Override
            public boolean isRequestedSessionIdValid() {
                return from.isRequestedSessionIdValid();
            }

            @Override
            public boolean isRequestedSessionIdFromCookie() {
                return from.isRequestedSessionIdFromCookie();
            }

            @Override
            public boolean isRequestedSessionIdFromURL() {
                return from.isRequestedSessionIdFromURL();
            }

            @Override
            public boolean isRequestedSessionIdFromUrl() {
                return from.isRequestedSessionIdFromUrl();
            }

            @Override
            public boolean authenticate(HttpServletResponse response)
                    throws IOException, ServletException {
                try {
                    return from.authenticate(response.toJakartaHttpServletResponse());
                } catch (jakarta.servlet.ServletException e) {
                    throw new ServletException(e);
                }
            }

            @Override
            public void login(String username, String password) throws ServletException {
                try {
                    from.login(username, password);
                } catch (jakarta.servlet.ServletException e) {
                    throw new ServletException(e);
                }
            }

            @Override
            public void logout() throws ServletException {
                try {
                    from.logout();
                } catch (jakarta.servlet.ServletException e) {
                    throw new ServletException(e);
                }
            }

            @Override
            public Collection<Part> getParts() throws IOException, ServletException {
                // TODO
                throw new UnsupportedOperationException();
            }

            @Override
            public Part getPart(String name) throws IOException, ServletException {
                // TODO
                throw new UnsupportedEncodingException();
            }

            @Override
            public <T extends HttpUpgradeHandler> T upgrade(Class<T> handlerClass)
                    throws IOException, ServletException {
                // TODO
                throw new UnsupportedOperationException();
            }

            @Override
            public Map<String, String> getTrailerFields() {
                return from.getTrailerFields();
            }

            @Override
            public boolean isTrailerFieldsReady() {
                return from.isTrailerFieldsReady();
            }

            @Override
            public jakarta.servlet.ServletRequest toJakartaServletRequest() {
                return from;
            }

            @Override
            public jakarta.servlet.http.HttpServletRequest toJakartaHttpServletRequest() {
                return from;
            }
        };
    }
}
