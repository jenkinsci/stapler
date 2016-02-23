package org.kohsuke.stapler;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;

/**
 * {@link Function} that forwards calls to another. Usually used
 * as a basis of decorator.
 *
 * @author Kohsuke Kawaguchi
 */
/*package*/ class ForwardingFunction extends Function {
    protected final Function next;

    public ForwardingFunction(Function next) {
        this.next = next;
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
        return next.invoke(req, rsp, o, args);
    }

    public <A extends Annotation> A getAnnotation(Class<A> annotation) {
        return next.getAnnotation(annotation);
    }

    @Override
    public Annotation[] getAnnotations() {
        return next.getAnnotations();
    }
}

