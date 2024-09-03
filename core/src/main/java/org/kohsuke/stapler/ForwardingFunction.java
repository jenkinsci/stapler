package org.kohsuke.stapler;

import jakarta.servlet.ServletException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;

/**
 * {@link Function} that forwards calls to another. Usually used
 * as a basis of decorator.
 *
 * @author Kohsuke Kawaguchi
 */
public class ForwardingFunction extends Function {
    protected final Function next;

    public ForwardingFunction(Function next) {
        this.next = next;
    }

    @Override
    public String getName() {
        return next.getName();
    }

    @Override
    public String getDisplayName() {
        return next.getDisplayName();
    }

    @Override
    public boolean isStatic() {
        return next.isStatic();
    }

    @Override
    public String getQualifiedName() {
        return next.getQualifiedName();
    }

    @Override
    public Class[] getParameterTypes() {
        return next.getParameterTypes();
    }

    @Override
    public Class getReturnType() {
        return next.getReturnType();
    }

    @Override
    public Class[] getCheckedExceptionTypes() {
        return next.getCheckedExceptionTypes();
    }

    @Override
    public Class getDeclaringClass() {
        return next.getDeclaringClass();
    }

    // can't really call next.contextualize()
    @Override
    public Function contextualize(Object usage) {
        return this;
    }

    @Override
    public Type[] getGenericParameterTypes() {
        return next.getGenericParameterTypes();
    }

    @Override
    public String getSignature() {
        return next.getSignature();
    }

    @Override
    public Annotation[][] getParameterAnnotations() {
        return next.getParameterAnnotations();
    }

    @Override
    public String[] getParameterNames() {
        return next.getParameterNames();
    }

    @Override
    public Object invoke(StaplerRequest2 req, StaplerResponse2 rsp, Object o, Object... args)
            throws IllegalAccessException, InvocationTargetException, ServletException {
        return next.invoke(req, rsp, o, args);
    }

    @Override
    public <A extends Annotation> A getAnnotation(Class<A> annotation) {
        return next.getAnnotation(annotation);
    }

    @Override
    public Annotation[] getAnnotations() {
        return next.getAnnotations();
    }
}
