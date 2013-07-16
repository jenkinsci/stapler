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

import org.apache.commons.beanutils.Converter;

import javax.servlet.ServletException;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Handles stapler parameter annotations by determining what values to inject for a method call.
 *
 * @author Kohsuke Kawaguchi
 * @see InjectedParameter
 */
public abstract class AnnotationHandler<T extends Annotation> {
    /**
     *
     * @param request
     *      Current request being processed. Normally the parameter injection grabs some value from here and returns it.
     *      Never null.
     * @param a
     *      The annotation object attached on the parameter for which this handler is configured. Never null
     * @param type
     *      The type of the parameter. Any value returned from this method must be assignable to this type.
     *      Never null.
     * @param parameterName
     *      Name of the parameter.
     */
    public abstract Object parse(StaplerRequest request, T a, Class type, String parameterName) throws ServletException;

    /**
     * Helper method for {@link #parse(StaplerRequest, Annotation, Class, String)} to convert to the right type
     * from String.
     */
    protected final Object convert(Class targetType, String value) {
        Converter converter = Stapler.lookupConverter(targetType);
        if (converter==null)
            throw new IllegalArgumentException("Unable to convert to "+targetType);

        return converter.convert(targetType,value);
    }

    static Object handle(StaplerRequest request, Annotation[] annotations, String parameterName, Class targetType) throws ServletException {
        for (Annotation a : annotations) {
            Class<? extends Annotation> at = a.annotationType();
            AnnotationHandler h = HANDLERS.get(at);
            if (h==null) {
                InjectedParameter ip = at.getAnnotation(InjectedParameter.class);
                if (ip!=null) {
                    try {
                        h = ip.value().newInstance();
                    } catch (InstantiationException e) {
                        throw new ServletException("Failed to instantiate parameter injector for "+at,e);
                    } catch (IllegalAccessException e) {
                        throw new ServletException("Failed to instantiate parameter injector for "+at,e);
                    }
                } else {
                    h = NOT_HANDLER;
                }
                AnnotationHandler prev = HANDLERS.putIfAbsent(at, h);
                if (prev!=null) h=prev;
            }
            if (h==NOT_HANDLER)
                continue;
            return h.parse(request,a,targetType,parameterName);
        }

        return null; // probably we should report an error
    }

    private static final ConcurrentMap<Class<? extends Annotation>,AnnotationHandler> HANDLERS = new ConcurrentHashMap<Class<? extends Annotation>, AnnotationHandler>();

    private static final AnnotationHandler NOT_HANDLER = new AnnotationHandler() {
        @Override
        public Object parse(StaplerRequest request, Annotation a, Class type, String parameterName) throws ServletException {
            return null;
        }
    };

    static {
        // synchronize with CaptureParameterNameTransformation.HANDLER_ANN

    }
}
