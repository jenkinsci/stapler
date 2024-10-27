/*
 * Copyright (c) 2004-2010, Kohsuke Kawaguchi
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided
 * that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice, this list of
 *       conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright notice, this list of
 *       conditions and the following disclaimer in the documentation and/or other materials
 *       provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS
 * OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
 * AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER
 * IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF
 * THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.kohsuke.stapler;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

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

    private static final Map<Class, Object> defaultPrimitiveValue = new HashMap<>();

    static {
        defaultPrimitiveValue.put(boolean.class, false);
        defaultPrimitiveValue.put(int.class, 0);
        defaultPrimitiveValue.put(long.class, 0L);
    }

    /**
     * Merge  two sets of annotations. If both contains the same annotation, the definition
     * in 'b' overrides the definition in 'a' and shows up in the result
     */
    public static Annotation[] union(Annotation[] a, Annotation[] b) {
        // fast path
        if (a.length == 0) {
            return b;
        }
        if (b.length == 0) {
            return a;
        }

        // slow path
        List<Annotation> combined = new ArrayList<>(a.length + b.length);
        combined.addAll(Arrays.asList(a));

        OUTER:
        for (Annotation x : b) {
            for (int i = 0; i < a.length; i++) {
                if (x.annotationType() == combined.get(i).annotationType()) {
                    combined.set(i, x); // override
                    continue OUTER;
                }
            }
            // not overlapping. append
            combined.add(x);
        }

        return combined.toArray(new Annotation[0]);
    }

    /**
     * Checks whether the method defined on the base type with the given arguments is overridden in the given derived
     * type.
     *
     * @param base       The base type.
     * @param derived    The derived type.
     * @param methodName The name of the method.
     * @param types      The types of the arguments for the method.
     * @return {@code true} when {@code derived} provides the specified method other than as inherited from {@code base}.
     * @throws IllegalArgumentException When {@code derived} does not derive from {@code base}, or when {@code base}
     *                                  does not contain the specified method.
     */
    public static boolean isOverridden(
            @NonNull Class<?> base, @NonNull Class<?> derived, @NonNull String methodName, @NonNull Class<?>... types) {
        if (base == derived) {
            // If base and derived are the same type, the method is not overridden by definition
            return false;
        }
        // If derived is not a subclass or implementor of base, it can't override any method
        // Technically this should also be triggered when base == derived, because it can't override its own method, but
        // the unit tests explicitly test for that as working.
        if (!base.isAssignableFrom(derived)) {
            throw new IllegalArgumentException("The specified derived class (" + derived.getCanonicalName()
                    + ") does not derive from the specified base class (" + base.getCanonicalName() + ").");
        }
        final Method baseMethod = getMethod(base, null, methodName, types);
        if (baseMethod == null) {
            throw new IllegalArgumentException("The specified method is not declared by the specified base class ("
                    + base.getCanonicalName() + "), or it is private, static or final.");
        }
        final Method derivedMethod = getMethod(derived, base, methodName, types);
        // the lookup will either return null or the base method when no override has been found (depending on whether
        // the base is an interface)
        return derivedMethod != null && derivedMethod != baseMethod;
    }

    /**
     * Calls the given supplier if the method defined on the base type with the given arguments is overridden in the
     * given derived type.
     *
     * @param supplier   The supplier to call if the method is indeed overridden.
     * @param base       The base type.
     * @param derived    The derived type.
     * @param methodName The name of the method.
     * @param types      The types of the arguments for the method.
     * @return {@code true} when {@code derived} provides the specified method other than as inherited from {@code base}.
     * @throws IllegalArgumentException When {@code derived} does not derive from {@code base}, or when {@code base}
     *                                  does not contain the specified method.
     * @throws AbstractMethodError If the derived class doesn't override the given method.
     * @since 2.259
     */
    public static <T> T ifOverridden(
            Supplier<T> supplier,
            @NonNull Class<?> base,
            @NonNull Class<?> derived,
            @NonNull String methodName,
            @NonNull Class<?>... types) {
        if (isOverridden(base, derived, methodName, types)) {
            return supplier.get();
        } else {
            throw new AbstractMethodError("The class " + derived.getName() + " must override at least one of the "
                    + base.getSimpleName() + "." + methodName + " methods");
        }
    }

    private static Method getMethod(
            @NonNull Class<?> clazz, @Nullable Class<?> base, @NonNull String methodName, @NonNull Class<?>... types) {
        try {
            final Method res = clazz.getDeclaredMethod(methodName, types);
            final int mod = res.getModifiers();
            // private and static methods are never ok, and end the search
            if (Modifier.isPrivate(mod) || Modifier.isStatic(mod)) {
                return null;
            }
            // when looking for the base/declaring method, final is not ok
            if (base == null && Modifier.isFinal(mod)) {
                return null;
            }
            // when looking for the overriding method, abstract is not ok
            if (base != null && Modifier.isAbstract(mod)) {
                return null;
            }
            return res;
        } catch (NoSuchMethodException e) {
            // If the base is an interface, the implementation may come from a default implementation on a derived
            // interface. So look at interfaces too.
            if (base != null && Modifier.isInterface(base.getModifiers())) {
                for (Class<?> iface : clazz.getInterfaces()) {
                    if (base.equals(iface) || !base.isAssignableFrom(iface)) {
                        continue;
                    }
                    final Method defaultImpl = getMethod(iface, base, methodName, types);
                    if (defaultImpl != null) {
                        return defaultImpl;
                    }
                }
            }
            // Method not found in clazz, let's search in superclasses
            Class<?> superclass = clazz.getSuperclass();
            if (superclass != null) {
                // if the superclass doesn't derive from base anymore (or IS base), stop looking
                if (base != null && (base.equals(superclass) || !base.isAssignableFrom(superclass))) {
                    return null;
                }
                return getMethod(superclass, base, methodName, types);
            }
            return null;
        } catch (SecurityException e) {
            throw new AssertionError(e);
        }
    }
}
