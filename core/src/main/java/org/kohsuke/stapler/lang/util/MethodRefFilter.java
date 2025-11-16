package org.kohsuke.stapler.lang.util;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.kohsuke.stapler.lang.MethodRef;

/**
 * {@link MethodRef} filter as a convenience class.
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class MethodRefFilter extends MethodRef {
    protected abstract MethodRef getBase();

    @Override
    public boolean isRoutable() {
        return getBase().isRoutable();
    }

    @Override
    public String getName() {
        return getBase().getName();
    }

    @Override
    public Object invoke(Object _this, Object... args) throws InvocationTargetException, IllegalAccessException {
        return getBase().invoke(_this, args);
    }

    @SuppressFBWarnings(value = "HSM_HIDING_METHOD", justification = "TODO needs triage")
    public static MethodRef wrap(Method m) {
        return MethodRef.wrap(m);
    }

    @Override
    public <T extends Annotation> T getAnnotation(Class<T> type) {
        return getBase().getAnnotation(type);
    }

    @Override
    public boolean hasAnnotation(Class<? extends Annotation> type) {
        return getBase().hasAnnotation(type);
    }
}
