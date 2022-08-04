/*
 * Copyright (c) 1997-2018 Oracle and/or its affiliates and others.
 * All rights reserved.
 * Copyright 2004 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package javax.servlet;

import java.util.Enumeration;

public interface ServletConfig {
    String getServletName();

    ServletContext getServletContext();

    String getInitParameter(String name);

    Enumeration<String> getInitParameterNames();

    default jakarta.servlet.ServletConfig toJakartaServletConfig() {
        return new jakarta.servlet.ServletConfig() {
            @Override
            public String getServletName() {
                return ServletConfig.this.getServletName();
            }

            @Override
            public jakarta.servlet.ServletContext getServletContext() {
                return ServletConfig.this.getServletContext().toJakartaServletContext();
            }

            @Override
            public String getInitParameter(String s) {
                return ServletConfig.this.getInitParameter(s);
            }

            @Override
            public Enumeration<String> getInitParameterNames() {
                return ServletConfig.this.getInitParameterNames();
            }
        };
    }

    static ServletConfig fromJakartaServletConfig(jakarta.servlet.ServletConfig from) {
        return new ServletConfig() {
            @Override
            public String getServletName() {
                return from.getServletName();
            }

            @Override
            public ServletContext getServletContext() {
                return ServletContext.fromJakartServletContext(from.getServletContext());
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
            public jakarta.servlet.ServletConfig toJakartaServletConfig() {
                return from;
            }
        };
    }
}
