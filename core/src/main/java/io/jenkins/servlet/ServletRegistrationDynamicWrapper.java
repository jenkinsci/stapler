package io.jenkins.servlet;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletRegistration;
import javax.servlet.ServletSecurityElement;

public class ServletRegistrationDynamicWrapper {
    public static jakarta.servlet.ServletRegistration.Dynamic toJakartaServletRegistrationDynamic(
            ServletRegistration.Dynamic from) {
        Objects.requireNonNull(from);
        return new jakarta.servlet.ServletRegistration.Dynamic() {
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

            @Override
            public void setAsyncSupported(boolean isAsyncSupported) {
                from.setAsyncSupported(isAsyncSupported);
            }

            @Override
            public void setLoadOnStartup(int loadOnStartup) {
                from.setLoadOnStartup(loadOnStartup);
            }

            @Override
            public Set<String> setServletSecurity(jakarta.servlet.ServletSecurityElement constraint) {
                // TODO implement this
                throw new UnsupportedOperationException();
            }

            @Override
            public void setMultipartConfig(jakarta.servlet.MultipartConfigElement multipartConfig) {
                // TODO implement this
                throw new UnsupportedOperationException();
            }

            @Override
            public void setRunAsRole(String roleName) {
                from.setRunAsRole(roleName);
            }
        };
    }

    public static ServletRegistration.Dynamic fromJakartaServletRegistrationDynamic(
            jakarta.servlet.ServletRegistration.Dynamic from) {
        Objects.requireNonNull(from);
        return new ServletRegistration.Dynamic() {
            @Override
            public void setLoadOnStartup(int loadOnStartup) {
                from.setLoadOnStartup(loadOnStartup);
            }

            @Override
            public Set<String> setServletSecurity(ServletSecurityElement constraint) {
                // TODO implement this
                throw new UnsupportedOperationException();
            }

            @Override
            public void setMultipartConfig(MultipartConfigElement multipartConfig) {
                // TODO implement this
                throw new UnsupportedOperationException();
            }

            @Override
            public void setRunAsRole(String roleName) {
                from.setRunAsRole(roleName);
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

            @Override
            public void setAsyncSupported(boolean isAsyncSupported) {
                from.setAsyncSupported(isAsyncSupported);
            }

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
        };
    }
}
