package org.kohsuke.stapler.lang;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author Kohsuke Kawaguchi
 * @since 1.220
 */
public abstract class MethodRef {
    public abstract <T extends Annotation> T getAnnotation(Class<T> type);

    public boolean hasAnnotation(Class<? extends Annotation> type) {
        return getAnnotation(type)!=null;
    }

    public abstract Object invoke(Object _this, Object... args) throws InvocationTargetException, IllegalAccessException;

    public static MethodRef wrap(final Method m) {
        m.setAccessible(true);

        return new MethodRef() {
            @Override
            public <T extends Annotation> T getAnnotation(Class<T> type) {
                return m.getAnnotation(type);
            }

            @Override
            public Object invoke(Object _this, Object... args) throws InvocationTargetException, IllegalAccessException {
                return m.invoke(_this,args);
            }
        };
    }
}
