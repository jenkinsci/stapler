package org.kohsuke.stapler.lang;

import org.kohsuke.stapler.ClassDescriptor;
import org.kohsuke.stapler.Function;
import org.kohsuke.stapler.MetaClassLoader;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * Strategy pattern to provide navigation across class-like objects in other languages of JVM.
 *
 * <p>
 * Implementations should be stateless and typically a singleton.
 *
 * @param <C>
 *     Variable that represents the type of {@code Class} like object in this language.
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class KlassNavigator<C> {
    /**
     * Loads the resources associated with this class.
     *
     * <p>
     * In stapler, the convention is that the "associated" resources live in the directory named after
     * the fully qualified class name (as opposed to the behavior of {@link Class#getResource(String)},
     * that looks for resources in the same package as the class.)
     *
     * <p>
     * But other languages can choose their own conventions if it makes more sense to do so.
     * For example, stapler-jruby uses camelized class name.
     *
     * <p>
     * Implementation must consult {@link MetaClassLoader#debugLoader} if it's available. Implementation
     * must not look for resources in the base type. That operation is performed by the caller when
     * needed.
     *
     * @return
     *      non-null if the resource is found. Otherwise null.
     */
    public abstract URL getResource(C clazz, String resourceName);

    /**
     * Lists up all the ancestor classes, from specific to general, without any duplicate.
     *
     * This is used to look up a resource.
     */
    public abstract Iterable<Klass<?>> getAncestors(C clazz);

    /**
     * Gets the super class.
     *
     * @return
     *      Can be null.
     */
    public abstract Klass<?> getSuperClass(C clazz);

    /**
     * For backward compatibility, map the given class to the closest Java equivalent.
     * In the worst case, this is Object.class
     */
    public abstract Class toJavaClass(C clazz);

    /**
     * List methods of this class, regardless of access modifier.
     *
     * This list excludes methods from super classes.
     * @since 1.220
     */
    public abstract List<MethodRef> getDeclaredMethods(C clazz);

    /**
     * List fields of this class.
     * This list excludes fields from super classes.
     * @param clazz Class
     * @return List of the fields declared for the class.
     *         By default this list is empty, {@link KlassNavigator} implementations are responsible to implement it.
     * @since 1.246
     */
    @NonNull
    public List<FieldRef> getDeclaredFields(C clazz) {
        return Collections.emptyList();
    }

    /**
     * Reports all the methods that can be used for routing requests on this class.
     * @param clazz Class
     * @return List of the fields functions declared for the class.
     *         By default this list is empty, {@link KlassNavigator} implementations are responsible to implement it.
     * @since 1.246
     */
    @NonNull
    public List<Function> getFunctions(C clazz) {
        return Collections.emptyList();
    }

    /**
     * If the given type is an array that supports index retrieval.
     * @see #getArrayElement(Object, int)
     */
    public boolean isArray(C clazz) {
        Class j = toJavaClass(clazz);
        return j.isArray() || List.class.isAssignableFrom(j);
    }

    /**
     * Given an instance for which the type reported {@code isArray()==true}, obtains the element
     * of the specified index.
     * @see #isArray(Object)
     */
    public Object getArrayElement(Object o, int index) throws IndexOutOfBoundsException {
        if (o instanceof List)
            return ((List)o).get(index);
        return Array.get(o,index);
    }

    /**
     * If the given type is a map/associative array type that supports lookup by a string key
     */
    public boolean isMap(C clazz) {
        return Map.class.isAssignableFrom(toJavaClass(clazz));
    }

    /**
     * Given an instance for which the type reported {@code isMap()==true}, obtains the element
     * of the specified index.
     */
    public Object getMapElement(Object o, String key) {
        return ((Map)o).get(key);
    }

    public static final KlassNavigator<Class> JAVA = new KlassNavigator<Class>() {
        @Override
        public URL getResource(Class clazz, String resourceName) {
            ClassLoader cl = clazz.getClassLoader();
            if (cl==null)   return null;

            String fullName;
            if (resourceName.startsWith("/"))
                fullName = resourceName.substring(1);
            else
                fullName = clazz.getName().replace('.','/').replace('$','/')+'/'+resourceName;

            if (MetaClassLoader.debugLoader!=null) {
                URL res = MetaClassLoader.debugLoader.loader.getResource(fullName);
                if (res!=null)  return res;
            }
            return cl.getResource(fullName);
        }

        @Override
        public Klass<Class> getSuperClass(Class clazz) {
            return Klass.java(clazz.getSuperclass());
        }

        @Override
        public Iterable<Klass<?>> getAncestors(Class clazz) {
            // TODO: shall we support interfaces?
            List<Klass<?>> r = new ArrayList<Klass<?>>();
            for (; clazz!=null; clazz=clazz.getSuperclass()) {
                r.add(Klass.java(clazz));
            }
            return r;
        }

        @Override
        public Class toJavaClass(Class clazz) {
            return clazz;
        }

        @Override
        public List<MethodRef> getDeclaredMethods(Class clazz) {
            final Method[] methods = clazz.getDeclaredMethods();
            return new AbstractList<MethodRef>() {
                @Override
                public MethodRef get(int index) {
                    return MethodRef.wrap(methods[index]);
                }

                @Override
                public int size() {
                    return methods.length;
                }
            };
        }

        @Override
        public List<FieldRef> getDeclaredFields(Class clazz) {
            final Field[] fields = clazz.getDeclaredFields();
            return new AbstractList<FieldRef>() {
                @Override
                public FieldRef get(int index) {
                    return FieldRef.wrap(fields[index]);
                }

                @Override
                public int size() {
                    return fields.length;
                }
            };
        }

        @Override
        public List<Function> getFunctions(Class clazz) {
            // Historically ClassDescriptor used to own this non-trivial logic of computing
            // valid functions for the class, so we'll keep it there.
            return new ClassDescriptor(clazz).methods;
        }
    };
}
