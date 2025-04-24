package io.jenkins.servlet;

import io.jenkins.servlet.descriptor.JspConfigDescriptorWrapper;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
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
                ServletContext context = from.getContext(uripath);
                return context != null ? toJakartaServletContext(context) : null;
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
                RequestDispatcher requestDispatcher = from.getRequestDispatcher(path);
                return requestDispatcher != null
                        ? RequestDispatcherWrapper.toJakartaRequestDispatcher(requestDispatcher)
                        : null;
            }

            @Override
            public jakarta.servlet.RequestDispatcher getNamedDispatcher(String path) {
                RequestDispatcher namedDispatcher = from.getNamedDispatcher(path);
                return namedDispatcher != null
                        ? RequestDispatcherWrapper.toJakartaRequestDispatcher(namedDispatcher)
                        : null;
            }

            @Override
            public void log(String msg) {
                from.log(msg);
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
                JspConfigDescriptor jspConfigDescriptor = from.getJspConfigDescriptor();
                return jspConfigDescriptor != null
                        ? JspConfigDescriptorWrapper.toJakartaJspConfigDescriptor(jspConfigDescriptor)
                        : null;
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
                jakarta.servlet.ServletContext context = from.getContext(uripath);
                return context != null ? fromJakartServletContext(context) : null;
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
                jakarta.servlet.RequestDispatcher requestDispatcher = from.getRequestDispatcher(path);
                return requestDispatcher != null
                        ? RequestDispatcherWrapper.fromJakartaRequestDispatcher(requestDispatcher)
                        : null;
            }

            @Override
            public RequestDispatcher getNamedDispatcher(String name) {
                jakarta.servlet.RequestDispatcher namedDispatcher = from.getNamedDispatcher(name);
                return namedDispatcher != null
                        ? RequestDispatcherWrapper.fromJakartaRequestDispatcher(namedDispatcher)
                        : null;
            }

            @Override
            public Servlet getServlet(String name) {
                // TODO implement this
                throw new UnsupportedOperationException();
            }

            @Override
            public Enumeration<Servlet> getServlets() {
                // TODO implement this
                throw new UnsupportedOperationException();
            }

            @Override
            public Enumeration<String> getServletNames() {
                // TODO implement this
                throw new UnsupportedOperationException();
            }

            @Override
            public void log(String msg) {
                from.log(msg);
            }

            @Override
            public void log(Exception exception, String msg) {
                // TODO implement this
                throw new UnsupportedOperationException();
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
                jakarta.servlet.descriptor.JspConfigDescriptor jspConfigDescriptor = from.getJspConfigDescriptor();
                return jspConfigDescriptor != null
                        ? JspConfigDescriptorWrapper.fromJakartaJspConfigDescriptor(jspConfigDescriptor)
                        : null;
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
