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

import org.apache.commons.io.IOUtils;
import org.jvnet.tiger_types.Types;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Logger;
import java.util.stream.Stream;

import static java.util.logging.Level.FINE;
import static java.util.logging.Level.WARNING;

/**
 * Reflection information of a {@link Class}.
 *
 * @author Kohsuke Kawaguchi
 */
public final class ClassDescriptor {
    public final Class clazz;
    public final FunctionList methods;
    public final Field[] fields;

    /**
     * @param clazz
     *      The class to build a descriptor around.
     * @param wrappers
     *      Optional wrapper duck-typing classes.
     *      Static methods on this class that has the first parameter
     *      as 'clazz' will be handled as if it's instance methods on
     *      'clazz'. Useful for adding view/controller methods on
     *      model classes.
     */
    public ClassDescriptor(Class clazz, Class... wrappers) {
        this.clazz = clazz;
        this.fields = clazz.getFields();

        // instance methods
        List<MethodMirror> methods = new ArrayList<>();
        findMethods(clazz,clazz,methods,new HashSet<>());

        // organize them into groups
        Map<Signature,List<Method>> groups = new LinkedHashMap<>();
        for (MethodMirror m : methods) {
            List<Method> v = groups.computeIfAbsent(m.sig, unused -> new ArrayList<>());
            v.add(m.method);
        }

        // build functions from groups
        List<Function> functions = new ArrayList<>();
        for (List<Method> m : groups.values()) {
            if (m.size()==1) {
                Method one = m.get(0);
                functions.add(new Function.InstanceFunction(one)
                        .wrapByInterceptors(one));
            } else {
                Collections.reverse(m);
                functions.add(new Function.OverridingInstanceFunction(m)
                        .wrapByInterceptors(new UnionAnnotatedElement(m)));
            }
        }

        if(wrappers!=null) {
            for (Class w : wrappers) {
                for (Method m : w.getMethods()) {
                    if(!Modifier.isStatic(m.getModifiers()))
                        continue;
                    Class<?>[] p = m.getParameterTypes();
                    if(p.length==0)
                        continue;
                    if(p[0].isAssignableFrom(clazz))
                        continue;
                    functions.add(new Function.StaticFunction(m).wrapByInterceptors(m));
                }
            }
        }
        this.methods = new FunctionList(functions);
    }

    /**
     * Finds all the public methods of 'c' across class/interface hierarchy and accumulates into a list,
     * from ancestor first.
     *
     * @param logical
     *      Actual type parameterization of 'c' in this type hierarchy. For example, methods
     *      we discover on {@code Collection<String>} is different from methods we discover on
     *      {@code Collection<Integer>}. This type arguments decorates 'c' to carry this semantics.
     *      Erasure of this is always {@code c}.
     *
     */
    private List<MethodMirror> findMethods(Class c, java.lang.reflect.Type logical, List<MethodMirror> result, Set<Class> visited) {
        if (!visited.add(c))
            return result; // avoid visiting the same type twice

        // visit interfaces first so that class methods are considered as overriding interface methods
        for (Class i : c.getInterfaces()) {
            findMethods(i, Types.getBaseClass(logical,i), result, visited);
        }
        Class sc = c.getSuperclass();
        if (sc!=null)
            findMethods(sc,Types.getBaseClass(logical,sc),result,visited);

        Method[] declaredMethods = c.getDeclaredMethods();
        Arrays.sort(declaredMethods, new Comparator<Method>() {
            @Override
            public int compare(Method m1, Method m2) {
                boolean m1d = m1.getAnnotation(Deprecated.class) != null;
                boolean m2d = m2.getAnnotation(Deprecated.class) != null;
                if (m1d && !m2d) {
                    // Prefer nondeprecated to deprecated.
                    return 1;
                } else if (!m1d && m2d) {
                    return -1;
                } else {
                    // Sort by string representation, so for example doFoo() is preferred to doFoo(StaplerRequest, StaplerResponse).
                    return m1.toString().compareTo(m2.toString());
                }
            }
        });
        for (Method m : declaredMethods) {
            if (m.isBridge())    continue;
            if ((m.getModifiers() & Modifier.PUBLIC)!=0) {
                java.lang.reflect.Type[] paramTypes = m.getGenericParameterTypes();
                Class[] erasedParamTypes = new Class[paramTypes.length];
                for (int i = 0; i < paramTypes.length; i++) {
                    if (logical instanceof ParameterizedType)
                        erasedParamTypes[i] = Types.erasure(Types.bind(paramTypes[i], c, (ParameterizedType)logical));
                    else
                        erasedParamTypes[i] = Types.erasure(paramTypes[i]);
                }

                result.add(new MethodMirror(new Signature(m.getName(), erasedParamTypes), m));
            }
        }
        return result;
    }

