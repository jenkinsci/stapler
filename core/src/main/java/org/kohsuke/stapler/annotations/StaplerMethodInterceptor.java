/*
 * Copyright (c) 2017, Stephen Connolly, CloudBees, Inc.
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

package org.kohsuke.stapler.annotations;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import javax.servlet.ServletException;
import org.kohsuke.stapler.CancelRequestHandlingException;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.interceptor.Interceptor;
import org.kohsuke.stapler.interceptor.InterceptorAnnotation;

public class StaplerMethodInterceptor extends Interceptor {
    @Override
    public Object invoke(StaplerRequest request, StaplerResponse response, Object instance, Object[] arguments)
            throws IllegalAccessException,
            InvocationTargetException, ServletException {
        if (matches(request)) {
            return target.invoke(request, response, instance, arguments);
        } else {
            throw new CancelRequestHandlingException();
        }
    }

    private boolean matches(StaplerRequest request) {
        String method = request.getMethod();
        String override = request.getHeader("X-HTTP-Method-Override");
        if (override != null) {
            method = override;
        }

        for (Annotation a : target.getAnnotations()) {
            if (a instanceof StaplerMethods) {
                for (StaplerMethod sa : ((StaplerMethods) a).value()) {
                    if (sa.value().equals(method)) {
                        return true;
                    }
                }
            } else if (a instanceof StaplerMethod) {
                if (((StaplerMethod) a).value().equals(method)) {
                    return true;
                }
            } else {
                Class<? extends Annotation> t = a.annotationType();
                InterceptorAnnotation ia = t.getAnnotation(InterceptorAnnotation.class);
                if (ia != null && ia.value() == StaplerMethodInterceptor.class) {
                    if (t.getSimpleName().equals("Stapler" + method)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
