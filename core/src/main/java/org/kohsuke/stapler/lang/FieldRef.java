package org.kohsuke.stapler.lang;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

/**
 * Fields of {@link Klass}.
 *
 * @see Klass#getDeclaredFields()
 * @author Kohsuke Kawaguchi
 */
public abstract class FieldRef extends AnnotatedRef {
    /**
     * Name of the method.
     *
     * @see Field#getName()
     */
    public abstract String getName();

    /**
     * Obtains the value of the field of the instance.
     */
    public abstract Object get(Object instance) throws IllegalAccessException;

    /**
     * Gets a fully qualified name of this field that includes the declaring type.
     */
    public abstract String getQualifiedName();

    public static FieldRef wrap(final Field f) {
        f.setAccessible(true);

        return new FieldRef() {
            @Override
            public <T extends Annotation> T getAnnotation(Class<T> type) {
                return f.getAnnotation(type);
            }

            @Override
            public String getName() {
                return f.getName();
            }

            @Override
            public Object get(Object instance) throws IllegalAccessException {
                return f.get(instance);
            }

            @Override
            public String getQualifiedName() {
                return f.getDeclaringClass().getName()+"."+getName();
            }
        };
    }
}