    /**
     * Loads the list of parameter names of the given method, by using a stapler-specific way of getting it.
     *
     * <p>
     * This is not the best place to expose this, but for now this would do.
     */
    public static String[] loadParameterNames(Method m) {
        CapturedParameterNames cpn = m.getAnnotation(CapturedParameterNames.class);
        if(cpn!=null)   return cpn.value();

        // reflection is the most efficient and supported system
        String[] n = loadParameterNamesFromReflection(m);
        if (n != null) {
            return n;
        }

        // debug information, if present, is more trustworthy
        try {
            n = ASM.loadParametersFromAsm(m);
            if (n!=null)    return n;
        } catch (LinkageError e) {
            LOGGER.log(FINE, "Incompatible ASM", e);
        } catch (IOException e) {
            LOGGER.log(WARNING, "Failed to load a class file", e);
        }

        // otherwise check the .stapler file
        Class<?> c = m.getDeclaringClass();
        URL url = c.getClassLoader().getResource(
                    c.getName().replace('.', '/').replace('$','/') + '/' + m.getName() + ".stapler");
        if(url!=null) {
            try {
                return IOUtils.toString(url.openStream()).split(",");
            } catch (IOException e) {
                LOGGER.log(WARNING, "Failed to load "+url,e);
                return EMPTY_ARRAY;
            }
        }

        // couldn't find it
        return EMPTY_ARRAY;
    }

    /**
     * Loads the list of parameter names of the given method, by using a stapler-specific way of getting it.
     *
     * <p>
     * This is not the best place to expose this, but for now this would do.
     */
    public static String[] loadParameterNames(Constructor<?> m) {
        CapturedParameterNames cpn = m.getAnnotation(CapturedParameterNames.class);
        if(cpn!=null)   return cpn.value();

        // reflection is the most efficient and supported system
        String[] n = loadParameterNamesFromReflection(m);
        if (n != null) {
            return n;
        }

        // debug information, if present, is more trustworthy
        try {
            n = ASM.loadParametersFromAsm(m);
            if (n!=null)    return n;
        } catch (LinkageError e) {
            LOGGER.log(FINE, "Incompatible ASM", e);
        } catch (IOException e) {
            LOGGER.log(WARNING, "Failed to load a class file", e);
        }

        // couldn't find it
        return EMPTY_ARRAY;
    }

    static String[] loadParameterNamesFromReflection(final Executable m) {
        Parameter[] ps = m.getParameters();
        if (Stream.of(ps).allMatch(Parameter::isNamePresent)) {
            return Stream.of(ps).map(Parameter::getName).toArray(String[]::new);
        } else {
            return null;
        }
    }

    /**
     * Determines the constructor parameter names.
     *
     * <p>
     * First, try to load names from the debug information. Otherwise
     * if there's the .stapler file, load it as a property file and determines the constructor parameter names.
     * Otherwise, look for {@link CapturedParameterNames} annotation.
     */
    public String[] loadConstructorParamNames() {
        Constructor<?>[] ctrs = clazz.getConstructors();
        // which constructor was data bound?
        Constructor<?> dbc = null;
        for (Constructor<?> c : ctrs) {
            if (c.getAnnotation(DataBoundConstructor.class) != null) {
                dbc = c;
                break;
            }
        }

        if (dbc==null)
            throw new NoStaplerConstructorException("There's no @DataBoundConstructor on any constructor of " + clazz);

        String[] names = ClassDescriptor.loadParameterNames(dbc);
        if (names.length==dbc.getParameterTypes().length)
            return names;

        String resourceName = clazz.getName().replace('.', '/').replace('$','/') + ".stapler";
        ClassLoader cl = clazz.getClassLoader();
        if(cl==null)
            throw new NoStaplerConstructorException(clazz+" is a built-in type");
        InputStream s = cl.getResourceAsStream(resourceName);
        if (s != null) {// load the property file and figure out parameter names
            try {
                Properties p = new Properties();
                p.load(s);
                s.close();

                String v = p.getProperty("constructor");
                if (v.length() == 0) return new String[0];
                return v.split(",");
            } catch (IOException e) {
                throw new IllegalArgumentException("Unable to load " + resourceName, e);
            }
        }

        // no debug info and no stapler file
        throw new NoStaplerConstructorException(
                "Unable to find " + resourceName + ". " +
                        "Run 'mvn clean compile' once to run the annotation processor.");
    }


