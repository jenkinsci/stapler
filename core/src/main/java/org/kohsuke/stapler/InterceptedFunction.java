package org.kohsuke.stapler;

import org.kohsuke.stapler.interceptor.Interceptor;
import org.kohsuke.stapler.interceptor.InterceptorAnnotation;

import java.lang.reflect.InvocationTargetException;

/**
 * Function that's wrapped by {@link Interceptor}.
 */
final class InterceptedFunction extends ForwardingFunction {
    private final Interceptor interceptor;

    public InterceptedFunction(Function next, InterceptorAnnotation ia) {
        super(next);
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

    public Object invoke(StaplerRequest req, StaplerResponse rsp, Object o, Object... args) throws IllegalAccessException, InvocationTargetException {
        return interceptor.invoke(req, rsp, o, args);
    }
}
