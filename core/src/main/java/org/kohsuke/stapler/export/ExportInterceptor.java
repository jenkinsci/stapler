package org.kohsuke.stapler.export;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

/**
 * Allows caller to intercept exporting of properties.
 *
 * Implementation can choose to ignore properties in case of failure during serialization.
 *
 * @author Vivek Pandey
 * @author James Dumay
 */
public abstract class ExportInterceptor {
    /**
     * Subclasses must call {@link Property#getValue(Object)}  to retrieve the property.
     *
     * If the subclass decides the value can be included in the request return the value
     * otherwise, return null results in to the property being written with value null.
     *
     * @param property to get the value from model object
     * @param model object with this property
     * @return the value of the property
     * @throws IOException if there was a problem with serialization that should prevent
     *         the serialization from proceeding
     * @see Exported#skipNull()
     */
    public abstract Object getValue(Property property, Object model, ExportConfig config) throws IOException;

    public static final ExportInterceptor DEFAULT = new ExportInterceptor() {
        @Override
        public Object getValue(Property property, Object model, ExportConfig config) throws IOException {
            try {
                return property.getValue(model);
            } catch (IllegalAccessException | InvocationTargetException e) {
                if(config.isSkipIfFail()) {
                    return null;
                }
                throw new IOException("Failed to write " + property.name + ":" + e.getMessage(), e);
            }
        }
    };
}
