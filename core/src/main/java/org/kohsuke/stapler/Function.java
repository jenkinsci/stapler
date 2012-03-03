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

import com.google.common.collect.MapMaker;
import org.kohsuke.stapler.interceptor.Interceptor;
import org.kohsuke.stapler.interceptor.InterceptorAnnotation;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Map;

/**
 * Abstracts the difference between normal instance methods and
 * static duck-typed methods.
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class Function {
    /**
     * Gets the method name.
     */
    public abstract String getName();

    /**
     * Gets the human readable name of this function. Used to assist debugging.
     */
    public abstract String getDisplayName();

    /**
     * Gets "className.methodName"
     */
    public abstract String getQualifiedName();

    /**
     * Gets the type of parameters in a single array.
     */
    public abstract Class[] getParameterTypes();

    public abstract Type[] getGenericParameterTypes();

    /**
     * Gets the annotations on parameters.
     */
    public abstract Annotation[][] getParameterAnnotations();

    /**
     * Gets the list of parameter names.
     */
    public abstract String[] getParameterNames();

    /**
     * Return type of the method.
     */
    public abstract Class getReturnType();

    /**
     * Calls {@link #bindAndInvoke(Object, StaplerRequest, StaplerResponse, Object...)} and then
     * optionally serve the response object.
     */
    void bindAndInvokeAndServeResponse(Object node, RequestImpl req, ResponseImpl rsp, Object... headArgs) throws IllegalAccessException, InvocationTargetException, ServletException, IOException {
        try {
            Object r = bindAndInvoke(node, req, rsp, headArgs);
            if (getReturnType()!=void.class)
                renderResponse(req,rsp,node, r);
        } catch (InvocationTargetException e) {
            // exception as an HttpResponse
            Throwable te = e.getTargetException();
            if (!renderResponse(req,rsp,node,te))
                throw e;    // unprocessed exception
        }
    }

    private boolean renderResponse(RequestImpl req, ResponseImpl rsp, Object node, Object ret) throws IOException, ServletException {
        for (HttpResponseRenderer r : req.stapler.getWebApp().getResponseRenderers())
            if (r.generateResponse(req,rsp,node,ret))
                return true;
        return false;
    }

    /**
     * Use the given arguments as the first N arguments,
     * then figure out the rest of the arguments by looking at parameter annotations,
     * then finally call {@link #invoke}.
     */
    Object bindAndInvoke(Object o, StaplerRequest req, StaplerResponse rsp, Object... headArgs) throws IllegalAccessException, InvocationTargetException, ServletException {
        Class[] types = getParameterTypes();
        Annotation[][] annotations = getParameterAnnotations();
        String[] parameterNames = getParameterNames();

        Object[] arguments = new Object[types.length];

        // fill in the first N arguments
        System.arraycopy(headArgs,0,arguments,0,headArgs.length);

        try {
            // find the rest of the arguments. either known types, or with annotations
            for( int i=headArgs.length; i<types.length; i++ ) {
                Class t = types[i];
                if(t==StaplerRequest.class || t==HttpServletRequest.class) {
                    arguments[i] = req;
                    continue;
                }
                if(t==StaplerResponse.class || t==HttpServletResponse.class) {
                    arguments[i] = rsp;
                    continue;
                }

                // if the databinding method is provided, call that
                Function binder = PARSE_METHODS.get(t);
                if (binder!=RETURN_NULL) {
                    arguments[i] = binder.bindAndInvoke(null,req,rsp);
                    continue;
                }
                
                arguments[i] = AnnotationHandler.handle(req,annotations[i],
                    i<parameterNames.length ? parameterNames[i] : null,
                    t);
            }
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Failed to invoke "+getDisplayName(),e);
        }

        return invoke(req, rsp, o,arguments);
    }

    /**
     * Computing map that discovers the static 'fromStapler' method from a class.
     * The discovered method will be returned as a Function so that the invocation can do parameter injections.
     */
    private static final Map<Class,Function> PARSE_METHODS;
    private static final Function RETURN_NULL;

    static {
        try {
            RETURN_NULL = new StaticFunction(Function.class.getMethod("returnNull"));
        } catch (NoSuchMethodException e) {
            throw new AssertionError(e);    // impossible
        }

        PARSE_METHODS = new MapMaker().weakKeys().makeComputingMap(new com.google.common.base.Function<Class,Function>() {
            public Function apply(Class from) {
                // MethdFunction for invoking a static method as a static method
                FunctionList methods = new ClassDescriptor(from).methods.name("fromStapler");
                switch (methods.size()) {
                case 0: return RETURN_NULL;
                default:
                    throw new IllegalArgumentException("Too many 'fromStapler' methods on "+from);
                case 1:
                    Method m = ((MethodFunction)methods.get(0)).m;
                    return new MethodFunction(m) {
                        @Override
                        public Class[] getParameterTypes() {
                            return m.getParameterTypes();
                        }

                        @Override
                        public Type[] getGenericParameterTypes() {
                            return m.getGenericParameterTypes();
                        }

                        @Override
                        public Annotation[][] getParameterAnnotations() {
                            return m.getParameterAnnotations();
                        }

                        @Override
                        public Object invoke(StaplerRequest req, StaplerResponse rsp, Object o, Object... args) throws IllegalAccessException, InvocationTargetException {
                            return m.invoke(null,args);
                        }
                    };
                }
            }
        });
    }

    public static Object returnNull() { return null; }

    /**
     * Invokes the method.
     */
    public abstract Object invoke(StaplerRequest req, StaplerResponse rsp, Object o, Object... args) throws IllegalAccessException, InvocationTargetException;

    final Function wrapByInterceptors(Method m) {
        try {
            Function f = this;
            for (Annotation a : m.getAnnotations()) {
                final InterceptorAnnotation ia = a.annotationType().getAnnotation(InterceptorAnnotation.class);
                if (ia!=null) {
                    f = new InterceptedFunction(f,ia);
                }
            }

            return f;
        } catch (LinkageError e) {
            // running in JDK 1.4
            return this;
        }
    }

    public abstract <A extends Annotation> A getAnnotation(Class<A> annotation);

    private abstract static class MethodFunction extends Function {
        protected final Method m;
        private volatile String[] names;

        public MethodFunction(Method m) {
            this.m = m;
        }

        public final String getName() {
            return m.getName();
        }

        public final String getDisplayName() {
            return m.toGenericString();
        }

        @Override
        public String getQualifiedName() {
            return m.getDeclaringClass().getName()+'.'+getName();
        }

        public final <A extends Annotation> A getAnnotation(Class<A> annotation) {
            return m.getAnnotation(annotation);
        }

        public final String[] getParameterNames() {
            if(names==null)
                names = ClassDescriptor.loadParameterNames(m);
            return names;
        }

        @Override
        public Class getReturnType() {
            return m.getReturnType();
        }
    }
    /**
     * Normal instance methods.
     */
    static final class InstanceFunction extends MethodFunction {
        public InstanceFunction(Method m) {
            super(m);
        }

        public Class[] getParameterTypes() {
            return m.getParameterTypes();
        }

        @Override
        public Type[] getGenericParameterTypes() {
            return m.getGenericParameterTypes();
        }

        public Annotation[][] getParameterAnnotations() {
            return m.getParameterAnnotations();
        }

        public Object invoke(StaplerRequest req, StaplerResponse rsp, Object o, Object... args) throws IllegalAccessException, InvocationTargetException {
            return m.invoke(o,args);
        }
    }

    /**
     * Static methods on the wrapper type.
     */
    static final class StaticFunction extends MethodFunction {
        public StaticFunction(Method m) {
            super(m);
        }

        public Class[] getParameterTypes() {
            Class[] p = m.getParameterTypes();
            Class[] r = new Class[p.length-1];
            System.arraycopy(p,1,r,0,r.length);
            return r;
        }

        @Override
        public Type[] getGenericParameterTypes() {
            Type[] p = m.getGenericParameterTypes();
            Type[] r = new Type[p.length-1];
            System.arraycopy(p,1,r,0,r.length);
            return r;
        }

        public Annotation[][] getParameterAnnotations() {
            Annotation[][] a = m.getParameterAnnotations();
            Annotation[][] r = new Annotation[a.length-1][];
            System.arraycopy(a,1,r,0,r.length);
            return r;
        }

        public Object invoke(StaplerRequest req, StaplerResponse rsp, Object o, Object... args) throws IllegalAccessException, InvocationTargetException {
            Object[] r = new Object[args.length+1];
            r[0] = o;
            System.arraycopy(args,0,r,1,args.length);
            return m.invoke(null,r);
        }
    }

    /**
     * Function that's wrapped by {@link Interceptor}.
     */
    static final class InterceptedFunction extends Function {
        private final Function next;
        private final Interceptor interceptor;

        public InterceptedFunction(Function next, InterceptorAnnotation ia) {
            this.next = next;
            this.interceptor = instantiate(ia);
            interceptor.setTarget(next);
        }

        private Interceptor instantiate(InterceptorAnnotation ia) {
            try {
                return ia.value().newInstance();
            } catch (InstantiationException e) {
                throw (Error)new InstantiationError("Failed to instantiate interceptor for "+next.getDisplayName()).initCause(e);
            } catch (IllegalAccessException e) {
                throw (Error)new IllegalAccessError("Failed to instantiate interceptor for "+next.getDisplayName()).initCause(e);
            }
        }
        
        public String getName() {
            return next.getName();
        }

        public String getDisplayName() {
            return next.getDisplayName();
        }

        @Override
        public String getQualifiedName() {
            return next.getQualifiedName();
        }

        public Class[] getParameterTypes() {
            return next.getParameterTypes();
        }

        @Override
        public Class getReturnType() {
            return next.getReturnType();
        }

        @Override
        public Type[] getGenericParameterTypes() {
            return next.getGenericParameterTypes();
        }

        public Annotation[][] getParameterAnnotations() {
            return next.getParameterAnnotations();
        }

        public String[] getParameterNames() {
            return next.getParameterNames();
        }

        public Object invoke(StaplerRequest req, StaplerResponse rsp, Object o, Object... args) throws IllegalAccessException, InvocationTargetException {
            return interceptor.invoke(req, rsp, o, args);
        }

        public <A extends Annotation> A getAnnotation(Class<A> annotation) {
            return next.getAnnotation(annotation);
        }
    }
}
