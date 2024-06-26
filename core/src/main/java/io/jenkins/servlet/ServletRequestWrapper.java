package io.jenkins.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

public class ServletRequestWrapper {
    public static jakarta.servlet.ServletRequest toJakartaServletRequest(ServletRequest from) {
        if (from instanceof JavaxServletRequestWrapper javax) {
            return javax.toJakartaServletRequest();
        }
        return new JakartaServletRequestWrapperImpl(from);
    }

    public static ServletRequest fromJakartaServletRequest(jakarta.servlet.ServletRequest from) {
        if (from instanceof JakartaServletRequestWrapper jakarta) {
            return jakarta.toJavaxServletRequest();
        }
        return new JavaxServletRequestWrapperImpl(from);
    }

    public interface JakartaServletRequestWrapper {
        ServletRequest toJavaxServletRequest();
    }

    private static class JakartaServletRequestWrapperImpl
            implements jakarta.servlet.ServletRequest, JakartaServletRequestWrapper {
        private final ServletRequest from;

        JakartaServletRequestWrapperImpl(ServletRequest from) {
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
            return RequestDispatcherWrapper.toJakartaRequestDispatcher(from.getRequestDispatcher(path));
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
                    fromJakartaServletRequest(servletRequest),
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
        public ServletRequest toJavaxServletRequest() {
            return from;
        }
    }

    public interface JavaxServletRequestWrapper {
        jakarta.servlet.ServletRequest toJakartaServletRequest();
    }

    private static class JavaxServletRequestWrapperImpl implements ServletRequest, JavaxServletRequestWrapper {
        private final jakarta.servlet.ServletRequest from;

        JavaxServletRequestWrapperImpl(jakarta.servlet.ServletRequest from) {
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
            return RequestDispatcherWrapper.fromJakartaRequestDispatcher(from.getRequestDispatcher(path));
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
        public jakarta.servlet.ServletRequest toJakartaServletRequest() {
            return from;
        }
    }
}
