package org.kohsuke.stapler.export;

import java.lang.reflect.Field;

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

    protected Object getValue(Object object) throws IllegalAccessException {
        return field.get(object);
    }
}
