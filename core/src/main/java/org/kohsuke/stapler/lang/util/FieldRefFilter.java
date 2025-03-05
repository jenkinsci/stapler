package org.kohsuke.stapler.lang.util;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import org.kohsuke.stapler.lang.FieldRef;

/**
 * {@link FieldRef} filter as a convenience class.
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class FieldRefFilter extends FieldRef {
    protected abstract FieldRef getBase();

    @Override
    public String getName() {
        return getBase().getName();
    }

    @Override
    public Object get(Object instance) throws IllegalAccessException {
        return getBase().get(instance);
    }

    @Override
    public boolean isStatic() {
        return getBase().isStatic();
    }

    @Override
    public String getQualifiedName() {
        return getBase().getQualifiedName();
    }

    @Override
    public boolean isRoutable() {
        return getBase().isRoutable();
    }

    @SuppressFBWarnings(value = "HSM_HIDING_METHOD", justification = "TODO needs triage")
    public static FieldRef wrap(Field f) {
        return FieldRef.wrap(f);
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
