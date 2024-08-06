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

import io.jenkins.servlet.ServletExceptionWrapper;
import jakarta.servlet.ServletException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

/**
 * Marks the object that can handle HTTP DELETE.
 *
 * @author Kohsuke Kawaguchi
 */
public interface HttpDeletable {
    /**
     * Called when HTTP DELETE method is invoked.
     */
    default void delete(StaplerRequest2 req, StaplerResponse2 rsp) throws IOException, ServletException {
        if (ReflectionUtils.isOverridden(
                HttpDeletable.class, getClass(), "delete", StaplerRequest.class, StaplerResponse.class)) {
            try {
                delete(StaplerRequest.fromStaplerRequest2(req), StaplerResponse.fromStaplerResponse2(rsp));
            } catch (javax.servlet.ServletException e) {
                throw ServletExceptionWrapper.toJakartaServletException(e);
            }
        } else {
            throw new AbstractMethodError("The class " + getClass().getName() + " must override at least one of the "
                    + HttpDeletable.class.getSimpleName() + ".delete methods");
        }
    }

    /**
     * @deprecated use {@link #delete(StaplerRequest2, StaplerResponse2)}
     */
    @Deprecated
    default void delete(StaplerRequest req, StaplerResponse rsp) throws IOException, javax.servlet.ServletException {
        if (ReflectionUtils.isOverridden(
                HttpDeletable.class, getClass(), "delete", StaplerRequest2.class, StaplerResponse2.class)) {
            try {
                delete(StaplerRequest.toStaplerRequest2(req), StaplerResponse.toStaplerResponse2(rsp));
            } catch (ServletException e) {
                throw ServletExceptionWrapper.fromJakartaServletException(e);
            }
        } else {
            throw new AbstractMethodError("The class " + getClass().getName() + " must override at least one of the "
                    + HttpDeletable.class.getSimpleName() + ".delete methods");
        }
    }

    /**
     * {@link Dispatcher} that processes {@link HttpDeletable}
     */
    class HttpDeletableDispatcher extends Dispatcher {
        @Override
        public boolean dispatch(RequestImpl req, ResponseImpl rsp, Object node)
                throws IOException, ServletException, IllegalAccessException, InvocationTargetException {
            if (!req.tokens.hasMore() && req.getMethod().equals("DELETE")) {
                ((HttpDeletable) node).delete(req, rsp);
                return true;
            }

            return false;
        }

        @Override
        public String toString() {
            return "delete() for url=/ with DELETE";
        }
    }
}
