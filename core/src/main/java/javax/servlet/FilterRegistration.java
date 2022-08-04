/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package javax.servlet;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public interface FilterRegistration extends Registration {
    void addMappingForServletNames(
            EnumSet<DispatcherType> dispatcherTypes, boolean isMatchAfter, String... servletNames);

    Collection<String> getServletNameMappings();

    void addMappingForUrlPatterns(
            EnumSet<DispatcherType> dispatcherTypes, boolean isMatchAfter, String... urlPatterns);

    Collection<String> getUrlPatternMappings();

    interface Dynamic extends FilterRegistration, Registration.Dynamic {
        default jakarta.servlet.FilterRegistration.Dynamic toJakartaFilterRegistrationDynamic() {
            return new jakarta.servlet.FilterRegistration.Dynamic() {
                @Override
                public String getName() {
                    return FilterRegistration.Dynamic.this.getName();
                }

                @Override
                public String getClassName() {
                    return FilterRegistration.Dynamic.this.getClassName();
                }

                @Override
                public boolean setInitParameter(String s, String s1) {
                    return FilterRegistration.Dynamic.this.setInitParameter(s, s1);
                }

                @Override
                public String getInitParameter(String s) {
                    return FilterRegistration.Dynamic.this.getInitParameter(s);
                }

                @Override
                public Set<String> setInitParameters(Map<String, String> map) {
                    return FilterRegistration.Dynamic.this.setInitParameters(map);
                }

                @Override
                public Map<String, String> getInitParameters() {
                    return FilterRegistration.Dynamic.this.getInitParameters();
                }

                @Override
                public void setAsyncSupported(boolean b) {
                    FilterRegistration.Dynamic.this.setAsyncSupported(b);
                }

                @Override
                public void addMappingForServletNames(
                        EnumSet<jakarta.servlet.DispatcherType> enumSet,
                        boolean b,
                        String... strings) {
                    FilterRegistration.Dynamic.this.addMappingForServletNames(
                            EnumSet.copyOf(
                                    enumSet.stream()
                                            .map(DispatcherType::fromJakartaDispatcherType)
                                            .collect(Collectors.toSet())),
                            b,
                            strings);
                }

                @Override
                public Collection<String> getServletNameMappings() {
                    return FilterRegistration.Dynamic.this.getServletNameMappings();
                }

                @Override
                public void addMappingForUrlPatterns(
                        EnumSet<jakarta.servlet.DispatcherType> enumSet,
                        boolean b,
                        String... strings) {
                    FilterRegistration.Dynamic.this.addMappingForUrlPatterns(
                            EnumSet.copyOf(
                                    enumSet.stream()
                                            .map(DispatcherType::fromJakartaDispatcherType)
                                            .collect(Collectors.toSet())),
                            b,
                            strings);
                }

                @Override
                public Collection<String> getUrlPatternMappings() {
                    return FilterRegistration.Dynamic.this.getUrlPatternMappings();
                }
            };
        }

        static FilterRegistration.Dynamic fromJakartaFilterRegistrationDynamic(
                jakarta.servlet.FilterRegistration.Dynamic from) {
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
                        EnumSet<DispatcherType> dispatcherTypes,
                        boolean isMatchAfter,
                        String... servletNames) {
                    from.addMappingForServletNames(
                            EnumSet.copyOf(
                                    dispatcherTypes.stream()
                                            .map(DispatcherType::toJakartaDispatcherType)
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
                        EnumSet<DispatcherType> dispatcherTypes,
                        boolean isMatchAfter,
                        String... urlPatterns) {
                    from.addMappingForUrlPatterns(
                            EnumSet.copyOf(
                                    dispatcherTypes.stream()
                                            .map(DispatcherType::toJakartaDispatcherType)
                                            .collect(Collectors.toSet())),
                            isMatchAfter,
                            urlPatterns);
                }

                @Override
                public Collection<String> getUrlPatternMappings() {
                    return from.getUrlPatternMappings();
                }

                @Override
                public jakarta.servlet.Registration toJakartaRegistration() {
                    return from;
                }

                @Override
                public jakarta.servlet.Registration.Dynamic toJakartaRegistrationDynamic() {
                    return from;
                }

                @Override
                public jakarta.servlet.FilterRegistration toJakartaFilterRegistration() {
                    return from;
                }

                @Override
                public jakarta.servlet.FilterRegistration.Dynamic
                        toJakartaFilterRegistrationDynamic() {
                    return from;
                }
            };
        }
    }

    default jakarta.servlet.FilterRegistration toJakartaFilterRegistration() {
        return new jakarta.servlet.FilterRegistration() {
            @Override
            public String getName() {
                return FilterRegistration.this.getName();
            }

            @Override
            public String getClassName() {
                return FilterRegistration.this.getClassName();
            }

            @Override
            public boolean setInitParameter(String s, String s1) {
                return FilterRegistration.this.setInitParameter(s, s1);
            }

            @Override
            public String getInitParameter(String s) {
                return FilterRegistration.this.getInitParameter(s);
            }

            @Override
            public Set<String> setInitParameters(Map<String, String> map) {
                return FilterRegistration.this.setInitParameters(map);
            }

            @Override
            public Map<String, String> getInitParameters() {
                return FilterRegistration.this.getInitParameters();
            }

            @Override
            public void addMappingForServletNames(
                    EnumSet<jakarta.servlet.DispatcherType> enumSet, boolean b, String... strings) {
                FilterRegistration.this.addMappingForServletNames(
                        EnumSet.copyOf(
                                enumSet.stream()
                                        .map(DispatcherType::fromJakartaDispatcherType)
                                        .collect(Collectors.toSet())),
                        b,
                        strings);
            }

            @Override
            public Collection<String> getServletNameMappings() {
                return FilterRegistration.this.getServletNameMappings();
            }

            @Override
            public void addMappingForUrlPatterns(
                    EnumSet<jakarta.servlet.DispatcherType> enumSet, boolean b, String... strings) {
                FilterRegistration.this.addMappingForUrlPatterns(
                        EnumSet.copyOf(
                                enumSet.stream()
                                        .map(DispatcherType::fromJakartaDispatcherType)
                                        .collect(Collectors.toSet())),
                        b,
                        strings);
            }

            @Override
            public Collection<String> getUrlPatternMappings() {
                return FilterRegistration.this.getUrlPatternMappings();
            }
        };
    }

    static FilterRegistration fromJakartaFilterRegistration(
            jakarta.servlet.FilterRegistration from) {
        return new FilterRegistration() {
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
            public void addMappingForServletNames(
                    EnumSet<DispatcherType> dispatcherTypes,
                    boolean isMatchAfter,
                    String... servletNames) {
                from.addMappingForServletNames(
                        EnumSet.copyOf(
                                dispatcherTypes.stream()
                                        .map(DispatcherType::toJakartaDispatcherType)
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
                    EnumSet<DispatcherType> dispatcherTypes,
                    boolean isMatchAfter,
                    String... urlPatterns) {
                from.addMappingForUrlPatterns(
                        EnumSet.copyOf(
                                dispatcherTypes.stream()
                                        .map(DispatcherType::toJakartaDispatcherType)
                                        .collect(Collectors.toSet())),
                        isMatchAfter,
                        urlPatterns);
            }

            @Override
            public Collection<String> getUrlPatternMappings() {
                return from.getUrlPatternMappings();
            }

            @Override
            public jakarta.servlet.Registration toJakartaRegistration() {
                return from;
            }

            @Override
            public jakarta.servlet.FilterRegistration toJakartaFilterRegistration() {
                return from;
            }
        };
    }
}
