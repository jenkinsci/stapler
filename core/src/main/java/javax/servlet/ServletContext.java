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

package javax.servlet;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.servlet.descriptor.JspConfigDescriptor;

public interface ServletContext {
    String TEMPDIR = "javax.servlet.context.tempdir";

    String ORDERED_LIBS = "javax.servlet.context.orderedLibs";

    String getContextPath();

    ServletContext getContext(String uripath);

    int getMajorVersion();

    int getMinorVersion();

    int getEffectiveMajorVersion();

    int getEffectiveMinorVersion();

    String getMimeType(String file);

    Set<String> getResourcePaths(String path);

    URL getResource(String path) throws MalformedURLException;

    InputStream getResourceAsStream(String path);

    RequestDispatcher getRequestDispatcher(String path);

    RequestDispatcher getNamedDispatcher(String name);

    @Deprecated
    Servlet getServlet(String name) throws ServletException;

    @Deprecated
    Enumeration<Servlet> getServlets();

    @Deprecated
    Enumeration<String> getServletNames();

    void log(String msg);

    @Deprecated
    void log(Exception exception, String msg);

    void log(String message, Throwable throwable);

    String getRealPath(String path);

    String getServerInfo();

    String getInitParameter(String name);

    Enumeration<String> getInitParameterNames();

    boolean setInitParameter(String name, String value);

    Object getAttribute(String name);

    Enumeration<String> getAttributeNames();

    void setAttribute(String name, Object object);

    void removeAttribute(String name);

    String getServletContextName();

    ServletRegistration.Dynamic addServlet(String servletName, String className);

    ServletRegistration.Dynamic addServlet(String servletName, Servlet servlet);

    ServletRegistration.Dynamic addServlet(String servletName, Class<? extends Servlet> servletClass);

    ServletRegistration.Dynamic addJspFile(String servletName, String jspFile);

    <T extends Servlet> T createServlet(Class<T> clazz) throws ServletException;

    ServletRegistration getServletRegistration(String servletName);

    Map<String, ? extends ServletRegistration> getServletRegistrations();

    FilterRegistration.Dynamic addFilter(String filterName, String className);

    FilterRegistration.Dynamic addFilter(String filterName, Filter filter);

    FilterRegistration.Dynamic addFilter(String filterName, Class<? extends Filter> filterClass);

    <T extends Filter> T createFilter(Class<T> clazz) throws ServletException;

    FilterRegistration getFilterRegistration(String filterName);

    Map<String, ? extends FilterRegistration> getFilterRegistrations();

    SessionCookieConfig getSessionCookieConfig();

    void setSessionTrackingModes(Set<SessionTrackingMode> sessionTrackingModes);

    Set<SessionTrackingMode> getDefaultSessionTrackingModes();

    Set<SessionTrackingMode> getEffectiveSessionTrackingModes();

    void addListener(String className);

    <T extends EventListener> void addListener(T t);

    void addListener(Class<? extends EventListener> listenerClass);

    <T extends EventListener> T createListener(Class<T> clazz) throws ServletException;

    JspConfigDescriptor getJspConfigDescriptor();

    ClassLoader getClassLoader();

    void declareRoles(String... roleNames);

    String getVirtualServerName();

    int getSessionTimeout();

    void setSessionTimeout(int sessionTimeout);

    String getRequestCharacterEncoding();

    void setRequestCharacterEncoding(String encoding);

    String getResponseCharacterEncoding();

    void setResponseCharacterEncoding(String encoding);

