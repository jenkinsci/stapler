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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.IOException;

@SuppressFBWarnings(
        value = "REQUESTDISPATCHER_FILE_DISCLOSURE",
        justification = "TODO needs triage")
public interface RequestDispatcher {
    String FORWARD_REQUEST_URI = "javax.servlet.forward.request_uri";

    String FORWARD_CONTEXT_PATH = "javax.servlet.forward.context_path";

    String FORWARD_MAPPING = "javax.servlet.forward.mapping";

    String FORWARD_PATH_INFO = "javax.servlet.forward.path_info";

    String FORWARD_SERVLET_PATH = "javax.servlet.forward.servlet_path";

    String FORWARD_QUERY_STRING = "javax.servlet.forward.query_string";

    String INCLUDE_REQUEST_URI = "javax.servlet.include.request_uri";

    String INCLUDE_CONTEXT_PATH = "javax.servlet.include.context_path";

    String INCLUDE_PATH_INFO = "javax.servlet.include.path_info";

    String INCLUDE_MAPPING = "javax.servlet.include.mapping";

    String INCLUDE_SERVLET_PATH = "javax.servlet.include.servlet_path";

    String INCLUDE_QUERY_STRING = "javax.servlet.include.query_string";

    String ERROR_EXCEPTION = "javax.servlet.error.exception";

    String ERROR_EXCEPTION_TYPE = "javax.servlet.error.exception_type";

    String ERROR_MESSAGE = "javax.servlet.error.message";

    String ERROR_REQUEST_URI = "javax.servlet.error.request_uri";

    String ERROR_SERVLET_NAME = "javax.servlet.error.servlet_name";

    String ERROR_STATUS_CODE = "javax.servlet.error.status_code";

    void forward(ServletRequest request, ServletResponse response)
            throws ServletException, IOException;

    void include(ServletRequest request, ServletResponse response)
            throws ServletException, IOException;

    default jakarta.servlet.RequestDispatcher toJakartaRequestDispatcher() {
        return new jakarta.servlet.RequestDispatcher() {
            @Override
            public void forward(
                    jakarta.servlet.ServletRequest servletRequest,
                    jakarta.servlet.ServletResponse servletResponse)
                    throws jakarta.servlet.ServletException, IOException {
                try {
                    RequestDispatcher.this.forward(
                            ServletRequest.fromJakartaServletRequest(servletRequest),
                            ServletResponse.fromJakartaServletResponse(servletResponse));
                } catch (ServletException e) {
                    throw new jakarta.servlet.ServletException(e);
                }
            }

            @Override
            public void include(
                    jakarta.servlet.ServletRequest servletRequest,
                    jakarta.servlet.ServletResponse servletResponse)
                    throws jakarta.servlet.ServletException, IOException {
                try {
                    RequestDispatcher.this.include(
                            ServletRequest.fromJakartaServletRequest(servletRequest),
                            ServletResponse.fromJakartaServletResponse(servletResponse));
                } catch (ServletException e) {
                    throw new jakarta.servlet.ServletException(e);
                }
            }
        };
    }

    static RequestDispatcher fromJakartaRequestDispatcher(jakarta.servlet.RequestDispatcher from) {
        return new RequestDispatcher() {
            @Override
            public void forward(ServletRequest request, ServletResponse response)
                    throws ServletException, IOException {
                try {
                    from.forward(
                            request.toJakartaServletRequest(), response.toJakartaServletResponse());
                } catch (jakarta.servlet.ServletException e) {
                    throw new ServletException(e);
                }
            }

            @Override
            public void include(ServletRequest request, ServletResponse response)
                    throws ServletException, IOException {
                try {
                    from.include(
                            request.toJakartaServletRequest(), response.toJakartaServletResponse());
                } catch (jakarta.servlet.ServletException e) {
                    throw new ServletException(e);
                }
            }

            @Override
            public jakarta.servlet.RequestDispatcher toJakartaRequestDispatcher() {
                return from;
            }
        };
    }
}
