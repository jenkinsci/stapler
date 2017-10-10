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
    public String getAuthType() {
        // TODO
        throw new UnsupportedOperationException();
    }

    public Cookie[] getCookies() {
        // TODO
        throw new UnsupportedOperationException();
    }

    public long getDateHeader(String name) {
        // TODO
        throw new UnsupportedOperationException();
    }

    public String getHeader(String name) {
        // TODO
        throw new UnsupportedOperationException();
    }

    public Enumeration getHeaders(String name) {
        // TODO
        throw new UnsupportedOperationException();
    }

    public Enumeration getHeaderNames() {
        // TODO
        throw new UnsupportedOperationException();
    }

    public int getIntHeader(String name) {
        // TODO
        throw new UnsupportedOperationException();
    }

    public String getMethod() {
        // TODO
        throw new UnsupportedOperationException();
    }

    public String getPathInfo() {
        // TODO
        throw new UnsupportedOperationException();
    }

    public String getPathTranslated() {
        // TODO
        throw new UnsupportedOperationException();
    }

    public String getContextPath() {
        // TODO
        throw new UnsupportedOperationException();
    }

    public String getQueryString() {
        // TODO
        throw new UnsupportedOperationException();
    }

    public String getRemoteUser() {
        // TODO
        throw new UnsupportedOperationException();
    }

    public boolean isUserInRole(String role) {
        // TODO
        throw new UnsupportedOperationException();
    }

    public Principal getUserPrincipal() {
        // TODO
        throw new UnsupportedOperationException();
    }

    public String getRequestedSessionId() {
        // TODO
        throw new UnsupportedOperationException();
    }

    public String getRequestURI() {
        return "";
    }

    public StringBuffer getRequestURL() {
        // TODO
        throw new UnsupportedOperationException();
    }

    public String getServletPath() {
        // TODO
        throw new UnsupportedOperationException();
    }

    public HttpSession getSession(boolean create) {
        // TODO
        throw new UnsupportedOperationException();
    }

    public HttpSession getSession() {
        // TODO
        throw new UnsupportedOperationException();
    }

    public boolean isRequestedSessionIdValid() {
        // TODO
        throw new UnsupportedOperationException();
    }

    public boolean isRequestedSessionIdFromCookie() {
        // TODO
        throw new UnsupportedOperationException();
    }

    public boolean isRequestedSessionIdFromURL() {
        // TODO
        throw new UnsupportedOperationException();
    }

    public boolean isRequestedSessionIdFromUrl() {
        // TODO
        throw new UnsupportedOperationException();
    }

    public Object getAttribute(String name) {
        // TODO
        throw new UnsupportedOperationException();
    }

    public Enumeration getAttributeNames() {
        // TODO
        throw new UnsupportedOperationException();
    }

    public String getCharacterEncoding() {
        // TODO
        throw new UnsupportedOperationException();
    }

    public void setCharacterEncoding(String s) throws UnsupportedEncodingException {
        // TODO
        throw new UnsupportedOperationException();
    }

    public int getContentLength() {
        // TODO
        throw new UnsupportedOperationException();
    }

    public String getContentType() {
        // TODO
        throw new UnsupportedOperationException();
    }

    public ServletInputStream getInputStream() throws IOException {
        // TODO
        throw new UnsupportedOperationException();
    }

    public Map<String,String> parameters = new HashMap<String,String>();

    public String getParameter(String name) {
        return parameters.get(name);
    }

    public Enumeration getParameterNames() {
        // TODO
        throw new UnsupportedOperationException();
    }

    public String[] getParameterValues(String name) {
        String v = getParameter(name);
        if (v==null)    return new String[0];
        return new String[]{v};
    }

    public Map getParameterMap() {
        return parameters;
    }

    public String getProtocol() {
        // TODO
        throw new UnsupportedOperationException();
    }

    public String getScheme() {
        // TODO
        throw new UnsupportedOperationException();
    }

    public String getServerName() {
        // TODO
        throw new UnsupportedOperationException();
    }

    public int getServerPort() {
        // TODO
        throw new UnsupportedOperationException();
    }

    public BufferedReader getReader() throws IOException {
        // TODO
        throw new UnsupportedOperationException();
    }

    public String getRemoteAddr() {
        // TODO
        throw new UnsupportedOperationException();
    }

    public String getRemoteHost() {
        // TODO
        throw new UnsupportedOperationException();
    }

    public void setAttribute(String name, Object o) {
        // TODO
        throw new UnsupportedOperationException();
    }

    public void removeAttribute(String name) {
        // TODO
        throw new UnsupportedOperationException();
    }

    public Locale getLocale() {
        // TODO
        throw new UnsupportedOperationException();
    }

    public Enumeration getLocales() {
        // TODO
        throw new UnsupportedOperationException();
    }

    public boolean isSecure() {
        // TODO
        throw new UnsupportedOperationException();
    }

    public RequestDispatcher getRequestDispatcher(String path) {
        // TODO
        throw new UnsupportedOperationException();
    }

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
