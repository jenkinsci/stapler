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
import io.jenkins.servlet.ServletExceptionWrapper;
import jakarta.servlet.ServletException;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * Object that represents the HTTP response, which is defined as a capability to produce the response.
 *
 * <p>
 * {@code doXyz(...)} method could return an object of this type or throw an exception of this type, and if it does so,
 * the object is asked to produce HTTP response.
 *
 * <p>
 * This is useful to make {@code doXyz} look like a real function, and decouple it further from HTTP.
 *
 * @author Kohsuke Kawaguchi
 */
public interface HttpResponse {
    /**
     * @param node
     *      The object whose "doXyz" method created this object.
     */
    default void generateResponse(StaplerRequest2 req, StaplerResponse2 rsp, Object node)
            throws IOException, ServletException {
        if (isOverridden(
                HttpResponse.class,
                getClass(),
                "generateResponse",
                StaplerRequest.class,
                StaplerResponse.class,
                Object.class)) {
            try {
                generateResponse(
                        req != null ? StaplerRequest.fromStaplerRequest2(req) : null,
                        rsp != null ? StaplerResponse.fromStaplerResponse2(rsp) : null,
                        node);
            } catch (javax.servlet.ServletException e) {
                throw ServletExceptionWrapper.toJakartaServletException(e);
            }
        } else {
            throw new AbstractMethodError("The class " + getClass().getName() + " must override at least one of the "
                    + HttpResponse.class.getSimpleName() + ".generateResponse methods");
        }
    }

    /**
     * @deprecated use {@link #generateResponse(StaplerRequest2, StaplerResponse2, Object)}
     */
    @Deprecated
    default void generateResponse(StaplerRequest req, StaplerResponse rsp, Object node)
            throws IOException, javax.servlet.ServletException {
        if (isOverridden(
                HttpResponse.class,
                getClass(),
                "generateResponse",
                StaplerRequest2.class,
                StaplerResponse2.class,
                Object.class)) {
            try {
                generateResponse(
                        req != null ? StaplerRequest.toStaplerRequest2(req) : null,
                        rsp != null ? StaplerResponse.toStaplerResponse2(rsp) : null,
                        node);
            } catch (ServletException e) {
                throw ServletExceptionWrapper.fromJakartaServletException(e);
            }
        } else {
            throw new AbstractMethodError("The class " + getClass().getName() + " must override at least one of the "
                    + HttpResponse.class.getSimpleName() + ".generateResponse methods");
        }
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
    private static boolean isOverridden(
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
