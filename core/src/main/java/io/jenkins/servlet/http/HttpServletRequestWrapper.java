package io.jenkins.servlet.http;

import io.jenkins.servlet.AsyncContextWrapper;
import io.jenkins.servlet.DispatcherTypeWrapper;
import io.jenkins.servlet.RequestDispatcherWrapper;
import io.jenkins.servlet.ServletContextWrapper;
import io.jenkins.servlet.ServletExceptionWrapper;
import io.jenkins.servlet.ServletInputStreamWrapper;
import io.jenkins.servlet.ServletRequestWrapper;
import io.jenkins.servlet.ServletResponseWrapper;
import jakarta.servlet.ServletConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletMapping;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpUpgradeHandler;
import javax.servlet.http.Part;
import javax.servlet.http.PushBuilder;

public class HttpServletRequestWrapper {

    public static jakarta.servlet.http.HttpServletRequest toJakartaHttpServletRequest(HttpServletRequest from) {
        if (from instanceof JavaxHttpServletRequestWrapper javax) {
            return javax.toJakartaHttpServletRequest();
        }
        return new JakartaHttpServletRequestWrapperImpl(from);
    }

    public static HttpServletRequest fromJakartaHttpServletRequest(jakarta.servlet.http.HttpServletRequest from) {
        if (from instanceof JakartaHttpServletRequestWrapper jakarta) {
            return jakarta.toJavaxHttpServletRequest();
        }
        return new JavaxHttpServletRequestWrapperImpl(from);
    }

    public interface JakartaHttpServletRequestWrapper {
        HttpServletRequest toJavaxHttpServletRequest();
    }

