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

package org.kohsuke.stapler.jsp;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import java.io.IOException;

/**
 * {@link RequestDispatcher} that sets "it" before the invocation.
 *
 * @author Kohsuke Kawaguchi
 */
final class RequestDispatcherWrapper implements RequestDispatcher {
    private final RequestDispatcher core;
    private final Object it;

    RequestDispatcherWrapper(RequestDispatcher core, Object it) {
        this.core = core;
        this.it = it;
    }

    @Override
    @SuppressFBWarnings(
            value = "REQUESTDISPATCHER_FILE_DISCLOSURE",
            justification = "Forwarding the request to be handled correctly.")
    public void forward(ServletRequest req, ServletResponse rsp) throws ServletException, IOException {
        req.setAttribute("it", it);
        req.setAttribute("staplerRequest", req);
        req.setAttribute("staplerResponse", rsp);
        core.forward(req, rsp);
    }

    @Override
    @SuppressFBWarnings(
            value = "REQUESTDISPATCHER_FILE_DISCLOSURE",
            justification = "Forwarding the request to be handled correctly.")
    public void include(ServletRequest req, ServletResponse rsp) throws ServletException, IOException {
        Object oldIt = push(req, "it", it);
        Object oldRq = push(req, "staplerRequest", req);
        Object oldRs = push(req, "staplerResponse", rsp);
        try {
            core.include(req, rsp);
        } finally {
            req.setAttribute("it", oldIt);
            req.setAttribute("staplerRequest", oldRq);
            req.setAttribute("staplerResponse", oldRs);
        }
    }

    private Object push(ServletRequest req, String paramName, Object value) {
        Object old = req.getAttribute(paramName);
        req.setAttribute(paramName, value);
        return old;
    }
}
