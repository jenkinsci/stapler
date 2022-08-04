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

public interface AsyncContext {
    String ASYNC_REQUEST_URI = "javax.servlet.async.request_uri";

    String ASYNC_CONTEXT_PATH = "javax.servlet.async.context_path";

    String ASYNC_MAPPING = "javax.servlet.async.mapping";

    String ASYNC_PATH_INFO = "javax.servlet.async.path_info";

    String ASYNC_SERVLET_PATH = "javax.servlet.async.servlet_path";

    String ASYNC_QUERY_STRING = "javax.servlet.async.query_string";

    ServletRequest getRequest();

    ServletResponse getResponse();

    boolean hasOriginalRequestAndResponse();

    void dispatch();

    void dispatch(String path);

    void dispatch(ServletContext context, String path);

    void complete();

    void start(Runnable run);

    void addListener(AsyncListener listener);

    void addListener(
            AsyncListener listener, ServletRequest servletRequest, ServletResponse servletResponse);

    <T extends AsyncListener> T createListener(Class<T> clazz) throws ServletException;

    void setTimeout(long timeout);

    long getTimeout();
}
