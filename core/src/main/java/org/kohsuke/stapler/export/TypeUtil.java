/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package org.kohsuke.stapler.export;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.MalformedParameterizedTypeException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.Objects;

/**
 * Type arithmetic code. Taken from the JAXB RI.
 *
 * @author Kohsuke Kawaguchi
 */
public class TypeUtil {
    abstract static class TypeVisitor<T, P> {
        public final T visit(Type t, P param) {
            assert t != null;

            if (t instanceof Class) {
                return onClass((Class) t, param);
            }
            if (t instanceof ParameterizedType) {
                return onParameterizedType((ParameterizedType) t, param);
            }
            if (t instanceof GenericArrayType) {
                return onGenericArray((GenericArrayType) t, param);
            }
            if (t instanceof WildcardType) {
                return onWildcard((WildcardType) t, param);
            }
            if (t instanceof TypeVariable) {
                return onVariable((TypeVariable) t, param);
            }

            // covered all the cases
            assert false;
            throw new IllegalArgumentException();
        }

        protected abstract T onClass(Class c, P param);

        protected abstract T onParameterizedType(ParameterizedType p, P param);

        protected abstract T onGenericArray(GenericArrayType g, P param);

        protected abstract T onVariable(TypeVariable v, P param);

        protected abstract T onWildcard(WildcardType w, P param);
    }

    /**
     * Implements the logic for {@link #erasure(Type)}.
     */
    private static final TypeVisitor<Class, Void> eraser = new TypeVisitor<>() {
        @Override
        public Class onClass(Class c, Void unused) {
            return c;
        }

        @Override
        public Class onParameterizedType(ParameterizedType p, Void unused) {
            // TODO: why getRawType returns Type? not Class?
            return visit(p.getRawType(), null);
        }

        @Override
        public Class onGenericArray(GenericArrayType g, Void unused) {
            return Array.newInstance(visit(g.getGenericComponentType(), null), 0)
                    .getClass();
        }

        @Override
        public Class onVariable(TypeVariable v, Void unused) {
            return visit(v.getBounds()[0], null);
        }

        @Override
        public Class onWildcard(WildcardType w, Void unused) {
            return visit(w.getUpperBounds()[0], null);
        }
    };

    /**
     * Returns the runtime representation of the given type.
     *
     * This corresponds to the notion of the erasure in JSR-14.
     */
    public static <T> Class<T> erasure(Type t) {
        return eraser.visit(t, null);
    }

    private static final TypeVisitor<Type, Class> baseClassFinder = new TypeVisitor<>() {
        @Override
        public Type onClass(Class c, Class sup) {
            // t is a raw type
            if (sup == c) {
                return sup;
            }

            Type r;

            Type sc = c.getGenericSuperclass();
            if (sc != null) {
                r = visit(sc, sup);
                if (r != null) {
                    return r;
                }
            }

            for (Type i : c.getGenericInterfaces()) {
                r = visit(i, sup);
                if (r != null) {
                    return r;
                }
            }

            return null;
        }

        @Override
        public Type onParameterizedType(ParameterizedType p, Class sup) {
            Class raw = (Class) p.getRawType();
            if (raw == sup) {
                // p is of the form sup<...>
                return p;
            } else {
                // recursively visit super class/interfaces
                Type r = raw.getGenericSuperclass();
                if (r != null) {
                    r = visit(bind(r, raw, p), sup);
                }
                if (r != null) {
                    return r;
                }
                for (Type i : raw.getGenericInterfaces()) {
                    r = visit(bind(i, raw, p), sup);
                    if (r != null) {
                        return r;
                    }
                }
                return null;
            }
        }

        @Override
        public Type onGenericArray(GenericArrayType g, Class sup) {
            // not clear what I should do here
            return null;
        }

        @Override
        public Type onVariable(TypeVariable v, Class sup) {
            return visit(v.getBounds()[0], sup);
        }

        @Override
        public Type onWildcard(WildcardType w, Class sup) {
            // not clear what I should do here
            return null;
        }

        /**
         * Replaces the type variables in {@code t} by its actual arguments.
         *
         * @param decl
         *      provides a list of type variables. See {@link GenericDeclaration#getTypeParameters()}
         * @param args
         *      actual arguments. See {@link ParameterizedType#getActualTypeArguments()}
         */
        private Type bind(Type t, GenericDeclaration decl, ParameterizedType args) {
            return binder.visit(t, new BinderArg(decl, args.getActualTypeArguments()));
        }
    };

