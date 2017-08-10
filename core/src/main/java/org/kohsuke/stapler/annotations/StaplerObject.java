/*
 * Copyright (c) 2017, Stephen Connolly, CloudBees, Inc.
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

package org.kohsuke.stapler.annotations;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Stack;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Marker annotation that declares the annotated type is complete from the point of view of
 * {@link org.kohsuke.stapler.annotations}{@code .Stapler*} annotations. A type that is complete can be subjected to
 * additional verifications.
 * <h2>Inheritance</h2>
 * When a class is annotated with {@link StaplerObject} it means that everything defined in that class is definitive.
 * Methods defined in a superclass that is not {@link StaplerObject} annotated will only require annotations if they
 * are overridden in the {@link StaplerObject} annotated class.
 * Methods defined in a subclass will only require annotations if that subclass is also {@link StaplerObject} annotated.
 *
 * @since TODO
 */
@Target(TYPE)
@Retention(RUNTIME)
@Documented
public @interface StaplerObject {

    /**
     * Helper class that consolidates the rules for determining if a class is a {@link StaplerObject}
     */
    class Helper {

        private Helper() {
            throw new IllegalAccessError("Utility class");
        }

        /**
         * Returns {@code true} if and only if the supplied object is an instance of a class that has a declared
         * {@link StaplerObject} annotation.
         *
         * @param object the instance.
         * @return {@code true} if and only if the supplied object is an instance of a class that has a declared
         * {@link StaplerObject} annotation.
         */
        public static boolean isObject(Object object) {
            return object != null && isObject(object.getClass());
        }

        /**
         * Returns {@code true} if and only if the supplied object is an instance of a class that either has a declared
         * {@link StaplerObject} annotation or has the annotation somewhere in it's parent class/interface hierarchy.
         *
         * @param object the instance.
         * @return {@code true} if and only if the supplied object is an instance of a class that either has a declared
         * {@link StaplerObject} annotation or has the annotation somewhere in it's parent class/interface hierarchy.
         */
        public static boolean hasObject(Object object) {
            return object != null && hasObject(object.getClass());
        }

        /**
         * Returns {@code true} if and only if the supplied class has a declared {@link StaplerObject} annotation.
         *
         * @param clazz the class.
         * @return {@code true} if and only if the supplied class has a declared {@link StaplerObject} annotation.
         */
        public static boolean isObject(Class clazz) {
            if (clazz == null) {
                return false;
            }
            // TODO Java 8 switch to getDeclaredAnnotation(StaplerObject.class) != null
            for (Annotation a : clazz.getDeclaredAnnotations()) {
                if (StaplerObject.class.equals(a.annotationType())) {
                    return true;
                }
            }
            return false;
        }

        /**
         * Returns {@code true} if and only if the supplied class that either has a declared {@link StaplerObject}
         * annotation or has the annotation somewhere in it's parent class/interface hierarchy.
         *
         * @param clazz the class.
         * @return {@code true} if and only if the supplied class that either has a declared {@link StaplerObject}
         * annotation or has the annotation somewhere in it's parent class/interface hierarchy.
         */
        public static boolean hasObject(Class clazz) {
            if (clazz == null) {
                return false;
            }
            if (isObject(clazz)) {
                return true;
            }
            for (Class c = clazz.getSuperclass(); c != null && c != Object.class; c = c.getSuperclass()) {
                if (isObject(c)) {
                    return true;
                }
            }
            Class[] interfaces = clazz.getInterfaces();
            if (interfaces.length > 0) {
                Set<Class> checked = new HashSet<>();
                Stack<Class> toCheck = new Stack<>();
                toCheck.addAll(Arrays.asList(interfaces));
                while (!toCheck.isEmpty()) {
                    Class c = toCheck.pop();
                    if (checked.add(c)) {
                        if (isObject(c)) {
                            return true;
                        } else {
                            for (Class t: c.getInterfaces()) {
                                if (!checked.contains(t)) {
                                    toCheck.push(t);
                                }
                            }
                        }
                    }
                }
            }
            return false;
        }

        /**
         * Finds the (possibly abstract) super method declarations. Only methods with the same or higher visibility
         * will be returned.
         *
         * @param method the method.
         * @return the (possibly abstract) super method declarations.
         */
        public static Iterable<Method> declaredSuperMethods(final Method method) {
            if (Modifier.isPrivate(method.getModifiers())) {
                // cannot override private methods
                return Collections.emptySet();
            }
            return new Iterable<Method>() {
                @Override
                public Iterator<Method> iterator() {
                    return new Iterator<Method>() {
                        Stack<Class> toCheck;
                        Set<Class> checked;
                        Class<?> current;
                        Method next;

                        @Override
                        public void remove() {
                            throw new UnsupportedOperationException("remove");
                        }

                        @Override
                        public boolean hasNext() {
                            if (next != null) {
                                return true;
                            }
                            if (toCheck == null) {
                                // initialize
                                toCheck = new Stack<>();
                                checked = new HashSet<>();
                                // check the class hierarchy first, then interfaces
                                current = method.getDeclaringClass().getSuperclass();
                                toCheck.addAll(Arrays.asList(method.getDeclaringClass().getInterfaces()));
                            }
                            while (true) {
                                if (current == null) {
                                    if (toCheck.isEmpty()) {
                                        return false;
                                    }
                                    current = toCheck.pop();
                                    if (current != null) {
                                        toCheck.addAll(Arrays.asList(current.getInterfaces()));
                                    }
                                }
                                if (checked.add(current)) {
                                    try {
                                        next = current.getDeclaredMethod(method.getName(), method.getParameterTypes());
                                        if (Modifier.isPrivate(next.getModifiers())) {
                                            // this is a private mirror, not the super method
                                            next = null;
                                        } else if (Modifier.isPublic(next.getModifiers())) {
                                            // this is a public parent, hence must be the parent
                                            return true;
                                        } else if (Modifier.isProtected(next.getModifiers())
                                                && Modifier.isProtected(method.getModifiers())) {
                                            // this is a protected parent, the child must be at least protected, but
                                            // if the child is public then we are not the "parent" declaration
                                            return true;
                                        } else if (!Modifier.isProtected(method.getModifiers())
                                                && !Modifier.isProtected(method.getModifiers())) {
                                            // this is a package scoped parent method, the child must also be
                                            // packaged scoped for this to be the "parent" declaration.
                                            return true;
                                        } else {
                                            // keep searching
                                            next = null;
                                        }
                                    } catch (NoSuchMethodException e) {
                                        // ignore
                                    }
                                }
                                current = current.getSuperclass();
                                if (current != null) {
                                    toCheck.addAll(Arrays.asList(current.getInterfaces()));
                                }
                            }
                        }

                        @Override
                        public Method next() {
                            if (hasNext()) {
                                try {
                                    return next;
                                } finally {
                                    next = null;
                                }
                            }
                            throw new NoSuchElementException();
                        }
                    };
                }
            };
        }
    }
}
