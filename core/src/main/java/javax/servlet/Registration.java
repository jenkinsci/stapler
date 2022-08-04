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

import java.util.Map;
import java.util.Set;

public interface Registration {
    String getName();

    String getClassName();

    boolean setInitParameter(String name, String value);

    String getInitParameter(String name);

    Set<String> setInitParameters(Map<String, String> initParameters);

    Map<String, String> getInitParameters();

    interface Dynamic extends Registration {
        void setAsyncSupported(boolean isAsyncSupported);

        default jakarta.servlet.Registration.Dynamic toJakartaRegistrationDynamic() {
            return new jakarta.servlet.Registration.Dynamic() {
                @Override
                public String getName() {
                    return Registration.Dynamic.this.getName();
                }

                @Override
                public String getClassName() {
                    return Registration.Dynamic.this.getClassName();
                }

                @Override
                public boolean setInitParameter(String s, String s1) {
                    return Registration.Dynamic.this.setInitParameter(s, s1);
                }

                @Override
                public String getInitParameter(String s) {
                    return Registration.Dynamic.this.getInitParameter(s);
                }

                @Override
                public Set<String> setInitParameters(Map<String, String> map) {
                    return Registration.Dynamic.this.setInitParameters(map);
                }

                @Override
                public Map<String, String> getInitParameters() {
                    return Registration.Dynamic.this.getInitParameters();
                }

                @Override
                public void setAsyncSupported(boolean b) {
                    Registration.Dynamic.this.setAsyncSupported(b);
                }
            };
        }

        static Registration.Dynamic fromJakartaRegistrationDynamic(
                jakarta.servlet.Registration.Dynamic from) {
            return new Registration.Dynamic() {
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
                public jakarta.servlet.Registration toJakartaRegistration() {
                    return from;
                }

                @Override
                public jakarta.servlet.Registration.Dynamic toJakartaRegistrationDynamic() {
                    return from;
                }
            };
        }
    }

    default jakarta.servlet.Registration toJakartaRegistration() {
        return new jakarta.servlet.Registration() {
            @Override
            public String getName() {
                return Registration.this.getName();
            }

            @Override
            public String getClassName() {
                return Registration.this.getClassName();
            }

            @Override
            public boolean setInitParameter(String s, String s1) {
                return Registration.this.setInitParameter(s, s1);
            }

            @Override
            public String getInitParameter(String s) {
                return Registration.this.getInitParameter(s);
            }

            @Override
            public Set<String> setInitParameters(Map<String, String> map) {
                return Registration.this.setInitParameters(map);
            }

            @Override
            public Map<String, String> getInitParameters() {
                return Registration.this.getInitParameters();
            }
        };
    }

    static Registration fromJakartaRegistration(jakarta.servlet.Registration from) {
        return new Registration() {
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
        };
    }
}
