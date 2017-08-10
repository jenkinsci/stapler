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
import java.lang.annotation.Documented;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import javax.servlet.http.HttpServletRequest;
import org.kohsuke.stapler.Function;
import org.kohsuke.stapler.interceptor.Interceptor;
import org.kohsuke.stapler.interceptor.InterceptorAnnotation;
import org.kohsuke.stapler.interceptor.Stage;
import org.kohsuke.stapler.lang.MethodRef;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target(METHOD)
@Retention(RUNTIME)
@Documented
@Repeatable(StaplerMethods.class)
@StaplerPath.Implicit(methodPrefix = "do")
@InterceptorAnnotation(value = StaplerMethodInterceptor.class, stage = Stage.SELECTION)
public @interface StaplerMethod {
    /**
     * Special constant used to indicate that the annotated method matches all {@link HttpServletRequest#getMethod()}s.
     */
    String ALL = "*";

    String value();

    /**
     * Helper class that consolidates the rules for determining if a method is a valid stapler method.
     */
    class Helper {

        private Helper() {
            throw new IllegalAccessError("Utility class");
        }

        public static boolean isMethod(Function method) {
            return isMethod(method.getAnnotations());
        }

        public static boolean isMethod(Method method) {
            return Modifier.isPublic(method.getModifiers()) && isMethod(method.getAnnotations());
        }

        public static boolean isMethod(MethodRef method) {
            return method.isRoutable() && isMethod(method.getAnnotations());
        }

        private static boolean isMethod(Annotation[] annotations) {
            for (Annotation a : annotations) {
                InterceptorAnnotation interceptor = a.annotationType().getAnnotation(InterceptorAnnotation.class);
                if (interceptor != null && StaplerMethodInterceptor.class.equals(interceptor.value())) {
                    return true;
                }
            }
            return false;
        }

    }
}