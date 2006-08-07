package org.kohsuke.stapler.subloader;

import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Set;

/**
 * {@link ServletContext} that simply delegates to another {@link ServletContext}.
 *
 * @author Kohsuke Kawaguchi
 */
public class FilterServletContext implements ServletContext {
    protected final ServletContext context;

    public FilterServletContext(ServletContext context) {
        this.context = context;
    }

    public ServletContext getContext(String s) {
        return context.getContext(s);
    }

    public int getMajorVersion() {
        return context.getMajorVersion();
    }

    public int getMinorVersion() {
        return context.getMinorVersion();
    }

    public String getMimeType(String s) {
        return context.getMimeType(s);
    }

    public Set getResourcePaths(String s) {
        return context.getResourcePaths(s);
    }

    public URL getResource(String s) throws MalformedURLException {
        return context.getResource(s);
    }

    public InputStream getResourceAsStream(String s) {
        return context.getResourceAsStream(s);
    }

    public RequestDispatcher getRequestDispatcher(String s) {
        return context.getRequestDispatcher(s);
    }

    public RequestDispatcher getNamedDispatcher(String s) {
        return context.getNamedDispatcher(s);
    }

    public Servlet getServlet(String s) throws ServletException {
        return context.getServlet(s);
    }

    public Enumeration getServlets() {
        return context.getServlets();
    }

    public Enumeration getServletNames() {
        return context.getServletNames();
    }

    public void log(String s) {
        context.log(s);
    }

    public void log(Exception e, String s) {
        context.log(e, s);
    }

    public void log(String s, Throwable throwable) {
        context.log(s, throwable);
    }

    public String getRealPath(String s) {
        return context.getRealPath(s);
    }

    public String getServerInfo() {
        return context.getServerInfo();
    }

    public String getInitParameter(String s) {
        return context.getInitParameter(s);
    }

    public Enumeration getInitParameterNames() {
        return context.getInitParameterNames();
    }

    public Object getAttribute(String s) {
        return context.getAttribute(s);
    }

    public Enumeration getAttributeNames() {
        return context.getAttributeNames();
    }

    public void setAttribute(String s, Object o) {
        context.setAttribute(s, o);
    }

    public void removeAttribute(String s) {
        context.removeAttribute(s);
    }

    public String getServletContextName() {
        return context.getServletContextName();
    }
}
