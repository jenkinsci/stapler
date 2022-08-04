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

public interface FilterConfig {
    String getFilterName();

    ServletContext getServletContext();

    String getInitParameter(String name);

    Enumeration<String> getInitParameterNames();

    default jakarta.servlet.FilterConfig toJakartaFilterConfig() {
        return new jakarta.servlet.FilterConfig() {
            @Override
            public String getFilterName() {
                return FilterConfig.this.getFilterName();
            }

            @Override
            public jakarta.servlet.ServletContext getServletContext() {
                return FilterConfig.this.getServletContext().toJakartaServletContext();
            }

            @Override
            public String getInitParameter(String s) {
                return FilterConfig.this.getInitParameter(s);
            }

            @Override
            public Enumeration<String> getInitParameterNames() {
                return FilterConfig.this.getInitParameterNames();
            }
        };
    }

    static FilterConfig fromJakartaFilterConfig(jakarta.servlet.FilterConfig from) {
        return new FilterConfig() {
            @Override
            public String getFilterName() {
                return from.getFilterName();
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
            public jakarta.servlet.FilterConfig toJakartaFilterConfig() {
                return from;
            }
        };
    }
}
