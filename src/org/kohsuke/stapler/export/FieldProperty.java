package org.kohsuke.stapler.export;

import java.lang.reflect.Field;
import java.lang.reflect.Type;

/**
 * {@link Property} based on {@link Field}.
 * @author Kohsuke Kawaguchi
 */
class FieldProperty extends Property {
    private final Field field;

    public FieldProperty(Model owner, Field field, Exported exported) {
        super(owner,field.getName(), exported);
        this.field = field;
    }

    public Type getGenericType() {
        return field.getGenericType();
    }

    public Class getType() {
        return field.getType();
    }

    protected Object getValue(Object object) throws IllegalAccessException {
        return field.get(object);
    }
}