    private static final TypeVisitor<Type, BinderArg> binder = new TypeVisitor<>() {
        @Override
        public Type onClass(Class c, BinderArg args) {
            return c;
        }

        @Override
        public Type onParameterizedType(ParameterizedType p, BinderArg args) {
            Type[] params = p.getActualTypeArguments();

            boolean different = false;
            for (int i = 0; i < params.length; i++) {
                Type t = params[i];
                params[i] = visit(t, args);
                different |= t != params[i];
            }

            Type newOwner = p.getOwnerType();
            if (newOwner != null) {
                newOwner = visit(newOwner, args);
            }
            different |= p.getOwnerType() != newOwner;

            if (!different) {
                return p;
            }

            return new ParameterizedTypeImpl((Class<?>) p.getRawType(), params, newOwner);
        }

        @Override
        public Type onGenericArray(GenericArrayType g, BinderArg types) {
            Type c = visit(g.getGenericComponentType(), types);
            if (c == g.getGenericComponentType()) {
                return g;
            }

            return new GenericArrayTypeImpl(c);
        }

        @Override
        public Type onVariable(TypeVariable v, BinderArg types) {
            return types.replace(v);
        }

        @Override
        public Type onWildcard(WildcardType w, BinderArg types) {
            // TODO: this is probably still incorrect
            // bind( "? extends T" ) with T= "? extends Foo" should be "? extends Foo",
            // not "? extends (? extends Foo)"
            Type[] lb = w.getLowerBounds();
            Type[] ub = w.getUpperBounds();
            boolean diff = false;

            for (int i = 0; i < lb.length; i++) {
                Type t = lb[i];
                lb[i] = visit(t, types);
                diff |= (t != lb[i]);
            }

            for (int i = 0; i < ub.length; i++) {
                Type t = ub[i];
                ub[i] = visit(t, types);
                diff |= (t != ub[i]);
            }

            if (!diff) {
                return w;
            }

            return new WildcardTypeImpl(lb, ub);
        }
    };

    private static class BinderArg {
        final TypeVariable[] params;
        final Type[] args;

        BinderArg(TypeVariable[] params, Type[] args) {
            this.params = params;
            this.args = args;
            assert params.length == args.length;
        }

        BinderArg(GenericDeclaration decl, Type[] args) {
            this(decl.getTypeParameters(), args);
        }

        Type replace(TypeVariable v) {
            for (int i = 0; i < params.length; i++) {
                if (params[i] == v) {
                    return args[i];
                }
            }
            return v; // this is a free variable
        }
    }

    /**
     * Gets the parameterization of the given base type.
     *
     * <p>
     * For example, given the following
     * <pre>{@code
     * interface Foo<T> extends List<List<T>> {}
     * interface Bar extends Foo<String> {}
     * }</pre>
     * This method works like this:
     * <pre>{@code
     * getBaseClass( Bar, List ) = List<List<String>
     * getBaseClass( Bar, Foo  ) = Foo<String>
     * getBaseClass( Foo<? extends Number>, Collection ) = Collection<List<? extends Number>>
     * getBaseClass( ArrayList<? extends BigInteger>, List ) = List<? extends BigInteger>
     * }</pre>
     *
     * @param type
     *      The type that derives from {@code baseType}
     * @param baseType
     *      The class whose parameterization we are interested in.
     * @return
     *      The use of {@code baseType} in {@code type}.
     *      or null if the type is not assignable to the base type.
     */
    public static Type getBaseClass(Type type, Class baseType) {
        return baseClassFinder.visit(type, baseType);
    }

    static class ParameterizedTypeImpl implements ParameterizedType {
        private Type[] actualTypeArguments;
        private Class<?> rawType;
        private Type ownerType;

        ParameterizedTypeImpl(Class<?> rawType, Type[] actualTypeArguments, Type ownerType) {
            this.actualTypeArguments = actualTypeArguments;
            this.rawType = rawType;
            if (ownerType != null) {
                this.ownerType = ownerType;
            } else {
                this.ownerType = rawType.getDeclaringClass();
            }
            validateConstructorArguments();
        }

        private void validateConstructorArguments() {
            TypeVariable /*<?>*/[] formals = rawType.getTypeParameters();
            // check correct arity of actual type args
            if (formals.length != actualTypeArguments.length) {
                throw new MalformedParameterizedTypeException();
            }
            for (int i = 0; i < actualTypeArguments.length; i++) {
                // check actuals against formals' bounds
            }
        }

        @Override
        public Type[] getActualTypeArguments() {
            return actualTypeArguments.clone();
        }

        @Override
        public Class<?> getRawType() {
            return rawType;
        }

        @Override
        public Type getOwnerType() {
            return ownerType;
        }

