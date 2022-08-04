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

import java.io.IOException;

public interface FilterChain {
    void doFilter(ServletRequest request, ServletResponse response)
            throws IOException, ServletException;

    default jakarta.servlet.FilterChain toJakartaFilterChain() {
        return new jakarta.servlet.FilterChain() {
            @Override
            public void doFilter(
                    jakarta.servlet.ServletRequest servletRequest,
                    jakarta.servlet.ServletResponse servletResponse)
                    throws IOException, jakarta.servlet.ServletException {
                try {
                    FilterChain.this.doFilter(
                            ServletRequest.fromJakartaServletRequest(servletRequest),
                            ServletResponse.fromJakartaServletResponse(servletResponse));
                } catch (ServletException e) {
                    throw new jakarta.servlet.ServletException(e);
                }
            }
        };
    }

    static FilterChain fromJakartaFilterChain(jakarta.servlet.FilterChain from) {
        return new FilterChain() {
            @Override
            public void doFilter(ServletRequest request, ServletResponse response)
                    throws IOException, ServletException {
                try {
                    from.doFilter(
                            request.toJakartaServletRequest(), response.toJakartaServletResponse());
                } catch (jakarta.servlet.ServletException e) {
                    throw new ServletException(e);
                }
            }

            @Override
            public jakarta.servlet.FilterChain toJakartaFilterChain() {
                return from;
            }
        };
    }
}
