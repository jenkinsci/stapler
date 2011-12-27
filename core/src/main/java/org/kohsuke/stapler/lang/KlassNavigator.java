package org.kohsuke.stapler.lang;

import org.kohsuke.stapler.MetaClassLoader;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Strategy pattern to provide navigation across class-like objects in other languages of JVM.
 *
 * <p>
 * Implementations should be stateless and typically a singleton.
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
        public Klass<?> getSuperClass(Class clazz) {
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
    };
}