        /*
         * From the JavaDoc for java.lang.reflect.ParameterizedType
         * "Instances of classes that implement this interface must
         * implement an equals() method that equates any two instances
         * that share the same generic type declaration and have equal
         * type parameters."
         */
        @Override
        public boolean equals(Object o) {
            if (o instanceof ParameterizedType) {
                // Check that information is equivalent
                ParameterizedType that = (ParameterizedType) o;

                if (this == that) {
                    return true;
                }

                Type thatOwner = that.getOwnerType();
                Type thatRawType = that.getRawType();

                if (false) { // Debugging
                    boolean ownerEquality = Objects.equals(ownerType, thatOwner);
                    boolean rawEquality = Objects.equals(rawType, thatRawType);

                    boolean typeArgEquality = Arrays.equals(
                            actualTypeArguments, // avoid clone
                            that.getActualTypeArguments());
                    for (Type t : actualTypeArguments) {
                        System.out.printf("\t\t%s%s%n", t, t.getClass());
                    }

                    System.out.printf("\towner %s\traw %s\ttypeArg %s%n", ownerEquality, rawEquality, typeArgEquality);
                    return ownerEquality && rawEquality && typeArgEquality;
                }

                return Objects.equals(ownerType, thatOwner)
                        && Objects.equals(rawType, thatRawType)
                        && Arrays.equals(
                                actualTypeArguments, // avoid clone
                                that.getActualTypeArguments());
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(actualTypeArguments)
                    ^ (ownerType == null ? 0 : ownerType.hashCode())
                    ^ (rawType == null ? 0 : rawType.hashCode());
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();

            if (ownerType != null) {
                if (ownerType instanceof Class) {
                    sb.append(((Class) ownerType).getName());
                } else {
                    sb.append(ownerType.toString());
                }

                sb.append(".");

                if (ownerType instanceof ParameterizedTypeImpl) {
                    // Find simple name of nested type by removing the
                    // shared prefix with owner.
                    sb.append(
                            rawType.getName().replace(((ParameterizedTypeImpl) ownerType).rawType.getName() + "$", ""));
                } else {
                    sb.append(rawType.getName());
                }
            } else {
                sb.append(rawType.getName());
            }

            if (actualTypeArguments != null && actualTypeArguments.length > 0) {
                sb.append("<");
                boolean first = true;
                for (Type t : actualTypeArguments) {
                    if (!first) {
                        sb.append(", ");
                    }
                    if (t instanceof Class) {
                        sb.append(((Class) t).getName());
                    } else {
                        sb.append(t.toString());
                    }
                    first = false;
                }
                sb.append(">");
            }

            return sb.toString();
        }
    }

    static final class GenericArrayTypeImpl implements GenericArrayType {
        private Type genericComponentType;

        GenericArrayTypeImpl(Type ct) {
            assert ct != null;
            genericComponentType = ct;
        }

        /**
         * Returns  a {@code Type} object representing the component type
         * of this array.
         *
         * @return a {@code Type} object representing the component type
         *         of this array
         * @since 1.5
         */
        @Override
        public Type getGenericComponentType() {
            return genericComponentType; // return cached component type
        }

        @Override
        public String toString() {
            Type componentType = getGenericComponentType();
            StringBuilder sb = new StringBuilder();

            if (componentType instanceof Class) {
                sb.append(((Class) componentType).getName());
            } else {
                sb.append(componentType.toString());
            }
            sb.append("[]");
            return sb.toString();
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof GenericArrayType) {
                GenericArrayType that = (GenericArrayType) o;

                Type thatComponentType = that.getGenericComponentType();
                return genericComponentType.equals(thatComponentType);
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            return genericComponentType.hashCode();
        }
    }

    static final class WildcardTypeImpl implements WildcardType {

        private final Type[] ub;
        private final Type[] lb;

        WildcardTypeImpl(Type[] ub, Type[] lb) {
            this.ub = ub;
            this.lb = lb;
        }

        @Override
        public Type[] getUpperBounds() {
            return ub;
        }

        @Override
        public Type[] getLowerBounds() {
            return lb;
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(lb) ^ Arrays.hashCode(ub);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof WildcardType) {
                WildcardType that = (WildcardType) obj;
                return Arrays.equals(that.getLowerBounds(), lb) && Arrays.equals(that.getUpperBounds(), ub);
            }
            return false;
        }
    }

    public static Type getTypeArgument(Type type, int i) {
        if (type instanceof ParameterizedType) {
            ParameterizedType p = (ParameterizedType) type;
            return fix(p.getActualTypeArguments()[i]);
        } else {
            throw new IllegalArgumentException();
        }
    }

    /**
     * JDK 5.0 has a bug of creating {@link GenericArrayType} where it shouldn't.
     * fix that manually to work around the problem.
     *
     * See bug 6202725.
     */
    private static Type fix(Type t) {
        if (!(t instanceof GenericArrayType)) {
            return t;
        }

        GenericArrayType gat = (GenericArrayType) t;
        if (gat.getGenericComponentType() instanceof Class) {
            Class c = (Class) gat.getGenericComponentType();
            return Array.newInstance(c, 0).getClass();
        }

        return t;
    }
}
