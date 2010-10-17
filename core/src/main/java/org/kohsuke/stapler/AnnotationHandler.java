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

/**
 * Handles stapler parameter annotations by determining what values to inject for a method call.
 *
 * @author Kohsuke Kawaguchi
 */
abstract class AnnotationHandler<T extends Annotation> {
    abstract Object parse(StaplerRequest request, T a, Class type, String parameterName) throws ServletException;

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
            AnnotationHandler h = HANDLERS.get(a.annotationType());
            if(h==null)     continue;
            return h.parse(request,a,targetType,parameterName);
        }

        return null; // probably we should report an error
    }


    static final Map<Class<? extends Annotation>,AnnotationHandler> HANDLERS = new HashMap<Class<? extends Annotation>, AnnotationHandler>();

    static {
        HANDLERS.put(Header.class,new AnnotationHandler<Header>() {
            Object parse(StaplerRequest request, Header a, Class type, String parameterName) throws ServletException {
                String name = a.value();
                if(name.length()==0)    name=parameterName;
                if(name==null)
                    throw new IllegalArgumentException("Parameter name unavailable neither in the code nor in annotation");

                String value = request.getHeader(name);
                if(a.required() && value==null)
                    throw new ServletException("Required HTTP header "+name+" is missing");

                return convert(type,value);
            }
        });

        HANDLERS.put(QueryParameter.class,new AnnotationHandler<QueryParameter>() {
            Object parse(StaplerRequest request, QueryParameter a, Class type, String parameterName) throws ServletException {
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
        });

        HANDLERS.put(AncestorInPath.class,new AnnotationHandler<AncestorInPath>() {
            Object parse(StaplerRequest request, AncestorInPath a, Class type, String parameterName) throws ServletException {
                return request.findAncestorObject(type);
            }
        });
    }
}
