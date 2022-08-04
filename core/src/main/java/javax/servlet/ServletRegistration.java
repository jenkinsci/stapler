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
import java.util.Map;
import java.util.Set;

public interface ServletRegistration extends Registration {
    Set<String> addMapping(String... urlPatterns);

    Collection<String> getMappings();

    String getRunAsRole();

    interface Dynamic extends ServletRegistration, Registration.Dynamic {
        void setLoadOnStartup(int loadOnStartup);

        Set<String> setServletSecurity(ServletSecurityElement constraint);

        void setMultipartConfig(MultipartConfigElement multipartConfig);

        void setRunAsRole(String roleName);

        default jakarta.servlet.ServletRegistration.Dynamic toJakartaServletRegistrationDynamic() {
            return new jakarta.servlet.ServletRegistration.Dynamic() {
                @Override
                public String getName() {
                    return ServletRegistration.Dynamic.this.getName();
                }

                @Override
                public String getClassName() {
                    return ServletRegistration.Dynamic.this.getClassName();
                }

                @Override
                public boolean setInitParameter(String s, String s1) {
                    return ServletRegistration.Dynamic.this.setInitParameter(s, s1);
                }

                @Override
                public String getInitParameter(String s) {
                    return ServletRegistration.Dynamic.this.getInitParameter(s);
                }

                @Override
                public Set<String> setInitParameters(Map<String, String> map) {
                    return ServletRegistration.Dynamic.this.setInitParameters(map);
                }

                @Override
                public Map<String, String> getInitParameters() {
                    return ServletRegistration.Dynamic.this.getInitParameters();
                }

                @Override
                public Set<String> addMapping(String... strings) {
                    return ServletRegistration.Dynamic.this.addMapping(strings);
                }

                @Override
                public Collection<String> getMappings() {
                    return ServletRegistration.Dynamic.this.getMappings();
                }

                @Override
                public String getRunAsRole() {
                    return ServletRegistration.Dynamic.this.getRunAsRole();
                }

                @Override
                public void setAsyncSupported(boolean b) {
                    ServletRegistration.Dynamic.this.setAsyncSupported(b);
                }

                @Override
                public void setLoadOnStartup(int i) {
                    ServletRegistration.Dynamic.this.setLoadOnStartup(i);
                }

                @Override
                public Set<String> setServletSecurity(
                        jakarta.servlet.ServletSecurityElement servletSecurityElement) {
                    // TODO
                    throw new UnsupportedOperationException();
                }

                @Override
                public void setMultipartConfig(
                        jakarta.servlet.MultipartConfigElement multipartConfigElement) {
                    // TODO
                    throw new UnsupportedOperationException();
                }

                @Override
                public void setRunAsRole(String s) {
                    ServletRegistration.Dynamic.this.setRunAsRole(s);
                }
            };
        }

        static ServletRegistration.Dynamic fromJakartaServletRegistrationDynamic(
                jakarta.servlet.ServletRegistration.Dynamic from) {
            return new ServletRegistration.Dynamic() {
                @Override
                public void setLoadOnStartup(int loadOnStartup) {
                    from.setLoadOnStartup(loadOnStartup);
                }

                @Override
                public Set<String> setServletSecurity(ServletSecurityElement constraint) {
                    // TODO
                    throw new UnsupportedOperationException();
                }

                @Override
                public void setMultipartConfig(MultipartConfigElement multipartConfig) {
                    // TODO
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

                @Override
                public jakarta.servlet.Registration toJakartaRegistration() {
                    return from;
                }

                @Override
                public jakarta.servlet.Registration.Dynamic toJakartaRegistrationDynamic() {
                    return from;
                }

                @Override
                public jakarta.servlet.ServletRegistration toJakartaServletRegistration() {
                    return from;
                }

                @Override
                public jakarta.servlet.ServletRegistration.Dynamic
                        toJakartaServletRegistrationDynamic() {
                    return from;
                }
            };
        }
    }

    default jakarta.servlet.ServletRegistration toJakartaServletRegistration() {
        return new jakarta.servlet.ServletRegistration() {
            @Override
            public String getName() {
                return ServletRegistration.this.getName();
            }

            @Override
            public String getClassName() {
                return ServletRegistration.this.getClassName();
            }

            @Override
            public boolean setInitParameter(String s, String s1) {
                return ServletRegistration.this.setInitParameter(s, s1);
            }

            @Override
            public String getInitParameter(String s) {
                return ServletRegistration.this.getInitParameter(s);
            }

            @Override
            public Set<String> setInitParameters(Map<String, String> map) {
                return ServletRegistration.this.setInitParameters(map);
            }

            @Override
            public Map<String, String> getInitParameters() {
                return ServletRegistration.this.getInitParameters();
            }

            @Override
            public Set<String> addMapping(String... strings) {
                return ServletRegistration.this.addMapping(strings);
            }

            @Override
            public Collection<String> getMappings() {
                return ServletRegistration.this.getMappings();
            }

            @Override
            public String getRunAsRole() {
                return ServletRegistration.this.getRunAsRole();
            }
        };
    }

    static ServletRegistration fromJakartaServletRegistration(
            jakarta.servlet.ServletRegistration from) {
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

            @Override
            public jakarta.servlet.Registration toJakartaRegistration() {
                return from;
            }

            @Override
            public jakarta.servlet.ServletRegistration toJakartaServletRegistration() {
                return from;
            }
        };
    }
}
