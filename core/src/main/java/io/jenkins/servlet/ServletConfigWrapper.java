package io.jenkins.servlet;

import java.util.Enumeration;
import java.util.Objects;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

public class ServletConfigWrapper {
    public static jakarta.servlet.ServletConfig toJakartaServletConfig(ServletConfig from) {
        Objects.requireNonNull(from);
        return new jakarta.servlet.ServletConfig() {
            @Override
            public String getServletName() {
                return from.getServletName();
            }

            @Override
            public jakarta.servlet.ServletContext getServletContext() {
                return ServletContextWrapper.toJakartaServletContext(from.getServletContext());
            }

            @Override
            public String getInitParameter(String name) {
                return from.getInitParameter(name);
            }

            @Override
            public Enumeration<String> getInitParameterNames() {
                return from.getInitParameterNames();
            }
        };
    }

    public static ServletConfig fromJakartaServletConfig(jakarta.servlet.ServletConfig from) {
        Objects.requireNonNull(from);
        return new ServletConfig() {
            @Override
            public String getServletName() {
                return from.getServletName();
            }

            @Override
            public ServletContext getServletContext() {
                return ServletContextWrapper.fromJakartServletContext(from.getServletContext());
            }

            @Override
            public String getInitParameter(String name) {
                return from.getInitParameter(name);
            }

            @Override
            public Enumeration<String> getInitParameterNames() {
                return from.getInitParameterNames();
            }
        };
    }
}
