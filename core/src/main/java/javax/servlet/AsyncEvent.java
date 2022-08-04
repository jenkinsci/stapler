/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates and others.
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

public class AsyncEvent {
    private AsyncContext context;
    private ServletRequest request;
    private ServletResponse response;
    private Throwable throwable;

    public AsyncEvent(AsyncContext context) {
        this(context, context.getRequest(), context.getResponse(), null);
    }

    public AsyncEvent(AsyncContext context, ServletRequest request, ServletResponse response) {
        this(context, request, response, null);
    }

    public AsyncEvent(AsyncContext context, Throwable throwable) {
        this(context, context.getRequest(), context.getResponse(), throwable);
    }

    public AsyncEvent(
            AsyncContext context,
            ServletRequest request,
            ServletResponse response,
            Throwable throwable) {
        this.context = context;
        this.request = request;
        this.response = response;
        this.throwable = throwable;
    }

    public AsyncContext getAsyncContext() {
        return context;
    }

    public ServletRequest getSuppliedRequest() {
        return request;
    }

    public ServletResponse getSuppliedResponse() {
        return response;
    }

    public Throwable getThrowable() {
        return throwable;
    }
}
