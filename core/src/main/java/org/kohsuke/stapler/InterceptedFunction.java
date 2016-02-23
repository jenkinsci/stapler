package org.kohsuke.stapler;

import org.kohsuke.stapler.interceptor.Interceptor;
import org.kohsuke.stapler.interceptor.InterceptorAnnotation;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;

/**
 * Function that's wrapped by {@link Interceptor}.
 */
final class InterceptedFunction extends Function {
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

    @Override
    public Annotation[] getAnnotations() {
        return next.getAnnotations();
    }
}
