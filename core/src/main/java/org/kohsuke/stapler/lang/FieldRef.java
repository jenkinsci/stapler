package org.kohsuke.stapler.lang;

import org.apache.commons.lang.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Fields of {@link Klass}.
 *
 * @see Klass#getDeclaredFields()
 * @author Kohsuke Kawaguchi
 */
public abstract class FieldRef extends AnnotatedRef {

    private static final Logger LOGGER = Logger.getLogger(FieldRef.class.getName());

    public interface Filter {
        boolean keep(FieldRef m);

        Filter ALWAYS_OK = new Filter() {
            @Override
            public boolean keep(FieldRef m) {
                return true;
            }
        };
    }

    /**
     * Name of the method.
     *
     * @see Field#getName()
     */
    public abstract String getName();

    /**
     * Whether the field is static.
     * @return
     */
    public abstract boolean isStatic();

    /**
     * Obtains the value of the field of the instance.
     */
    public abstract Object get(Object instance) throws IllegalAccessException;

    /**
     * Gets a fully qualified name of this field that includes the declaring type.
     */
    public abstract String getQualifiedName();

    /**
     * Gets the signature for this for use in lists
     *
     * @see org.kohsuke.stapler.Function#getSignature()
     *
     */
    public abstract String getSignature();

    // TODO Should this be Klass?
    public abstract Class<?> getReturnType();

    /**
     * Returns true if this method is a 'public' method that should be used for routing requests.
     */
    public boolean isRoutable() {
        return true;
    }

    public static FieldRef wrap(final Field f) {
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
                try {
                    return f.get(instance);
                } catch (IllegalAccessException e) {
                    LOGGER.warning(e.getMessage() + ", please report to the respective component");
                    f.setAccessible(true);
                    return f.get(instance);
                }
            }

            @Override
            public boolean isStatic() {
                return Modifier.isStatic(f.getModifiers());
            }

            @Override
            public Class<?> getReturnType() {
                return f.getType();
            }

            @Override
            public String getSignature() {
                String prefix = isStatic() ? "staticField" : "field";
                return StringUtils.join(Arrays.asList(prefix, f.getDeclaringClass().getName(), getName()), ' ');
            }

            @Override
            public String getQualifiedName() {
                return f.getDeclaringClass().getName()+"."+getName();
            }

            @Override
            public boolean isRoutable() {
                return Modifier.isPublic(f.getModifiers());
            }
        };
    }
}
