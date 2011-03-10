/*
 * Copyright (c) 2004-2010, Kohsuke Kawaguchi
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided
 * that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice, this list of
 *       conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright notice, this list of
 *       conditions and the following disclaimer in the documentation and/or other materials
 *       provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS
 * OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
 * AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER
 * IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF
 * THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.kohsuke.stapler;

import javax.servlet.ServletException;
import javax.servlet.RequestDispatcher;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * {@link HttpResponse} that forwards to a {@link RequestDispatcher}, such as a view.
 * Extends from {@link RuntimeException} so that you can throw it.
 *
 * @author Kohsuke Kawaguchi
 */
public class ForwardToView extends RuntimeException implements HttpResponse {
    private final DispatcherFactory factory;
    private boolean optional;
    private final Map<String,Object> attributes = new HashMap<String, Object>();

    private interface DispatcherFactory {
        RequestDispatcher get(StaplerRequest req) throws IOException;
    }

    public ForwardToView(final RequestDispatcher dispatcher) {
        this.factory = new DispatcherFactory() {
            public RequestDispatcher get(StaplerRequest req) {
                return dispatcher;
            }
        };
    }

    public ForwardToView(final Object it, final String view) {
        this.factory = new DispatcherFactory() {
            public RequestDispatcher get(StaplerRequest req) throws IOException {
                return req.getView(it,view);
            }
        };
    }

    public ForwardToView(final Class c, final String view) {
        this.factory = new DispatcherFactory() {
            public RequestDispatcher get(StaplerRequest req) throws IOException {
                return req.getView(c,view);
            }
        };
    }

    /**
     * Forwards to the view with specified attributes exposed as a variable binding.
     */
    public ForwardToView with(String varName, Object value) {
        attributes.put(varName,value);
        return this;
    }

    public ForwardToView with(Map<String,?> attributes) {
        this.attributes.putAll(attributes);
        return this;
    }

    /**
     * Make this forwarding optional. Render nothing if a view doesn't exist.
     */
    public ForwardToView optional() {
        optional = true;
        return this;
    }

    public void generateResponse(StaplerRequest req, StaplerResponse rsp, Object node) throws IOException, ServletException {
        for (Entry<String, Object> e : attributes.entrySet())
            req.setAttribute(e.getKey(),e.getValue());
        RequestDispatcher rd = factory.get(req);
        if (rd==null && optional)
            return;
        rd.forward(req, rsp);
    }
}
