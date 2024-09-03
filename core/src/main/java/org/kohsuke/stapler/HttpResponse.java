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

/**
 * Object that represents the HTTP response, which is defined as a capability to produce the response.
 *
 * <p>
 * {@code doXyz(...)} method could return an object of this type or throw an exception of this type, and if it does so,
 * the object is asked to produce HTTP response.
 *
 * <p>
 * This is useful to make {@code doXyz} look like a real function, and decouple it further from HTTP.
 *
 * @author Kohsuke Kawaguchi
 */
public interface HttpResponse {
    /**
     * @param node
     *      The object whose "doXyz" method created this object.
     */
    default void generateResponse(StaplerRequest2 req, StaplerResponse2 rsp, Object node)
            throws IOException, ServletException {
        if (ReflectionUtils.isOverridden(
                HttpResponse.class,
                getClass(),
                "generateResponse",
                StaplerRequest.class,
                StaplerResponse.class,
                Object.class)) {
            try {
                generateResponse(
                        req != null ? StaplerRequest.fromStaplerRequest2(req) : null,
                        rsp != null ? StaplerResponse.fromStaplerResponse2(rsp) : null,
                        node);
            } catch (javax.servlet.ServletException e) {
                throw ServletExceptionWrapper.toJakartaServletException(e);
            }
        } else {
            throw new AbstractMethodError("The class " + getClass().getName() + " must override at least one of the "
                    + HttpResponse.class.getSimpleName() + ".generateResponse methods");
        }
    }

    /**
     * @deprecated use {@link #generateResponse(StaplerRequest2, StaplerResponse2, Object)}
     */
    @Deprecated
    default void generateResponse(StaplerRequest req, StaplerResponse rsp, Object node)
            throws IOException, javax.servlet.ServletException {
        if (ReflectionUtils.isOverridden(
                HttpResponse.class,
                getClass(),
                "generateResponse",
                StaplerRequest2.class,
                StaplerResponse2.class,
                Object.class)) {
            try {
                generateResponse(
                        req != null ? StaplerRequest.toStaplerRequest2(req) : null,
                        rsp != null ? StaplerResponse.toStaplerResponse2(rsp) : null,
                        node);
            } catch (ServletException e) {
                throw ServletExceptionWrapper.fromJakartaServletException(e);
            }
        } else {
            throw new AbstractMethodError("The class " + getClass().getName() + " must override at least one of the "
                    + HttpResponse.class.getSimpleName() + ".generateResponse methods");
        }
    }
}
