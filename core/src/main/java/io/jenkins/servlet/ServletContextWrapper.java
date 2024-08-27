package io.jenkins.servlet;

import io.jenkins.servlet.descriptor.JspConfigDescriptorWrapper;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.servlet.Filter;
import javax.servlet.FilterRegistration;
import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import javax.servlet.SessionCookieConfig;
import javax.servlet.SessionTrackingMode;
import javax.servlet.descriptor.JspConfigDescriptor;

public class ServletContextWrapper {
    public static jakarta.servlet.ServletContext toJakartaServletContext(ServletContext from) {
        Objects.requireNonNull(from);
        return new jakarta.servlet.ServletContext() {
            @Override
            public String getContextPath() {
                return from.getContextPath();
            }

            @Override
            public jakarta.servlet.ServletContext getContext(String uripath) {
                return toJakartaServletContext(from.getContext(uripath));
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
            public Set<String> getResourcePaths(String paths) {
                return from.getResourcePaths(paths);
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
            public jakarta.servlet.RequestDispatcher getRequestDispatcher(String path) {
                return RequestDispatcherWrapper.toJakartaRequestDispatcher(from.getRequestDispatcher(path));
            }

            @Override
            public jakarta.servlet.RequestDispatcher getNamedDispatcher(String path) {
                return RequestDispatcherWrapper.toJakartaRequestDispatcher(from.getNamedDispatcher(path));
            }

            @Override
            public jakarta.servlet.Servlet getServlet(String name) throws jakarta.servlet.ServletException {
                try {
                    return ServletWrapper.toJakartaServlet(from.getServlet(name));
                } catch (ServletException e) {
                    throw ServletExceptionWrapper.toJakartaServletException(e);
                }
            }

            @Override
            public Enumeration<jakarta.servlet.Servlet> getServlets() {
                return Collections.enumeration(Collections.list(from.getServlets()).stream()
                        .map(ServletWrapper::toJakartaServlet)
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
            public jakarta.servlet.ServletRegistration.Dynamic addServlet(String servletName, String className) {
                return ServletRegistrationDynamicWrapper.toJakartaServletRegistrationDynamic(
                        from.addServlet(servletName, className));
            }

            @Override
            public jakarta.servlet.ServletRegistration.Dynamic addServlet(
                    String servletName, jakarta.servlet.Servlet servlet) {
                return ServletRegistrationDynamicWrapper.toJakartaServletRegistrationDynamic(
                        from.addServlet(servletName, ServletWrapper.fromJakartaServlet(servlet)));
            }

            @Override
            public jakarta.servlet.ServletRegistration.Dynamic addServlet(
                    String servletName, Class<? extends jakarta.servlet.Servlet> servletClass) {
                // TODO implement this
                throw new UnsupportedOperationException();
            }

            @Override
            public jakarta.servlet.ServletRegistration.Dynamic addJspFile(String servletName, String jspFile) {
                return ServletRegistrationDynamicWrapper.toJakartaServletRegistrationDynamic(
                        from.addJspFile(servletName, jspFile));
            }

            @Override
            public <T extends jakarta.servlet.Servlet> T createServlet(Class<T> clazz) {
                // TODO implement this
                throw new UnsupportedOperationException();
            }

            @Override
            public jakarta.servlet.ServletRegistration getServletRegistration(String servletName) {
                return ServletRegistrationWrapper.toJakartaServletRegistration(
                        from.getServletRegistration(servletName));
            }

            @Override
            public Map<String, ? extends jakarta.servlet.ServletRegistration> getServletRegistrations() {
                return from.getServletRegistrations().entrySet().stream()
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                entry -> ServletRegistrationWrapper.toJakartaServletRegistration(entry.getValue())));
            }

            @Override
            public jakarta.servlet.FilterRegistration.Dynamic addFilter(String filterName, String className) {
                return FilterRegistrationDynamicWrapper.toJakartaFilterRegistrationDynamic(
                        from.addFilter(filterName, className));
            }

            @Override
            public jakarta.servlet.FilterRegistration.Dynamic addFilter(
                    String filterName, jakarta.servlet.Filter filter) {
                return FilterRegistrationDynamicWrapper.toJakartaFilterRegistrationDynamic(
                        from.addFilter(filterName, FilterWrapper.fromJakartaFilter(filter)));
            }

            @Override
            public jakarta.servlet.FilterRegistration.Dynamic addFilter(
                    String filterName, Class<? extends jakarta.servlet.Filter> filterClass) {
                // TODO implement this
                throw new UnsupportedOperationException();
            }

            @Override
            public <T extends jakarta.servlet.Filter> T createFilter(Class<T> clazz) {
                // TODO implement this
                throw new UnsupportedOperationException();
            }

            @Override
            public jakarta.servlet.FilterRegistration getFilterRegistration(String filterName) {
                return FilterRegistrationWrapper.toJakartaFilterRegistration(from.getFilterRegistration(filterName));
            }

            @Override
            public Map<String, ? extends jakarta.servlet.FilterRegistration> getFilterRegistrations() {
                return from.getFilterRegistrations().entrySet().stream()
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                entry -> FilterRegistrationWrapper.toJakartaFilterRegistration(entry.getValue())));
            }

            @Override
            public jakarta.servlet.SessionCookieConfig getSessionCookieConfig() {
                return SessionCookieConfigWrapper.toJakartaSessionCookieConfig(from.getSessionCookieConfig());
            }

            @Override
            public void setSessionTrackingModes(Set<jakarta.servlet.SessionTrackingMode> sessionTrackingModes) {
                from.setSessionTrackingModes(sessionTrackingModes.stream()
                        .map(SessionTrackingModeWrapper::fromJakartaSessionTrackingMode)
                        .collect(Collectors.toSet()));
            }

            @Override
            public Set<jakarta.servlet.SessionTrackingMode> getDefaultSessionTrackingModes() {
                return from.getDefaultSessionTrackingModes().stream()
                        .map(SessionTrackingModeWrapper::toJakartaSessionTrackingMode)
                        .collect(Collectors.toSet());
            }

            @Override
            public Set<jakarta.servlet.SessionTrackingMode> getEffectiveSessionTrackingModes() {
                return from.getEffectiveSessionTrackingModes().stream()
                        .map(SessionTrackingModeWrapper::toJakartaSessionTrackingMode)
                        .collect(Collectors.toSet());
            }

            @Override
            public void addListener(String className) {
                from.addListener(className);
            }

            @Override
            public <T extends EventListener> void addListener(T t) {
                from.addListener(t);
            }

            @Override
            public void addListener(Class<? extends EventListener> listenerClass) {
                from.addListener(listenerClass);
            }

            @Override
            public <T extends EventListener> T createListener(Class<T> clazz) throws jakarta.servlet.ServletException {
                try {
                    return from.createListener(clazz);
                } catch (ServletException e) {
                    throw ServletExceptionWrapper.toJakartaServletException(e);
                }
            }

            @Override
            public jakarta.servlet.descriptor.JspConfigDescriptor getJspConfigDescriptor() {
                return JspConfigDescriptorWrapper.toJakartaJspConfigDescriptor(from.getJspConfigDescriptor());
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
        };
    }

    public static ServletContext fromJakartServletContext(jakarta.servlet.ServletContext from) {
        Objects.requireNonNull(from);
        return new ServletContext() {
            @Override
            public String getContextPath() {
                return from.getContextPath();
            }

            @Override
            public ServletContext getContext(String uripath) {
                return fromJakartServletContext(from.getContext(uripath));
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
                return RequestDispatcherWrapper.fromJakartaRequestDispatcher(from.getRequestDispatcher(path));
            }

            @Override
            public RequestDispatcher getNamedDispatcher(String name) {
                return RequestDispatcherWrapper.fromJakartaRequestDispatcher(from.getNamedDispatcher(name));
            }

            @Override
            public Servlet getServlet(String name) throws ServletException {
                try {
                    return ServletWrapper.fromJakartaServlet(from.getServlet(name));
                } catch (jakarta.servlet.ServletException e) {
                    throw ServletExceptionWrapper.fromJakartaServletException(e);
                }
            }

            @Override
            public Enumeration<Servlet> getServlets() {
                return Collections.enumeration(Collections.list(from.getServlets()).stream()
                        .map(ServletWrapper::fromJakartaServlet)
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
                return ServletRegistrationDynamicWrapper.fromJakartaServletRegistrationDynamic(
                        from.addServlet(servletName, className));
            }

            @Override
            public ServletRegistration.Dynamic addServlet(String servletName, Servlet servlet) {
                return ServletRegistrationDynamicWrapper.fromJakartaServletRegistrationDynamic(
                        from.addServlet(servletName, ServletWrapper.toJakartaServlet(servlet)));
            }

            @Override
            public ServletRegistration.Dynamic addServlet(String servletName, Class<? extends Servlet> servletClass) {
                // TODO implement this
                throw new UnsupportedOperationException();
            }

            @Override
            public ServletRegistration.Dynamic addJspFile(String servletName, String jspFile) {
                return ServletRegistrationDynamicWrapper.fromJakartaServletRegistrationDynamic(
                        from.addJspFile(servletName, jspFile));
            }

            @Override
            public <T extends Servlet> T createServlet(Class<T> clazz) {
                // TODO implement this
                throw new UnsupportedOperationException();
            }

            @Override
            public ServletRegistration getServletRegistration(String servletName) {
                return ServletRegistrationWrapper.fromJakartaServletRegistration(
                        from.getServletRegistration(servletName));
            }

            @Override
            public Map<String, ? extends ServletRegistration> getServletRegistrations() {
                return from.getServletRegistrations().entrySet().stream()
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                entry -> ServletRegistrationWrapper.fromJakartaServletRegistration(entry.getValue())));
            }

            @Override
            public FilterRegistration.Dynamic addFilter(String filterName, String className) {
                return FilterRegistrationDynamicWrapper.fromJakartaFilterRegistrationDynamic(
                        from.addFilter(filterName, className));
            }

            @Override
            public FilterRegistration.Dynamic addFilter(String filterName, Filter filter) {
                return FilterRegistrationDynamicWrapper.fromJakartaFilterRegistrationDynamic(
                        from.addFilter(filterName, FilterWrapper.toJakartaFilter(filter)));
            }

            @Override
            public FilterRegistration.Dynamic addFilter(String filterName, Class<? extends Filter> filterClass) {
                // TODO implement this
                throw new UnsupportedOperationException();
            }

            @Override
            public <T extends Filter> T createFilter(Class<T> clazz) {
                // TODO implement this
                throw new UnsupportedOperationException();
            }

            @Override
            public FilterRegistration getFilterRegistration(String filterName) {
                return FilterRegistrationWrapper.fromJakartaFilterRegistration(from.getFilterRegistration(filterName));
            }

            @Override
            public Map<String, ? extends FilterRegistration> getFilterRegistrations() {
                return from.getFilterRegistrations().entrySet().stream()
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                entry -> FilterRegistrationWrapper.fromJakartaFilterRegistration(entry.getValue())));
            }

