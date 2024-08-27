package io.jenkins.servlet;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.servlet.ServletRegistration;

public class ServletRegistrationWrapper {
    public static jakarta.servlet.ServletRegistration toJakartaServletRegistration(ServletRegistration from) {
        Objects.requireNonNull(from);
        return new jakarta.servlet.ServletRegistration() {
            @Override
            public String getName() {
                return from.getName();
            }

            @Override
            public String getClassName() {
                return from.getClassName();
            }

            @Override
            public boolean setInitParameter(String name, String value) {
                return from.setInitParameter(name, value);
            }

            @Override
            public String getInitParameter(String name) {
                return from.getInitParameter(name);
            }

            @Override
            public Set<String> setInitParameters(Map<String, String> initParameters) {
                return from.setInitParameters(initParameters);
            }

            @Override
            public Map<String, String> getInitParameters() {
                return from.getInitParameters();
            }

            @Override
            public Set<String> addMapping(String... urlPatterns) {
                return from.addMapping(urlPatterns);
            }

            @Override
            public Collection<String> getMappings() {
                return from.getMappings();
            }

            @Override
            public String getRunAsRole() {
                return from.getRunAsRole();
            }
        };
    }

    public static ServletRegistration fromJakartaServletRegistration(jakarta.servlet.ServletRegistration from) {
        Objects.requireNonNull(from);
        return new ServletRegistration() {
            @Override
            public String getName() {
                return from.getName();
            }

            @Override
            public String getClassName() {
                return from.getClassName();
            }

            @Override
            public boolean setInitParameter(String name, String value) {
                return from.setInitParameter(name, value);
            }

            @Override
            public String getInitParameter(String name) {
                return from.getInitParameter(name);
            }

            @Override
            public Set<String> setInitParameters(Map<String, String> initParameters) {
                return from.setInitParameters(initParameters);
            }

            @Override
            public Map<String, String> getInitParameters() {
                return from.getInitParameters();
            }

            @Override
            public Set<String> addMapping(String... urlPatterns) {
                return from.addMapping(urlPatterns);
            }

            @Override
            public Collection<String> getMappings() {
                return from.getMappings();
            }

            @Override
            public String getRunAsRole() {
                return from.getRunAsRole();
            }
        };
    }
}
