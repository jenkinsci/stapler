package org.kohsuke.stapler.lang;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * @author Kohsuke Kawaguchi
 * @since 1.220
 */
public abstract class MethodRef extends AnnotatedRef {
    /**
     * Returns true if this method is a 'public' method that should be used for routing requests.
     */
    public boolean isRoutable() {
        return true;
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
            public boolean isRoutable() {
                if (m.isBridge())    return false;
                return (m.getModifiers() & Modifier.PUBLIC)!=0;
            }

            @Override
            public Object invoke(Object _this, Object... args) throws InvocationTargetException, IllegalAccessException {
                return m.invoke(_this,args);
            }
        };
    }
}
