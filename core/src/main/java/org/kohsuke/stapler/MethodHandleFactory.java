package org.kohsuke.stapler;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Method;

/**
 * Implementation detail in Stapler. Do not use from outside.
 */
public final class MethodHandleFactory {
    private static final Lookup LOOKUP = MethodHandles.lookup();

    public static MethodHandle get(Method method) {
        try {
            /*
                Stapler generally deals with
             */
            method.setAccessible(true);

            return LOOKUP.unreflect(method);
        } catch (IllegalAccessException e) {
            throw (Error)new IllegalAccessError("Protected method: "+method).initCause(e);
        }
    }


    private MethodHandleFactory() {}
}