    default jakarta.servlet.ServletContext toJakartaServletContext() {
        return new jakarta.servlet.ServletContext() {
            @Override
            public String getContextPath() {
                return ServletContext.this.getContextPath();
            }

            @Override
            public jakarta.servlet.ServletContext getContext(String uripath) {
                return ServletContext.this.getContext(uripath).toJakartaServletContext();
            }

            @Override
            public int getMajorVersion() {
                return ServletContext.this.getMajorVersion();
            }

            @Override
            public int getMinorVersion() {
                return ServletContext.this.getMinorVersion();
            }

            @Override
            public int getEffectiveMajorVersion() {
                return ServletContext.this.getEffectiveMajorVersion();
            }

            @Override
            public int getEffectiveMinorVersion() {
                return ServletContext.this.getEffectiveMinorVersion();
            }

            @Override
            public String getMimeType(String file) {
                return ServletContext.this.getMimeType(file);
            }

            @Override
            public Set<String> getResourcePaths(String paths) {
                return ServletContext.this.getResourcePaths(paths);
            }

            @Override
            public URL getResource(String path) throws MalformedURLException {
                return ServletContext.this.getResource(path);
            }

            @Override
            public InputStream getResourceAsStream(String path) {
                return ServletContext.this.getResourceAsStream(path);
            }

            @Override
            public jakarta.servlet.RequestDispatcher getRequestDispatcher(String path) {
                return ServletContext.this.getRequestDispatcher(path).toJakartaRequestDispatcher();
            }

            @Override
            public jakarta.servlet.RequestDispatcher getNamedDispatcher(String path) {
                return ServletContext.this.getNamedDispatcher(path).toJakartaRequestDispatcher();
            }

            @Override
            public jakarta.servlet.Servlet getServlet(String name) throws jakarta.servlet.ServletException {
                try {
                    return ServletContext.this.getServlet(name).toJakartaServlet();
                } catch (ServletException e) {
                    throw new jakarta.servlet.ServletException(e);
                }
            }

            @Override
            public Enumeration<jakarta.servlet.Servlet> getServlets() {
                return Collections.enumeration(Collections.list(ServletContext.this.getServlets()).stream()
                        .map(Servlet::toJakartaServlet)
                        .collect(Collectors.toList()));
            }

            @Override
            public Enumeration<String> getServletNames() {
                return ServletContext.this.getServletNames();
            }

            @Override
            public void log(String msg) {
                ServletContext.this.log(msg);
            }

            @Override
            public void log(Exception exception, String msg) {
                ServletContext.this.log(exception, msg);
            }

            @Override
            public void log(String message, Throwable throwable) {
                ServletContext.this.log(message, throwable);
            }

            @Override
            public String getRealPath(String path) {
                return ServletContext.this.getRealPath(path);
            }

            @Override
            public String getServerInfo() {
                return ServletContext.this.getServerInfo();
            }

            @Override
            public String getInitParameter(String name) {
                return ServletContext.this.getInitParameter(name);
            }

            @Override
            public Enumeration<String> getInitParameterNames() {
                return ServletContext.this.getInitParameterNames();
            }

            @Override
            public boolean setInitParameter(String name, String value) {
                return ServletContext.this.setInitParameter(name, value);
            }

            @Override
            public Object getAttribute(String name) {
                return ServletContext.this.getAttribute(name);
            }

            @Override
            public Enumeration<String> getAttributeNames() {
                return ServletContext.this.getAttributeNames();
            }

            @Override
            public void setAttribute(String name, Object object) {
                ServletContext.this.setAttribute(name, object);
            }

            @Override
            public void removeAttribute(String name) {
                ServletContext.this.removeAttribute(name);
            }

            @Override
            public String getServletContextName() {
                return ServletContext.this.getServletContextName();
            }

            @Override
            public jakarta.servlet.ServletRegistration.Dynamic addServlet(String servletName, String className) {
                return ServletContext.this.addServlet(servletName, className).toJakartaServletRegistrationDynamic();
            }

            @Override
            public jakarta.servlet.ServletRegistration.Dynamic addServlet(
                    String servletName, jakarta.servlet.Servlet servlet) {
                return ServletContext.this
                        .addServlet(servletName, Servlet.fromJakartaServlet(servlet))
                        .toJakartaServletRegistrationDynamic();
            }

            @Override
            public jakarta.servlet.ServletRegistration.Dynamic addServlet(
                    String servletName, Class<? extends jakarta.servlet.Servlet> servletClass) {
                // TODO
                throw new UnsupportedOperationException();
            }

            @Override
            public jakarta.servlet.ServletRegistration.Dynamic addJspFile(String servletName, String jspFile) {
                return ServletContext.this.addJspFile(servletName, jspFile).toJakartaServletRegistrationDynamic();
            }

            @Override
            public <T extends jakarta.servlet.Servlet> T createServlet(Class<T> clazz)
                    throws jakarta.servlet.ServletException {
                // TODO
                throw new UnsupportedOperationException();
            }

            @Override
            public jakarta.servlet.ServletRegistration getServletRegistration(String servletName) {
                return ServletContext.this.getServletRegistration(servletName).toJakartaServletRegistration();
            }

            @Override
            public Map<String, ? extends jakarta.servlet.ServletRegistration> getServletRegistrations() {
                return ServletContext.this.getServletRegistrations().entrySet().stream()
                        .collect(Collectors.toMap(
                                Map.Entry::getKey, entry -> entry.getValue().toJakartaServletRegistration()));
            }

            @Override
            public jakarta.servlet.FilterRegistration.Dynamic addFilter(String filterName, String className) {
                return ServletContext.this.addFilter(filterName, className).toJakartaFilterRegistrationDynamic();
            }

            @Override
            public jakarta.servlet.FilterRegistration.Dynamic addFilter(
                    String filterName, jakarta.servlet.Filter filter) {
                return ServletContext.this
                        .addFilter(filterName, Filter.fromJakartaFilter(filter))
                        .toJakartaFilterRegistrationDynamic();
            }

            @Override
            public jakarta.servlet.FilterRegistration.Dynamic addFilter(
                    String filterName, Class<? extends jakarta.servlet.Filter> filterClass) {
                // TODO
                throw new UnsupportedOperationException();
            }

            @Override
            public <T extends jakarta.servlet.Filter> T createFilter(Class<T> clazz)
                    throws jakarta.servlet.ServletException {
                // TODO
                throw new UnsupportedOperationException();
            }

            @Override
            public jakarta.servlet.FilterRegistration getFilterRegistration(String filterName) {
                return ServletContext.this.getFilterRegistration(filterName).toJakartaFilterRegistration();
            }

            @Override
            public Map<String, ? extends jakarta.servlet.FilterRegistration> getFilterRegistrations() {
                return ServletContext.this.getFilterRegistrations().entrySet().stream()
                        .collect(Collectors.toMap(
                                Map.Entry::getKey, entry -> entry.getValue().toJakartaFilterRegistration()));
            }

            @Override
            public jakarta.servlet.SessionCookieConfig getSessionCookieConfig() {
                return ServletContext.this.getSessionCookieConfig().toJakartaSessionCookieConfig();
            }

            @Override
            public void setSessionTrackingModes(Set<jakarta.servlet.SessionTrackingMode> sessionTrackingModes) {
                ServletContext.this.setSessionTrackingModes(sessionTrackingModes.stream()
                        .map(SessionTrackingMode::fromJakartaSessionTrackingMode)
                        .collect(Collectors.toSet()));
            }

            @Override
            public Set<jakarta.servlet.SessionTrackingMode> getDefaultSessionTrackingModes() {
                return ServletContext.this.getDefaultSessionTrackingModes().stream()
                        .map(SessionTrackingMode::toJakartaSessionTrackingMode)
                        .collect(Collectors.toSet());
            }

            @Override
            public Set<jakarta.servlet.SessionTrackingMode> getEffectiveSessionTrackingModes() {
                return ServletContext.this.getEffectiveSessionTrackingModes().stream()
                        .map(SessionTrackingMode::toJakartaSessionTrackingMode)
                        .collect(Collectors.toSet());
            }

            @Override
            public void addListener(String className) {
                ServletContext.this.addListener(className);
            }

            @Override
            public <T extends EventListener> void addListener(T t) {
                ServletContext.this.addListener(t);
            }

            @Override
            public void addListener(Class<? extends EventListener> listenerClass) {
                ServletContext.this.addListener(listenerClass);
            }

            @Override
            public <T extends EventListener> T createListener(Class<T> clazz) throws jakarta.servlet.ServletException {
                try {
                    return ServletContext.this.createListener(clazz);
                } catch (ServletException e) {
                    throw new jakarta.servlet.ServletException(e);
                }
            }

            @Override
            public jakarta.servlet.descriptor.JspConfigDescriptor getJspConfigDescriptor() {
                return ServletContext.this.getJspConfigDescriptor().toJakartaJspConfigDescriptor();
            }

            @Override
            public ClassLoader getClassLoader() {
                return ServletContext.this.getClassLoader();
            }

            @Override
            public void declareRoles(String... roleNames) {
                ServletContext.this.declareRoles(roleNames);
            }

            @Override
            public String getVirtualServerName() {
                return ServletContext.this.getVirtualServerName();
            }

            @Override
            public int getSessionTimeout() {
                return ServletContext.this.getSessionTimeout();
            }

            @Override
            public void setSessionTimeout(int sessionTimeout) {
                ServletContext.this.setSessionTimeout(sessionTimeout);
            }

            @Override
            public String getRequestCharacterEncoding() {
                return ServletContext.this.getRequestCharacterEncoding();
            }

            @Override
            public void setRequestCharacterEncoding(String encoding) {
                ServletContext.this.setRequestCharacterEncoding(encoding);
            }

            @Override
            public String getResponseCharacterEncoding() {
                return ServletContext.this.getResponseCharacterEncoding();
            }

            @Override
            public void setResponseCharacterEncoding(String encoding) {
                ServletContext.this.setResponseCharacterEncoding(encoding);
            }
        };
    }