            @Override
            public SessionCookieConfig getSessionCookieConfig() {
                return SessionCookieConfigWrapper.fromJakartaSessionCookieConfig(from.getSessionCookieConfig());
            }

            @Override
            public void setSessionTrackingModes(Set<SessionTrackingMode> sessionTrackingModes) {
                from.setSessionTrackingModes(sessionTrackingModes.stream()
                        .map(SessionTrackingModeWrapper::toJakartaSessionTrackingMode)
                        .collect(Collectors.toSet()));
            }

            @Override
            public Set<SessionTrackingMode> getDefaultSessionTrackingModes() {
                return from.getDefaultSessionTrackingModes().stream()
                        .map(SessionTrackingModeWrapper::fromJakartaSessionTrackingMode)
                        .collect(Collectors.toSet());
            }

            @Override
            public Set<SessionTrackingMode> getEffectiveSessionTrackingModes() {
                return from.getEffectiveSessionTrackingModes().stream()
                        .map(SessionTrackingModeWrapper::fromJakartaSessionTrackingMode)
                        .collect(Collectors.toSet());
            }

            @Override
            public void addListener(String className) {
                from.addListener(className);
            }

            @Override
            public <T extends EventListener> void addListener(T t) {
                // TODO implement this
                throw new UnsupportedOperationException();
            }

            @Override
            public void addListener(Class<? extends EventListener> listenerClass) {
                // TODO implement this
                throw new UnsupportedOperationException();
            }

            @Override
            public <T extends EventListener> T createListener(Class<T> clazz) {
                // TODO implement this
                throw new UnsupportedOperationException();
            }

            @Override
            public JspConfigDescriptor getJspConfigDescriptor() {
                return JspConfigDescriptorWrapper.fromJakartaJspConfigDescriptor(from.getJspConfigDescriptor());
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
        };
    }
}