    /**
     * Isolate the ASM dependency to its own class, as otherwise this seems to cause linkage error on the whole {@link ClassDescriptor}.
     */
    /*package*/ static class ASM {
        /**
         * Try to load parameter names from the debug info by using ASM.
         */
        private static String[] loadParametersFromAsm(final Method m) throws IOException {
            final String[] paramNames = new String[m.getParameterTypes().length];
            if (paramNames.length==0) return paramNames;
            Class<?> c = m.getDeclaringClass();
            URL clazz = c.getClassLoader().getResource(c.getName().replace('.', '/') + ".class");
            if (clazz==null)    return null;

            final TreeMap<Integer,String> localVars = new TreeMap<>();
            ClassReader r = new ClassReader(clazz.openStream());
            r.accept(new ClassVisitor(Opcodes.ASM9) {
                final String md = Type.getMethodDescriptor(m);
                // First localVariable is "this" for non-static method
                final int limit = (m.getModifiers() & Modifier.STATIC) != 0 ? 0 : 1;
                @Override public MethodVisitor visitMethod(int access, String methodName, String desc, String signature, String[] exceptions) {
                    if (methodName.equals(m.getName()) && desc.equals(md))
                        return new MethodVisitor(Opcodes.ASM9) {
                            @Override public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
                                if (index >= limit)
                                    localVars.put(index, name);
                            }
                        };
                    else
                        return null; // ignore this method
                }
            }, 0);

            // Indexes may not be sequential, but first set of local variables are method params
            int i = 0;
            for (String s : localVars.values()) {
                paramNames[i] = s;
                if (++i == paramNames.length) return paramNames;
            }
            return null; // Not enough data found to fill array
        }

        /**
         * Try to load parameter names from the debug info by using ASM.
         */
        private static String[] loadParametersFromAsm(final Constructor m) throws IOException {
            final String[] paramNames = new String[m.getParameterTypes().length];
            if (paramNames.length==0) return paramNames;
            Class<?> c = m.getDeclaringClass();
            URL clazz = c.getClassLoader().getResource(c.getName().replace('.', '/') + ".class");
            if (clazz==null)    return null;

            final TreeMap<Integer,String> localVars = new TreeMap<>();
            try (InputStream is = clazz.openStream()) {
                ClassReader r = new ClassReader(is);
                r.accept(new ClassVisitor(Opcodes.ASM9) {
                    final String md = getConstructorDescriptor(m);
                    public MethodVisitor visitMethod(int access, String methodName, String desc, String signature, String[] exceptions) {
                        if (methodName.equals("<init>") && desc.equals(md))
                            return new MethodVisitor(Opcodes.ASM9) {
                                @Override public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
                                    if (index>0)   // 0 is 'this'
                                        localVars.put(index, name);
                                }
                            };
                        else
                            return null; // ignore this method
                    }
                }, 0);
            }

            // Indexes may not be sequential, but first set of local variables are method params
            int i = 0;
            for (String s : localVars.values()) {
                paramNames[i] = s;
                if (++i == paramNames.length) return paramNames;
            }
            return null; // Not enough data found to fill array
        }

        private static String getConstructorDescriptor(Constructor c) {
            StringBuilder buf = new StringBuilder("(");
            for (Class p : c.getParameterTypes())
                buf.append(Type.getDescriptor(p));
            return buf.append(")V").toString();
        }
    }

    final static class MethodMirror {
        final Signature sig;
        final Method method;

        public MethodMirror(Signature sig, Method method) {
            this.sig = sig;
            this.method = method;
        }
    }

    /**
     * A method signature used to determine what methods override each other
     */
    final static class Signature {
        final String methodName;
        final Class[] parameters;

        Signature(String methodName, Class[] parameters) {
            this.methodName = methodName;
            this.parameters = parameters;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Signature that = (Signature) o;

            return this.methodName.equals(that.methodName)
                && Arrays.equals(this.parameters, that.parameters);

        }

        @Override
        public int hashCode() {
            return 31 * methodName.hashCode() + Arrays.hashCode(parameters);
        }
    }

    private static final Logger LOGGER = Logger.getLogger(ClassDescriptor.class.getName());
    private static final String[] EMPTY_ARRAY = new String[0];
}
