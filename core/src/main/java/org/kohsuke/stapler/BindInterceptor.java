package org.kohsuke.stapler;

import net.sf.json.JSONObject;

import java.lang.reflect.Type;

/**
 * Intercepts (and receives callbacks) about the JSON → object binding process.
 *
 * @author Kohsuke Kawaguchi
 * @see StaplerRequest#setBindInterceptor(BindInterceptor)
 * @see WebApp#bindInterceptors
 */
public class BindInterceptor {
    /**
     * Called for each object conversion, after the expected type is determined.
     *
     * @param targetType
     *      Type that the converted object must be assignable to.
     * @param targetTypeErasure
     *      Erasure of the {@code targetType} parameter.
     * @param jsonSource
     *      JSON object to be mapped to Java object.
     * @return
     *      {@link #DEFAULT} to indicate that the default conversion process should proceed.
     *      Any other values (including null) will override the process.
     */
    public Object onConvert(Type targetType, Class targetTypeErasure, Object jsonSource) {
        return DEFAULT;
    }

    /**
     * Called for each object conversion, after the actual subtype to instantiate is determined.
     *
     * @param actualType
     *      The type to instnatiate
     * @param json
     *      JSON object to be mapped to Java object.
     * @return
     *      {@link #DEFAULT} to indicate that the default conversion process should proceed.
     *      Any other values (including null) will override the process.
     */
    public Object instantiate(Class actualType, JSONObject json) {
        return DEFAULT;
    }

    /**
     * Indicates that the conversion should proceed as it normally does,
     * and that the listener isn't replacing the process.
     */
    public static final Object DEFAULT = new Object();

    /**
     * Default {@link BindInterceptor} that does nothing.
     */
    public static final BindInterceptor NOOP = new BindInterceptor();
}
