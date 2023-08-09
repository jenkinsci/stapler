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
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface Filter {
    default void init(FilterConfig filterConfig) throws ServletException {}

    void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException;

    default void destroy() {}

    default jakarta.servlet.Filter toJakartaFilter() {
        return new jakarta.servlet.Filter() {
            @Override
            public void init(jakarta.servlet.FilterConfig filterConfig) throws jakarta.servlet.ServletException {
                try {
                    Filter.this.init(FilterConfig.fromJakartaFilterConfig(filterConfig));
                } catch (ServletException e) {
                    throw new jakarta.servlet.ServletException(e);
                }
            }

            @Override
            public void doFilter(
                    jakarta.servlet.ServletRequest request,
                    jakarta.servlet.ServletResponse response,
                    jakarta.servlet.FilterChain chain)
                    throws IOException, jakarta.servlet.ServletException {
                try {
                    if (request instanceof jakarta.servlet.http.HttpServletRequest
                            && response instanceof jakarta.servlet.http.HttpServletResponse) {
                        jakarta.servlet.http.HttpServletRequest httpRequest =
                                (jakarta.servlet.http.HttpServletRequest) request;
                        jakarta.servlet.http.HttpServletResponse httpResponse =
                                (jakarta.servlet.http.HttpServletResponse) response;
                        Filter.this.doFilter(
                                HttpServletRequest.fromJakartaHttpServletRequest(httpRequest),
                                HttpServletResponse.fromJakartaHttpServletResponse(httpResponse),
                                FilterChain.fromJakartaFilterChain(chain));
                    } else {
                        Filter.this.doFilter(
                                ServletRequest.fromJakartaServletRequest(request),
                                ServletResponse.fromJakartaServletResponse(response),
                                FilterChain.fromJakartaFilterChain(chain));
                    }
                } catch (ServletException e) {
                    throw new jakarta.servlet.ServletException(e);
                }
            }

            @Override
            public void destroy() {
                Filter.this.destroy();
            }
        };
    }

    static Filter fromJakartaFilter(jakarta.servlet.Filter from) {
        return new Filter() {
            @Override
            public void init(FilterConfig filterConfig) throws ServletException {
                try {
                    from.init(filterConfig.toJakartaFilterConfig());
                } catch (jakarta.servlet.ServletException e) {
                    throw new ServletException(e);
                }
            }

            @Override
            public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
                    throws IOException, ServletException {
                try {
                    if (request instanceof HttpServletRequest && response instanceof HttpServletResponse) {
                        HttpServletRequest httpRequest = (HttpServletRequest) request;
                        HttpServletResponse httpResponse = (HttpServletResponse) response;
                        from.doFilter(
                                httpRequest.toJakartaHttpServletRequest(),
                                httpResponse.toJakartaHttpServletResponse(),
                                chain.toJakartaFilterChain());
                    } else {
                        from.doFilter(
                                request.toJakartaServletRequest(),
                                response.toJakartaServletResponse(),
                                chain.toJakartaFilterChain());
                    }
                } catch (jakarta.servlet.ServletException e) {
                    throw new ServletException(e);
                }
            }

            @Override
            public void destroy() {
                from.destroy();
            }

            @Override
            public jakarta.servlet.Filter toJakartaFilter() {
                return from;
            }
        };
    }
}
