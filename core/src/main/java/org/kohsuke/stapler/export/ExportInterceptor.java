package org.kohsuke.stapler.export;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

/**
 * Allows caller to intercept exporting of properties
 *
 * @author Vivek Pandey
 * @author James Dumay
 */
public abstract class ExportInterceptor {
    /**
     * Subclasses must call {@link Property#getValue(Object)}  to retrieve the property
     *
     * If the subclass decides the value can be included in the request return the value
     * otherwise, return null to exclude
     *
     * @param property to get the value for
     * @return the value of the property or null to exclude the property
     * @throws IOException if there was a problem with serialization that should prevent the serialization from proceeding
     */
    public abstract Object getValue(Property property, Object model) throws IOException;

    public static final ExportInterceptor DEFAULT = new ExportInterceptor() {
        @Override
        public Object getValue(Property property, Object model) throws IOException {
            try {
                return property.getValue(model);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw  new IOException("Failed to write " + property.name, e);
            }
        }
    };
}
