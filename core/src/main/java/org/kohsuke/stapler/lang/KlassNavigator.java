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
     * Implementation must consult {@link MetaClassLoader#debugLoader} if it's available.
     */
    public abstract URL getResource(C clazz, String resourceName);

    /**
     * Lists up all the ancestor classes, from specific to general, without any duplicate.
     *
     * This is used to look up a resource.
     */
    public abstract Iterable<Klass<?>> getAncestors(C clazz);

    public URL lookupResourceFromInheritanceTree(C clazz, String resourceName) {
        for (Klass<?> c : getAncestors(clazz)) {
            URL url = c.getResource(resourceName);
            if (url!=null)  return url;
        }
        return null;
    }

    public abstract Klass<?> getSuperClass(C clazz);

    /**
     * For backward compatibility, map the given class to the closest Java equivalent.
     * In the worst case, this is Object.class
     */
    public abstract Class toJavaClass(C clazz);

    public static final KlassNavigator<Class> JAVA = new KlassNavigator<Class>() {
        @Override
        public URL getResource(Class clazz, String resourceName) {
            String fullName;
            if (resourceName.startsWith("/"))
                fullName = resourceName.substring(1);
            else
                fullName = clazz.getName().replace('.','/').replace('$','/')+'/'+resourceName;

            if (MetaClassLoader.debugLoader!=null) {
                URL res = MetaClassLoader.debugLoader.loader.getResource(fullName);
                if (res!=null)  return res;
            }
            return clazz.getClassLoader().getResource(fullName);
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