    static ServletContext fromJakartServletContext(jakarta.servlet.ServletContext from) {
        return new ServletContext() {
            @Override
            public String getContextPath() {
                return from.getContextPath();
            }

            @Override
            public ServletContext getContext(String uripath) {
                return ServletContext.fromJakartServletContext(from.getContext(uripath));
            }

            @Override
            public int getMajorVersion() {
                return from.getMajorVersion();
            }

            @Override
            public int getMinorVersion() {
                return from.getMinorVersion();
            }

            @Override
            public int getEffectiveMajorVersion() {
                return from.getEffectiveMajorVersion();
            }

            @Override
            public int getEffectiveMinorVersion() {
                return from.getEffectiveMinorVersion();
            }

            @Override
            public String getMimeType(String file) {
                return from.getMimeType(file);
            }

            @Override
            public Set<String> getResourcePaths(String path) {
                return from.getResourcePaths(path);
            }

            @Override
            public URL getResource(String path) throws MalformedURLException {
                return from.getResource(path);
            }

            @Override
            public InputStream getResourceAsStream(String path) {
                return from.getResourceAsStream(path);
            }

            @Override
            public RequestDispatcher getRequestDispatcher(String path) {
                return RequestDispatcher.fromJakartaRequestDispatcher(from.getRequestDispatcher(path));
            }

            @Override
            public RequestDispatcher getNamedDispatcher(String name) {
                return RequestDispatcher.fromJakartaRequestDispatcher(from.getNamedDispatcher(name));
            }

            @Override
            public Servlet getServlet(String name) throws ServletException {
                try {
                    return Servlet.fromJakartaServlet(from.getServlet(name));
                } catch (jakarta.servlet.ServletException e) {
                    throw new ServletException(e);
                }
            }

            @Override
            public Enumeration<Servlet> getServlets() {
                return Collections.enumeration(Collections.list(from.getServlets()).stream()
                        .map(Servlet::fromJakartaServlet)
                        .collect(Collectors.toList()));
            }

            @Override
            public Enumeration<String> getServletNames() {
                return from.getServletNames();
            }

            @Override
            public void log(String msg) {
                from.log(msg);
            }

            @Override
            public void log(Exception exception, String msg) {
                from.log(exception, msg);
            }

            @Override
            public void log(String message, Throwable throwable) {
                from.log(message, throwable);
            }

            @Override
            public String getRealPath(String path) {
                return from.getRealPath(path);
            }

            @Override
            public String getServerInfo() {
                return from.getServerInfo();
            }

            @Override
            public String getInitParameter(String name) {
                return from.getInitParameter(name);
            }

            @Override
            public Enumeration<String> getInitParameterNames() {
                return from.getInitParameterNames();
            }

            @Override
            public boolean setInitParameter(String name, String value) {
                return from.setInitParameter(name, value);
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
            public void setAttribute(String name, Object object) {
                from.setAttribute(name, object);
            }

            @Override
            public void removeAttribute(String name) {
                from.removeAttribute(name);
            }

            @Override
            public String getServletContextName() {
                return from.getServletContextName();
            }

            @Override
            public ServletRegistration.Dynamic addServlet(String servletName, String className) {
                return ServletRegistration.Dynamic.fromJakartaServletRegistrationDynamic(
                        from.addServlet(servletName, className));
            }

            @Override
            public ServletRegistration.Dynamic addServlet(String servletName, Servlet servlet) {
                return ServletRegistration.Dynamic.fromJakartaServletRegistrationDynamic(
                        from.addServlet(servletName, servlet.toJakartaServlet()));
            }

            @Override
            public ServletRegistration.Dynamic addServlet(String servletName, Class<? extends Servlet> servletClass) {
                // TODO
                throw new UnsupportedOperationException();
            }

            @Override
            public ServletRegistration.Dynamic addJspFile(String servletName, String jspFile) {
                return ServletRegistration.Dynamic.fromJakartaServletRegistrationDynamic(
                        from.addJspFile(servletName, jspFile));
            }

            @Override
            public <T extends Servlet> T createServlet(Class<T> clazz) throws ServletException {
                // TODO
                throw new UnsupportedOperationException();
            }

            @Override
            public ServletRegistration getServletRegistration(String servletName) {
                return ServletRegistration.fromJakartaServletRegistration(from.getServletRegistration(servletName));
            }

            @Override
            public Map<String, ? extends ServletRegistration> getServletRegistrations() {
                return from.getServletRegistrations().entrySet().stream()
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                entry -> ServletRegistration.fromJakartaServletRegistration(entry.getValue())));
            }

            @Override
            public FilterRegistration.Dynamic addFilter(String filterName, String className) {
                return FilterRegistration.Dynamic.fromJakartaFilterRegistrationDynamic(
                        from.addFilter(filterName, className));
            }

            @Override
            public FilterRegistration.Dynamic addFilter(String filterName, Filter filter) {
                return FilterRegistration.Dynamic.fromJakartaFilterRegistrationDynamic(
                        from.addFilter(filterName, filter.toJakartaFilter()));
            }

            @Override
            public FilterRegistration.Dynamic addFilter(String filterName, Class<? extends Filter> filterClass) {
                // TODO
                throw new UnsupportedOperationException();
            }

            @Override
            public <T extends Filter> T createFilter(Class<T> clazz) throws ServletException {
                // TODO
                throw new UnsupportedOperationException();
            }

            @Override
            public FilterRegistration getFilterRegistration(String filterName) {
                return FilterRegistration.fromJakartaFilterRegistration(from.getFilterRegistration(filterName));
            }

            @Override
            public Map<String, ? extends FilterRegistration> getFilterRegistrations() {
                return from.getFilterRegistrations().entrySet().stream()
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                entry -> FilterRegistration.fromJakartaFilterRegistration(entry.getValue())));
            }

            @Override
            public SessionCookieConfig getSessionCookieConfig() {
                return SessionCookieConfig.fromJakartaSessionCookieConfig(from.getSessionCookieConfig());
            }

            @Override
            public void setSessionTrackingModes(Set<SessionTrackingMode> sessionTrackingModes) {
                from.setSessionTrackingModes(sessionTrackingModes.stream()
                        .map(SessionTrackingMode::toJakartaSessionTrackingMode)
                        .collect(Collectors.toSet()));
            }

            @Override
            public Set<SessionTrackingMode> getDefaultSessionTrackingModes() {
                return from.getDefaultSessionTrackingModes().stream()
                        .map(SessionTrackingMode::fromJakartaSessionTrackingMode)
                        .collect(Collectors.toSet());
            }

            @Override
            public Set<SessionTrackingMode> getEffectiveSessionTrackingModes() {
                return from.getEffectiveSessionTrackingModes().stream()
                        .map(SessionTrackingMode::fromJakartaSessionTrackingMode)
                        .collect(Collectors.toSet());
            }

            @Override
            public void addListener(String className) {
                from.addListener(className);
            }

            @Override
            public <T extends EventListener> void addListener(T t) {
                // TODO
                throw new UnsupportedOperationException();
            }

            @Override
            public void addListener(Class<? extends EventListener> listenerClass) {
                // TODO
                throw new UnsupportedOperationException();
            }

            @Override
            public <T extends EventListener> T createListener(Class<T> clazz) throws ServletException {
                // TODO
                throw new UnsupportedOperationException();
            }

            @Override
            public JspConfigDescriptor getJspConfigDescriptor() {
                return JspConfigDescriptor.fromJakartaJspConfigDescriptor(from.getJspConfigDescriptor());
            }

            @Override
            public ClassLoader getClassLoader() {
                return from.getClassLoader();
            }

            @Override
            public void declareRoles(String... roleNames) {
                from.declareRoles(roleNames);
            }

            @Override
            public String getVirtualServerName() {
                return from.getVirtualServerName();
            }

            @Override
            public int getSessionTimeout() {
                return from.getSessionTimeout();
            }

            @Override
            public void setSessionTimeout(int sessionTimeout) {
                from.setSessionTimeout(sessionTimeout);
            }

            @Override
            public String getRequestCharacterEncoding() {
                return from.getRequestCharacterEncoding();
            }

            @Override
            public void setRequestCharacterEncoding(String encoding) {
                from.setRequestCharacterEncoding(encoding);
            }

            @Override
            public String getResponseCharacterEncoding() {
                return from.getResponseCharacterEncoding();
            }

            @Override
            public void setResponseCharacterEncoding(String encoding) {
                from.setResponseCharacterEncoding(encoding);
            }

            @Override
            public jakarta.servlet.ServletContext toJakartaServletContext() {
                return from;
            }
        };
    }
}
