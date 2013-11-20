package org.kohsuke.stapler.lang;

import java.net.URL;
import java.util.List;

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
 * @author Kohsuke Kawaguchi
 */
public class Klass<C> {
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