    private static class JakartaHttpServletRequestWrapperImpl
            implements jakarta.servlet.http.HttpServletRequest, ServletRequestWrapper.JakartaServletRequestWrapper {
        private final HttpServletRequest from;

        JakartaHttpServletRequestWrapperImpl(HttpServletRequest from) {
            this.from = Objects.requireNonNull(from);
        }

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
        public jakarta.servlet.ServletInputStream getInputStream() throws IOException {
            return ServletInputStreamWrapper.toJakartaServletInputStream(from.getInputStream());
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
        public jakarta.servlet.RequestDispatcher getRequestDispatcher(String path) {
            RequestDispatcher requestDispatcher = from.getRequestDispatcher(path);
            return requestDispatcher != null
                    ? RequestDispatcherWrapper.toJakartaRequestDispatcher(requestDispatcher)
                    : null;
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
        public jakarta.servlet.ServletContext getServletContext() {
            return ServletContextWrapper.toJakartaServletContext(from.getServletContext());
        }

        @Override
        public jakarta.servlet.AsyncContext startAsync() {
            return AsyncContextWrapper.toJakartaAsyncContext(from.startAsync());
        }

        @Override
        public jakarta.servlet.AsyncContext startAsync(
                jakarta.servlet.ServletRequest servletRequest, jakarta.servlet.ServletResponse servletResponse) {
            return AsyncContextWrapper.toJakartaAsyncContext(from.startAsync(
                    ServletRequestWrapper.fromJakartaServletRequest(servletRequest),
                    ServletResponseWrapper.fromJakartaServletResponse(servletResponse)));
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
        public jakarta.servlet.AsyncContext getAsyncContext() {
            return AsyncContextWrapper.toJakartaAsyncContext(from.getAsyncContext());
        }

        @Override
        public jakarta.servlet.DispatcherType getDispatcherType() {
            return DispatcherTypeWrapper.toJakartaDispatcherType(from.getDispatcherType());
        }

        @Override
        public String getRequestId() {
            // TODO implement this
            throw new UnsupportedOperationException();
        }

        @Override
        public String getProtocolRequestId() {
            // TODO implement this
            throw new UnsupportedOperationException();
        }

        @Override
        public ServletConnection getServletConnection() {
            // TODO implement this
            throw new UnsupportedOperationException();
        }

        @Override
        public String getAuthType() {
            return from.getAuthType();
        }

        @Override
        public jakarta.servlet.http.Cookie[] getCookies() {
            Cookie[] cookies = from.getCookies();
            if (cookies == null) {
                return null;
            }
            return Stream.of(cookies)
                    .map(CookieWrapper::toJakartaServletHttpCookie)
                    .toArray(jakarta.servlet.http.Cookie[]::new);
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
        public jakarta.servlet.http.HttpServletMapping getHttpServletMapping() {
            return HttpServletMappingWrapper.toJakartaHttpServletMapping(from.getHttpServletMapping());
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
        public jakarta.servlet.http.PushBuilder newPushBuilder() {
            // TODO implement this
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
        public jakarta.servlet.http.HttpSession getSession(boolean create) {
            HttpSession session = from.getSession(create);
            return session != null ? HttpSessionWrapper.toJakartaHttpSession(session) : null;
        }

        @Override
        public jakarta.servlet.http.HttpSession getSession() {
            HttpSession session = from.getSession();
            return session != null ? HttpSessionWrapper.toJakartaHttpSession(session) : null;
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
        public boolean authenticate(jakarta.servlet.http.HttpServletResponse response)
                throws IOException, jakarta.servlet.ServletException {
            try {
                return from.authenticate(HttpServletResponseWrapper.fromJakartaHttpServletResponse(response));
            } catch (ServletException e) {
                throw ServletExceptionWrapper.toJakartaServletException(e);
            }
        }

        @Override
        public void login(String username, String password) throws jakarta.servlet.ServletException {
            try {
                from.login(username, password);
            } catch (ServletException e) {
                throw ServletExceptionWrapper.toJakartaServletException(e);
            }
        }

        @Override
        public void logout() throws jakarta.servlet.ServletException {
            try {
                from.logout();
            } catch (ServletException e) {
                throw ServletExceptionWrapper.toJakartaServletException(e);
            }
        }

        @Override
        public Collection<jakarta.servlet.http.Part> getParts() throws IOException, jakarta.servlet.ServletException {
            try {
                return from.getParts().stream()
                        .map(PartWrapper::toJakartaPart)
                        .collect(Collectors.toCollection(ArrayList::new));
            } catch (ServletException e) {
                throw ServletExceptionWrapper.toJakartaServletException(e);
            }
        }

        @Override
        public jakarta.servlet.http.Part getPart(String name) throws IOException, jakarta.servlet.ServletException {
            try {
                return PartWrapper.toJakartaPart(from.getPart(name));
            } catch (ServletException e) {
                throw ServletExceptionWrapper.toJakartaServletException(e);
            }
        }

        @Override
        public <T extends jakarta.servlet.http.HttpUpgradeHandler> T upgrade(Class<T> handlerClass) {
            // TODO implement this
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
        public HttpServletRequest toJavaxServletRequest() {
            return from;
        }
    }

    public interface JavaxHttpServletRequestWrapper {
        jakarta.servlet.http.HttpServletRequest toJakartaHttpServletRequest();
    }

    private static class JavaxHttpServletRequestWrapperImpl
            implements HttpServletRequest, ServletRequestWrapper.JavaxServletRequestWrapper {
        private final jakarta.servlet.http.HttpServletRequest from;

        JavaxHttpServletRequestWrapperImpl(jakarta.servlet.http.HttpServletRequest from) {
            this.from = Objects.requireNonNull(from);
        }

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
            return ServletInputStreamWrapper.fromJakartaServletInputStream(from.getInputStream());
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
            jakarta.servlet.RequestDispatcher requestDispatcher = from.getRequestDispatcher(path);
            return requestDispatcher != null
                    ? RequestDispatcherWrapper.fromJakartaRequestDispatcher(requestDispatcher)
                    : null;
        }

        @Override
        public String getRealPath(String path) {
            // TODO implement this
            throw new UnsupportedOperationException();
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
            return ServletContextWrapper.fromJakartServletContext(from.getServletContext());
        }

        @Override
        public AsyncContext startAsync() {
            return AsyncContextWrapper.fromJakartaAsyncContext(from.startAsync());
        }

        @Override
        public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse) {
            return AsyncContextWrapper.fromJakartaAsyncContext(from.startAsync(
                    ServletRequestWrapper.toJakartaServletRequest(servletRequest),
                    ServletResponseWrapper.toJakartaServletResponse(servletResponse)));
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
            return AsyncContextWrapper.fromJakartaAsyncContext(from.getAsyncContext());
        }

        @Override
        public DispatcherType getDispatcherType() {
            return DispatcherTypeWrapper.fromJakartaDispatcherType(from.getDispatcherType());
        }

        @Override
        public String getAuthType() {
            return from.getAuthType();
        }

        @Override
        public Cookie[] getCookies() {
            jakarta.servlet.http.Cookie[] cookies = from.getCookies();
            if (cookies == null) {
                return null;
            }
            return Stream.of(cookies)
                    .map(CookieWrapper::fromJakartaServletHttpCookie)
                    .toArray(Cookie[]::new);
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
            return HttpServletMappingWrapper.fromJakartaHttpServletMapping(from.getHttpServletMapping());
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
            // TODO implement this
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
            jakarta.servlet.http.HttpSession session = from.getSession(create);
            return session != null ? HttpSessionWrapper.fromJakartaHttpSession(session) : null;
        }

        @Override
        public HttpSession getSession() {
            jakarta.servlet.http.HttpSession session = from.getSession();
            return session != null ? HttpSessionWrapper.fromJakartaHttpSession(session) : null;
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
            // TODO implement this
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean authenticate(HttpServletResponse response) throws IOException, ServletException {
            try {
                return from.authenticate(HttpServletResponseWrapper.toJakartaHttpServletResponse(response));
            } catch (jakarta.servlet.ServletException e) {
                throw ServletExceptionWrapper.fromJakartaServletException(e);
            }
        }

        @Override
        public void login(String username, String password) throws ServletException {
            try {
                from.login(username, password);
            } catch (jakarta.servlet.ServletException e) {
                throw ServletExceptionWrapper.fromJakartaServletException(e);
            }
        }

        @Override
        public void logout() throws ServletException {
            try {
                from.logout();
            } catch (jakarta.servlet.ServletException e) {
                throw ServletExceptionWrapper.fromJakartaServletException(e);
            }
        }

        @Override
        public Collection<Part> getParts() throws IOException, ServletException {
            try {
                return from.getParts().stream()
                        .map(PartWrapper::fromJakartaPart)
                        .collect(Collectors.toCollection(ArrayList::new));
            } catch (jakarta.servlet.ServletException e) {
                throw ServletExceptionWrapper.fromJakartaServletException(e);
            }
        }

        @Override
        public Part getPart(String name) throws IOException, ServletException {
            try {
                return PartWrapper.fromJakartaPart(from.getPart(name));
            } catch (jakarta.servlet.ServletException e) {
                throw ServletExceptionWrapper.fromJakartaServletException(e);
            }
        }

        @Override
        public <T extends HttpUpgradeHandler> T upgrade(Class<T> handlerClass) {
            // TODO implement this
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
        public jakarta.servlet.http.HttpServletRequest toJakartaServletRequest() {
            return from;
        }
    }
}
