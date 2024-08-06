package io.jenkins.servlet;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;

public class FilterRegistrationDynamicWrapper {
    public static jakarta.servlet.FilterRegistration.Dynamic toJakartaFilterRegistrationDynamic(
            FilterRegistration.Dynamic from) {
        Objects.requireNonNull(from);
        return new jakarta.servlet.FilterRegistration.Dynamic() {
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
            public void setAsyncSupported(boolean isAsyncSupported) {
                from.setAsyncSupported(isAsyncSupported);
            }

            @Override
            public void addMappingForServletNames(
                    EnumSet<jakarta.servlet.DispatcherType> dispatcherTypes,
                    boolean isMatchAfter,
                    String... servletNames) {
                from.addMappingForServletNames(
                        EnumSet.copyOf(dispatcherTypes.stream()
                                .map(DispatcherTypeWrapper::fromJakartaDispatcherType)
                                .collect(Collectors.toSet())),
                        isMatchAfter,
                        servletNames);
            }

            @Override
            public Collection<String> getServletNameMappings() {
                return from.getServletNameMappings();
            }

            @Override
            public void addMappingForUrlPatterns(
                    EnumSet<jakarta.servlet.DispatcherType> dispatcherTypes,
                    boolean isMatchAfter,
                    String... urlPatterns) {
                from.addMappingForUrlPatterns(
                        EnumSet.copyOf(dispatcherTypes.stream()
                                .map(DispatcherTypeWrapper::fromJakartaDispatcherType)
                                .collect(Collectors.toSet())),
                        isMatchAfter,
                        urlPatterns);
            }

            @Override
            public Collection<String> getUrlPatternMappings() {
                return from.getUrlPatternMappings();
            }
        };
    }

    public static FilterRegistration.Dynamic fromJakartaFilterRegistrationDynamic(
            jakarta.servlet.FilterRegistration.Dynamic from) {
        Objects.requireNonNull(from);
        return new FilterRegistration.Dynamic() {
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
            public void setAsyncSupported(boolean isAsyncSupported) {
                from.setAsyncSupported(isAsyncSupported);
            }

            @Override
            public void addMappingForServletNames(
                    EnumSet<DispatcherType> dispatcherTypes, boolean isMatchAfter, String... servletNames) {
                from.addMappingForServletNames(
                        EnumSet.copyOf(dispatcherTypes.stream()
                                .map(DispatcherTypeWrapper::toJakartaDispatcherType)
                                .collect(Collectors.toSet())),
                        isMatchAfter,
                        servletNames);
            }

            @Override
            public Collection<String> getServletNameMappings() {
                return from.getServletNameMappings();
            }

            @Override
            public void addMappingForUrlPatterns(
                    EnumSet<DispatcherType> dispatcherTypes, boolean isMatchAfter, String... urlPatterns) {
                from.addMappingForUrlPatterns(
                        EnumSet.copyOf(dispatcherTypes.stream()
                                .map(DispatcherTypeWrapper::toJakartaDispatcherType)
                                .collect(Collectors.toSet())),
                        isMatchAfter,
                        urlPatterns);
            }

            @Override
            public Collection<String> getUrlPatternMappings() {
                return from.getUrlPatternMappings();
            }
        };
    }
}
