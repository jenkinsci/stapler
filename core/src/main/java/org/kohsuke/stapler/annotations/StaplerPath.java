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
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import org.kohsuke.stapler.Function;
import org.kohsuke.stapler.lang.FieldRef;
import org.kohsuke.stapler.lang.MethodRef;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Marks methods and fields as being navigable by Stapler.
 *
 * @since TODO
 */
@Target({METHOD, FIELD})
@Retention(RUNTIME)
@Documented
@Repeatable(StaplerPaths.class)
public @interface StaplerPath {
    /**
     * Special constant used to signify that the annotated method is a catch-all dynamic match.
     */
    String DYNAMIC = "\u0000\ufefforg.kohsuke.stapler.annotations.StaplerPath#DYNAMIC\ufeff\u0000";
    /**
     * Special constant used to signify that the path segment should be inferred from the method name:
     * <ul>
     * <li>Method names starting with {@code get} will have the {@code get} removed and the next character turned to
     * lowercase</li>
     * <li>Method names starting with {@code do} will have the {@code do} removed and the next character turned to
     * lowercase</li>
     * <li>Method names starting with {@code js} will have the {@code js} removed and the next character turned to
     * lowercase</li>
     * <li>All other methods will be ignored (annotation processor should flag such methods as incorrectly annotated)
     * </li>
     * <li>Field names will be used verbatim</li>
     * </ul>
     */
    String INFER_FROM_NAME = "\u0000\ufefforg.kohsuke.stapler.annotations.StaplerPath#INFER\ufeff\u0000";
    /**
     * Special constant used to signify that the path segment should be treated as the "index" page
     * (which also matches an empty segment)
     */
    String INDEX = "";

    String value() default INFER_FROM_NAME;

    /**
     * Meta-annotation to flag an annotation as implying {@link StaplerPath#INFER_FROM_NAME} without explicitly
     * requiring the method / field to have a {@link StaplerPath} annotation
     */
    @Target(ANNOTATION_TYPE)
    @Retention(RUNTIME)
    @Documented
    @interface Implicit {
        /**
         * The prefix to remove from the method name in order to recover the inferred name.
         * @return the prefix to remove from the method name in order to recover the inferred name.
         */
        String methodPrefix() default Helper.DEFAULT_METHOD_PREFIX;
    }

    /**
     * Helper class that consolidates the rules for determining the names to infer from a
     */
    class Helper {
        private static final String DEFAULT_METHOD_PREFIX = "get";

        private Helper() {
            throw new IllegalAccessError("Utility class");
        }

        public static boolean isPath(Function method) {
            return isMethodPath(method.getAnnotations());
        }

        public static boolean isPath(Method method) {
            return Modifier.isPublic(method.getModifiers()) && isMethodPath(method.getAnnotations());
        }

        public static boolean isPath(MethodRef method) {
            return method.isRoutable() && isMethodPath(method.getAnnotations());
        }

        private static boolean isMethodPath(Annotation[] annotations) {
            for (Annotation a : annotations) {
                if (a instanceof StaplerPaths) {
                    return true;
                } else if (a instanceof StaplerPath) {
                    return true;
                }
                Implicit implicit = a.annotationType().getAnnotation(Implicit.class);
                if (implicit != null) {
                    return true;
                }
            }
            return false;
        }

        public static boolean isPath(Field field) {
            return Modifier.isPublic(field.getModifiers()) && isFieldPath(field.getAnnotations());
        }

        public static boolean isPath(FieldRef field) {
            return field.isRoutable() && isFieldPath(field.getAnnotations());
        }

        private static boolean isFieldPath(Annotation[] annotations) {
            for (Annotation a : annotations) {
                if (a instanceof StaplerPaths) {
                    StaplerPath[] paths = ((StaplerPaths) a).value();
                    if (paths.length == 0) {
                        return true;
                    }
                    for (StaplerPath p : paths) {
                        if (!DYNAMIC.equals(p.value())) {
                            return true;
                        }
                    }
                    return false;
                } else if (a instanceof StaplerPath) {
                    return !DYNAMIC.equals(((StaplerPath) a).value());
                }
                Implicit implicit = a.annotationType().getAnnotation(Implicit.class);
                if (implicit != null) {
                    return true;
                }
            }
            return false;
        }

        public static boolean isDynamic(Function method) {
            return isMethodDynamic(method.getAnnotations());
        }

        public static boolean isDynamic(Method method) {
            return Modifier.isPublic(method.getModifiers()) && isMethodDynamic(method.getAnnotations());
        }

        public static boolean isDynamic(MethodRef method) {
            return method.isRoutable() && isMethodDynamic(method.getAnnotations());
        }

        private static boolean isMethodDynamic(Annotation[] annotations) {
            for (Annotation a : annotations) {
                if (a instanceof StaplerPaths) {
                    for (StaplerPath p : ((StaplerPaths) a).value()) {
                        if (DYNAMIC.equals(p.value())) {
                            return true;
                        }
                    }
                } else if (a instanceof StaplerPath) {
                    StaplerPath p = (StaplerPath) a;
                    if (DYNAMIC.equals(p.value())) {
                        return true;
                    }
                }
            }
            return false;
        }

        public static Iterable<String> getPaths(Function method) {
            return getMethodPaths(method.getName(), method.getAnnotations());
        }

