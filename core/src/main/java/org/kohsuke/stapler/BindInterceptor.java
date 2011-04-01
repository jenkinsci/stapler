package org.kohsuke.stapler;

import java.lang.reflect.Type;

/**
 * Intercepts (and receives callbacks) about the JSON->object binding process.
 *
 * @author Kohsuke Kawaguchi
 */
public class BindInterceptor {
    /**
     * Called for each object conversion.
     *
     * @return
     *      {@link #DEFAULT} to indicate that the default conversion process should proceed.
     *      Any other values (including null) will override the process.
     */
    public Object onConvert(Type targetType, Class targetTypeErasure, Object jsonSource) {
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
