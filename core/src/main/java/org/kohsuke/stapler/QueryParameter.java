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

import org.kohsuke.stapler.QueryParameter.HandlerImpl;

import javax.servlet.ServletException;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.annotation.Documented;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Indicates that this parameter is injected from HTTP query parameter.
 *
 * @author Kohsuke Kawaguchi
 */
@Retention(RUNTIME)
@Target(PARAMETER)
@Documented
@InjectedParameter(HandlerImpl.class)
public @interface QueryParameter {
    /**
     * query parameter name. By default, name of the parameter.
     */
    String value() default "";

    /**
     * If true, request without this header will be rejected.
     */
    boolean required() default false;

    /**
     * If true, and the actual value of this parameter is "",
     * null is passed instead. This is useful to unify the treatment of
     * the absence of the value vs the empty value.
     */
    boolean fixEmpty() default false;

    class HandlerImpl extends AnnotationHandler<QueryParameter> {
        public Object parse(StaplerRequest request, QueryParameter a, Class type, String parameterName) throws ServletException {
            String name = a.value();
            if(name.length()==0)    name=parameterName;
            if(name==null)
                throw new IllegalArgumentException("Parameter name unavailable neither in the code nor in annotation");

            String value = request.getParameter(name);
            if(a.required() && value==null)
                throw new ServletException("Required Query parameter "+name+" is missing");
            if(a.fixEmpty() && value!=null && value.length()==0)
                value = null;
            return convert(type,value);
        }
    }
}
