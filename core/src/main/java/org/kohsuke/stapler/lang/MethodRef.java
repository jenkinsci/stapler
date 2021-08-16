package org.kohsuke.stapler.lang;

import org.kohsuke.stapler.util.IllegalReflectiveAccessLogHandler;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.logging.Logger;
import edu.umd.cs.findbugs.annotations.CheckForNull;

/**
 * @author Kohsuke Kawaguchi
 * @since 1.220
 */
public abstract class MethodRef extends AnnotatedRef {

    private static final Logger LOGGER = Logger.getLogger(MethodRef.class.getName());

    /**
     * Returns true if this method is a 'public' method that should be used for routing requests.
     */
    public boolean isRoutable() {
        return true;
    }
    
    /**
     * Retrieves the referenced method name.
     * Some implementations (e.g. Ruby) cannot guarantee availability of names for all cases,
     * so sometimes the name may be missing.
     * @return Method name. {@code null} if it cannot be determined.
     * @since 1.248
     */
    @CheckForNull
    public String getName() {
        return null;
    }

    public abstract Object invoke(Object _this, Object... args) throws InvocationTargetException, IllegalAccessException;

    public static MethodRef wrap(final Method m) {
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
            public String getName() {
                return m.getName();
            }
            
            @Override
            public Object invoke(Object _this, Object... args) throws InvocationTargetException, IllegalAccessException {
                try {
                    return m.invoke(_this, args);
                } catch (IllegalAccessException e) {
                    LOGGER.warning(IllegalReflectiveAccessLogHandler.get(e));
                    m.setAccessible(true);
                    return m.invoke(_this, args);
                }
            }
        };
    }
}
