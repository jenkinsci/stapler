package org.kohsuke.stapler.lang;

import org.kohsuke.stapler.Function;

import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;

/**
 * Abstraction of class-like object, agnostic to languages.
 *
 * <p>
 * To support other JVM languages that use their own specific types to represent a class
 * (such as JRuby and Jython), we now use this object instead of {@link Class}. This allows
 * us to reuse much of the logic of class traversal/resource lookup across different languages.
 * 
 * This is a convenient tuple so that we can pass around a single argument instead of two.
 *
 * @param <C>
 *     Variable that represents the type of {@code Class} like object in this language.
 *
 * @author Kohsuke Kawaguchi
 */
public final class Klass<C> {
    public final C clazz;
    public final KlassNavigator<C> navigator;

    public Klass(C clazz, KlassNavigator<C> navigator) {
        this.clazz = clazz;
        this.navigator = navigator;
    }

    public URL getResource(String resourceName) {
        return navigator.getResource(clazz,resourceName);
    }

    public Iterable<Klass<?>> getAncestors() {
        return navigator.getAncestors(clazz);
    }
    
    public Klass<?> getSuperClass() {
        return navigator.getSuperClass(clazz);
    }

    public Class toJavaClass() {
        return navigator.toJavaClass(clazz);
    }

    /**
     * @since 1.220
     */
    public List<MethodRef> getDeclaredMethods() {
        return navigator.getDeclaredMethods(clazz);
    }

    /**
     * Gets list of fields declared by the class.
     * @return List of fields. 
     *         May return empty list in the case of obsolete {@link #navigator}, which does not offer the method.
     * @since 1.246
     */
    @Nonnull
    public List<FieldRef> getDeclaredFields() {
        return navigator.getDeclaredFields(clazz);
    }

    /**
     * Gets all the public fields defined in this type, including super types.
     *
     * @see Class#getFields()
     */
    public List<FieldRef> getFields() {
        Map<String,FieldRef> fields = new LinkedHashMap<String,FieldRef>();
        for (Klass<?> k = this; k!=null; k=k.getSuperClass()) {
            for (FieldRef f : k.getDeclaredFields()) {
                String name = f.getName();
                if (!fields.containsKey(name) && f.isRoutable()) {
                    fields.put(name,f);
                }
            }
        }

        return new ArrayList<FieldRef>(fields.values());
    }

    /**
     * Reports all the methods that can be used for routing requests on this class.
     * @return List of functions. 
     *         May return empty list in the case of obsolete {@link #navigator}, which does not offer the method.
     * @since 1.246
     */
    @Nonnull
    public List<Function> getFunctions() {
        return navigator.getFunctions(clazz);
    }

    public boolean isArray() {
        return navigator.isArray(clazz);
    }

    public Object getArrayElement(Object o, int index) throws IndexOutOfBoundsException {
        return navigator.getArrayElement(o,index);
    }

    public boolean isMap() {
        return navigator.isMap(clazz);
    }

    public Object getMapElement(Object o, String key) {
        return navigator.getMapElement(o,key);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Klass that = (Klass) o;
        return clazz.equals(that.clazz) && navigator.equals(that.navigator);

    }

    @Override
    public int hashCode() {
        return 31 * clazz.hashCode() + navigator.hashCode();
    }

    @Override
    public String toString() {
        return clazz.toString();
    }

    /**
     * Creates {@link Klass} from a Java {@link Class}.
     */
    public static Klass<Class> java(Class c) {
        return c == null ? null : new Klass<Class>(c, KlassNavigator.JAVA);
    }
}
