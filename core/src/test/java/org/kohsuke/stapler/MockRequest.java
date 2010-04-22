package org.kohsuke.stapler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpSession;
import javax.servlet.ServletInputStream;
import javax.servlet.RequestDispatcher;
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
}
