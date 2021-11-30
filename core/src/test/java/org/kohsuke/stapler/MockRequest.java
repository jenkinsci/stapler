package org.kohsuke.stapler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpUpgradeHandler;
import javax.servlet.http.Part;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Locale;
import java.security.Principal;
import java.io.UnsupportedEncodingException;
import java.io.IOException;
import java.io.BufferedReader;

/**
 * @author Kohsuke Kawaguchi
 */
public class MockRequest implements HttpServletRequest {
    @Override
    public String getAuthType() {
        // TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public Cookie[] getCookies() {
        // TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public long getDateHeader(String name) {
        // TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public String getHeader(String name) {
        // TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public Enumeration getHeaders(String name) {
        // TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public Enumeration getHeaderNames() {
        // TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public int getIntHeader(String name) {
        // TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public String getMethod() {
        // TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public String getPathInfo() {
        // TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public String getPathTranslated() {
        // TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public String getContextPath() {
        // TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public String getQueryString() {
        // TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public String getRemoteUser() {
        // TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isUserInRole(String role) {
        // TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public Principal getUserPrincipal() {
        // TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public String getRequestedSessionId() {
        // TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public String getRequestURI() {
        return "";
    }

    @Override
    public StringBuffer getRequestURL() {
        // TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public String getServletPath() {
        // TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public HttpSession getSession(boolean create) {
        // TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public HttpSession getSession() {
        // TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isRequestedSessionIdValid() {
        // TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isRequestedSessionIdFromCookie() {
        // TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isRequestedSessionIdFromURL() {
        // TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isRequestedSessionIdFromUrl() {
        // TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public Object getAttribute(String name) {
        // TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public Enumeration getAttributeNames() {
        // TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public String getCharacterEncoding() {
        // TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public void setCharacterEncoding(String s) throws UnsupportedEncodingException {
        // TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public int getContentLength() {
        // TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public String getContentType() {
        // TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        // TODO
        throw new UnsupportedOperationException();
    }

    public Map<String,String> parameters = new HashMap<>();

    @Override
    public String getParameter(String name) {
        return parameters.get(name);
    }

    @Override
    public Enumeration getParameterNames() {
        // TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public String[] getParameterValues(String name) {
        String v = getParameter(name);
        if (v==null)    return new String[0];
        return new String[]{v};
    }

    @Override
    public Map getParameterMap() {
        return parameters;
    }

    @Override
    public String getProtocol() {
        // TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public String getScheme() {
        // TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public String getServerName() {
        // TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public int getServerPort() {
        // TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public BufferedReader getReader() throws IOException {
        // TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public String getRemoteAddr() {
        // TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public String getRemoteHost() {
        // TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public void setAttribute(String name, Object o) {
        // TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeAttribute(String name) {
        // TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public Locale getLocale() {
        // TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public Enumeration getLocales() {
        // TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isSecure() {
        // TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public RequestDispatcher getRequestDispatcher(String path) {
        // TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public String getRealPath(String path) {
        // TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public long getContentLengthLong() {
        // TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public int getRemotePort() {
        // TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public String getLocalName() {
        // TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public String getLocalAddr() {
        // TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public int getLocalPort() {
        // TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public ServletContext getServletContext() {
        // TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public AsyncContext startAsync() throws IllegalStateException {
        // TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse)
            throws IllegalStateException {
        // TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isAsyncStarted() {
        // TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isAsyncSupported() {
        // TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public AsyncContext getAsyncContext() {
        // TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public DispatcherType getDispatcherType() {
        // TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public String changeSessionId() {
        // TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean authenticate(HttpServletResponse response) throws IOException, ServletException {
        // TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public void login(String username, String password) throws ServletException {
        // TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public void logout() throws ServletException {
        // TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<Part> getParts() throws IOException, ServletException {
        // TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public Part getPart(String name) throws IOException, ServletException {
        // TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public <T extends HttpUpgradeHandler> T upgrade(Class<T> handlerClass) throws IOException, ServletException {
        // TODO
        throw new UnsupportedOperationException();
    }
}
