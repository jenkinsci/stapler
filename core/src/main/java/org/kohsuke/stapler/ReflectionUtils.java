package org.kohsuke.stapler;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Kohsuke Kawaguchi
 */
public class ReflectionUtils {
    /**
     * Given the primitive type, returns the VM default value for that type in a boxed form.
     * For reference types, return null.
     */
    public static Object getVmDefaultValueFor(Class<?> type) {
        return defaultPrimitiveValue.get(type);
    }

    private static final Map<Class,Object> defaultPrimitiveValue = new HashMap<Class, Object>();
    static {
        defaultPrimitiveValue.put(boolean.class,false);
        defaultPrimitiveValue.put(int.class,0);
        defaultPrimitiveValue.put(long.class,0L);
    }
}
