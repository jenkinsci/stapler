package io.jenkins.servlet;

import java.util.Enumeration;
import java.util.Objects;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;

public class FilterConfigWrapper {
    public static jakarta.servlet.FilterConfig toJakartaFilterConfig(FilterConfig from) {
        Objects.requireNonNull(from);
        return new jakarta.servlet.FilterConfig() {
            @Override
            public String getFilterName() {
                return from.getFilterName();
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

    public static FilterConfig fromJakartaFilterConfig(jakarta.servlet.FilterConfig from) {
        Objects.requireNonNull(from);
        return new FilterConfig() {
            @Override
            public String getFilterName() {
                return from.getFilterName();
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
