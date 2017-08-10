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
import java.lang.annotation.Inherited;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import org.kohsuke.stapler.Facet;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotation to document that the class expects there to be a {@link Facet} with the specified name available.
 * If the annotated type is either an {@code interface} or an {@code abstract class} then the {@link Facet} is
 * not required available to the annotated class but must be present on any concrete implementation subclasses.
 *
 * @since TODO
 */
@Target(TYPE)
@Retention(RUNTIME)
@Documented
@Repeatable(StaplerFacets.class)
@Inherited
public @interface StaplerFacet {
    /**
     * Special constant used to signify that the annotated class has a catch-all fallback dispatch facet. For example
     * a JRuby facet can have a rack dispatcher that could handle any facet name.
     */
    String FALLBACK = "\u0000\ufefforg.kohsuke.stapler.annotations.StaplerFacet#FALLBACK\ufeff\u0000";

    /**
     * The name of the {@link Facet} excluding the extension, for example {@code index} not {@code index.jelly} or
     * {@code index.groovy}.
     *
     * @return the name of the {@link Facet}.
     */
    String value();

    /**
     * Helper class.
     */
    class Helper {

        private Helper() {
            throw new IllegalAccessError("Utility class");
        }

        /**
         * Collects the {@link StaplerFacet} annotations of a class. Walks the class hierarchy and all implemented
         * interfaces to collect the facets.
         *
         * @param clazz the class.
         * @return the annotations.
         */
        public static List<StaplerFacet> facetsOf(Class<?> clazz) {
            Map<String, StaplerFacet> result = new LinkedHashMap<>();
            Stack<Class<?>> interfaces = new Stack<>();
            interfaces.addAll(Arrays.asList(clazz.getInterfaces()));
            for (Class<?> c = clazz; c != null && c != Object.class; c = c.getSuperclass()) {
                interfaces.addAll(Arrays.asList(c.getInterfaces()));
                for (Annotation a : c.getDeclaredAnnotations()) {
                    if (a instanceof StaplerFacet) {
                        StaplerFacet f = (StaplerFacet) a;
                        if (!result.containsKey(f.value()) && !FALLBACK.equals(f.value())) {
                            result.put(f.value(), f);
                        }
                    } else if (a instanceof StaplerFacets) {
                        for (StaplerFacet f : ((StaplerFacets) a).value()) {
                            if (!result.containsKey(f.value()) && !FALLBACK.equals(f.value())) {
                                result.put(f.value(), f);
                            }
                        }
                    }
                }
            }
            if (!interfaces.isEmpty()) {
                Set<Class<?>> checked = new HashSet<>();
                while (!interfaces.isEmpty()) {
                    Class<?> c = interfaces.pop();
                    if (checked.contains(c)) {
                        continue;
                    }
                    checked.add(c);
                    interfaces.addAll(Arrays.asList(c.getInterfaces()));
                    for (Annotation a : c.getDeclaredAnnotations()) {
                        if (a instanceof StaplerFacet) {
                            StaplerFacet f = (StaplerFacet) a;
                            if (!result.containsKey(f.value()) && !FALLBACK.equals(f.value())) {
                                result.put(f.value(), f);
                            }
                        } else if (a instanceof StaplerFacets) {
                            for (StaplerFacet f : ((StaplerFacets) a).value()) {
                                if (!result.containsKey(f.value()) && !FALLBACK.equals(f.value())) {
                                    result.put(f.value(), f);
                                }
                            }
                        }
                    }
                }
            }
            return new ArrayList<>(result.values());
        }

        /**
         * Checks if any of the {@link StaplerFacet} annotations of a class allow for a dynamic dispatch facet.
         *
         * @param clazz the class.
         * @return the annotations.
         */
        public static boolean hasFallback(Class<?> clazz) {
            Stack<Class<?>> interfaces = new Stack<>();
            interfaces.addAll(Arrays.asList(clazz.getInterfaces()));
            for (Class<?> c = clazz; c != null && c != Object.class; c = c.getSuperclass()) {
                interfaces.addAll(Arrays.asList(c.getInterfaces()));
                for (Annotation a : c.getDeclaredAnnotations()) {
                    if (a instanceof StaplerFacet) {
                        StaplerFacet f = (StaplerFacet) a;
                        if (FALLBACK.equals(f.value())) {
                            return true;
                        }
                    } else if (a instanceof StaplerFacets) {
                        for (StaplerFacet f : ((StaplerFacets) a).value()) {
                            if (FALLBACK.equals(f.value())) {
                                return true;
                            }
                        }
                    }
                }
            }
            if (!interfaces.isEmpty()) {
                Set<Class<?>> checked = new HashSet<>();
                while (!interfaces.isEmpty()) {
                    Class<?> c = interfaces.pop();
                    if (checked.contains(c)) {
                        continue;
                    }
                    checked.add(c);
                    interfaces.addAll(Arrays.asList(c.getInterfaces()));
                    for (Annotation a : c.getDeclaredAnnotations()) {
                        if (a instanceof StaplerFacet) {
                            StaplerFacet f = (StaplerFacet) a;
                            if (FALLBACK.equals(f.value())) {
                                return true;
                            }
                        } else if (a instanceof StaplerFacets) {
                            for (StaplerFacet f : ((StaplerFacets) a).value()) {
                                if (FALLBACK.equals(f.value())) {
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
            return false;
        }

    }
}
