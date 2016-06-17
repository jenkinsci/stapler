package org.kohsuke.stapler.lang.util;

import org.kohsuke.stapler.lang.MethodRef;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

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
    public Object invoke(Object _this, Object... args) throws InvocationTargetException, IllegalAccessException {
        return getBase().invoke(_this, args);
    }

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
