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

public interface Servlet {
    void init(ServletConfig config) throws ServletException;

    ServletConfig getServletConfig();

    void service(ServletRequest req, ServletResponse res) throws ServletException, IOException;

    String getServletInfo();

    void destroy();

    default jakarta.servlet.Servlet toJakartaServlet() {
        return new jakarta.servlet.Servlet() {
            @Override
            public void init(jakarta.servlet.ServletConfig config) throws jakarta.servlet.ServletException {
                try {
                    Servlet.this.init(ServletConfig.fromJakartaServletConfig(config));
                } catch (ServletException e) {
                    throw new jakarta.servlet.ServletException(e);
                }
            }

            @Override
            public jakarta.servlet.ServletConfig getServletConfig() {
                return Servlet.this.getServletConfig().toJakartaServletConfig();
            }

            @Override
            public void service(jakarta.servlet.ServletRequest request, jakarta.servlet.ServletResponse response)
                    throws jakarta.servlet.ServletException, IOException {
                try {
                    if (request instanceof jakarta.servlet.http.HttpServletRequest
                            && response instanceof jakarta.servlet.http.HttpServletResponse) {
                        jakarta.servlet.http.HttpServletRequest httpRequest =
                                (jakarta.servlet.http.HttpServletRequest) request;
                        jakarta.servlet.http.HttpServletResponse httpResponse =
                                (jakarta.servlet.http.HttpServletResponse) response;
                        Servlet.this.service(
                                HttpServletRequest.fromJakartaHttpServletRequest(httpRequest),
                                HttpServletResponse.fromJakartaHttpServletResponse(httpResponse));
                    } else {
                        Servlet.this.service(
                                ServletRequest.fromJakartaServletRequest(request),
                                ServletResponse.fromJakartaServletResponse(response));
                    }
                } catch (ServletException e) {
                    throw new jakarta.servlet.ServletException(e);
                }
            }

            @Override
            public String getServletInfo() {
                return Servlet.this.getServletInfo();
            }

            @Override
            public void destroy() {
                Servlet.this.destroy();
            }
        };
    }

    static Servlet fromJakartaServlet(jakarta.servlet.Servlet from) {
        return new Servlet() {
            @Override
            public void init(ServletConfig config) throws ServletException {
                try {
                    from.init(config.toJakartaServletConfig());
                } catch (jakarta.servlet.ServletException e) {
                    throw new ServletException(e);
                }
            }

            @Override
            public ServletConfig getServletConfig() {
                return ServletConfig.fromJakartaServletConfig(from.getServletConfig());
            }

            @Override
            public void service(ServletRequest request, ServletResponse response) throws ServletException, IOException {
                try {
                    if (request instanceof HttpServletRequest && response instanceof HttpServletResponse) {
                        HttpServletRequest httpRequest = (HttpServletRequest) request;
                        HttpServletResponse httpResponse = (HttpServletResponse) response;
                        from.service(
                                httpRequest.toJakartaHttpServletRequest(), httpResponse.toJakartaHttpServletResponse());
                    } else {
                        from.service(request.toJakartaServletRequest(), response.toJakartaServletResponse());
                    }
                } catch (jakarta.servlet.ServletException e) {
                    throw new ServletException(e);
                }
            }

            @Override
            public String getServletInfo() {
                return from.getServletInfo();
            }

            @Override
            public void destroy() {
                from.destroy();
            }

            @Override
            public jakarta.servlet.Servlet toJakartaServlet() {
                return from;
            }
        };
    }
}