        public static Iterable<String> getPaths(Method method) {
            if (!Modifier.isPublic(method.getModifiers())) {
                return Collections.emptyList();
            }
            return getMethodPaths(method.getName(), method.getAnnotations());
        }

        public static Iterable<String> getPaths(MethodRef method) {
            if (!method.isRoutable()) {
                return Collections.emptyList();
            }
            return getMethodPaths(method.getName(), method.getAnnotations());
        }

        private static Iterable<String> getMethodPaths(String methodName, Annotation[] annotations) {
            Set<String> names = new TreeSet<>();
            Set<String> prefixes = null; // lazy init to avoid an allocation
            boolean hasPathAnnotation = false;
            boolean hasImplicitAnnotation = false;
            boolean hasInferredName = false;
            for (Annotation a: annotations) {
                if (a instanceof StaplerPaths) {
                    hasPathAnnotation = true;
                    StaplerPath[] paths = ((StaplerPaths) a).value();
                    if (paths.length == 0) {
                        hasInferredName = true;
                    } else {
                        for (StaplerPath p: paths) {
                            switch (p.value()) {
                                case DYNAMIC:
                                    continue;
                                case INFER_FROM_NAME:
                                    hasInferredName = true;
                                    break;
                                default:
                                    names.add(p.value());
                                    break;
                            }
                        }
                    }
                } else if (a instanceof StaplerPath) {
                    hasPathAnnotation = true;
                    StaplerPath p = (StaplerPath) a;
                    switch (p.value()) {
                        case DYNAMIC:
                            continue;
                        case INFER_FROM_NAME:
                            hasInferredName = true;
                            break;
                        default:
                            names.add(p.value());
                            break;
                    }
                } else {
                    Implicit implicit = a.annotationType().getAnnotation(Implicit.class);
                    if (implicit != null) {
                        hasImplicitAnnotation = true;
                        if (!implicit.methodPrefix().isEmpty()) {
                            if (prefixes == null) {
                                prefixes = new TreeSet<>();
                            }
                            prefixes.add(implicit.methodPrefix());
                        }
                    }
                }
            }
            // we infer the name if either we have a path annotation that specifies inference or we do not have a path
            // annotation but we have another implicit annotation
            if (hasPathAnnotation ? hasInferredName : hasImplicitAnnotation) {
                if (prefixes == null) {
                    if (methodName.startsWith(DEFAULT_METHOD_PREFIX)) {
                        names.add(removePrefix(DEFAULT_METHOD_PREFIX, methodName));
                    } else {
                        names.add(methodName);
                    }
                } else {
                    boolean matchedPrefix = false;
                    for (String prefix : prefixes) {
                        if (prefix.isEmpty()) continue;
                        if (methodName.startsWith(prefix)) {
                            matchedPrefix = true;
                            names.add(removePrefix(prefix, methodName));
                        }
                    }
                    if (!matchedPrefix) {
                        names.add(methodName);
                    }
                }
            }
            return names;
        }

        public static Iterable<String> getPaths(Field field) {
            if (!Modifier.isPublic(field.getModifiers())) {
                return Collections.emptyList();
            }
            return getFieldPaths(field.getName(), field.getAnnotations());
        }

        public static Iterable<String> getPaths(FieldRef field) {
            if (!field.isRoutable()) {
                return Collections.emptyList();
            }
            return getFieldPaths(field.getName(), field.getAnnotations());
        }

        private static Iterable<String> getFieldPaths(String fieldName, Annotation[] annotations) {
            Set<String> names = new TreeSet<>();
            boolean hasPathAnnotation = false;
            boolean hasImplicitAnnotation = false;
            for (Annotation a : annotations) {
                if (a instanceof StaplerPaths) {
                    hasPathAnnotation = true;
                    StaplerPath[] paths = ((StaplerPaths) a).value();
                    if (paths.length == 0) {
                        // infer name for an empty @StaplerPaths()
                        names.add(fieldName);
                    } else {
                        for (StaplerPath p : paths) {
                            switch (p.value()) {
                                case DYNAMIC:
                                    continue;
                                case INFER_FROM_NAME:
                                    names.add(fieldName);
                                    break;
                                default:
                                    names.add(p.value());
                                    break;
                            }
                        }
                    }
                } else if (a instanceof StaplerPath) {
                    hasPathAnnotation = true;
                    StaplerPath p = (StaplerPath) a;
                    switch (p.value()) {
                        case DYNAMIC:
                            continue;
                        case INFER_FROM_NAME:
                            names.add(fieldName);
                            break;
                        default:
                            names.add(p.value());
                            break;
                    }
                } else {
                    Implicit implicit = a.annotationType().getAnnotation(Implicit.class);
                    if (implicit != null) {
                        hasImplicitAnnotation = true;
                    }
                }
            }
            if (!hasPathAnnotation && hasImplicitAnnotation) {
                names.add(fieldName);
            }
            return names;
        }

        private static String removePrefix(String prefix, String methodName) {
            int prefixLength = prefix.length();
            String name = methodName;
            int nameLength = name.length();
            if (prefixLength == 0 || nameLength < prefixLength + 1) return name;
            StringBuilder result = new StringBuilder(nameLength);
            result.append(name.substring(prefixLength, prefixLength+1).toLowerCase(Locale.ENGLISH));
            if (nameLength > prefixLength + 1) {
                result.append(name.substring(prefixLength+1));
            }
            return result.toString();
        }
    }
}
