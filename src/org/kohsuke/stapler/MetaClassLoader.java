package org.kohsuke.stapler;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * The stapler version of the {@link ClassLoader} object,
 * that retains some useful cache about a class loader.
 *
 * @author Kohsuke Kawaguchi
 */
public class MetaClassLoader extends TearOffSupport {
    public final MetaClassLoader parent;
    public final ClassLoader loader;

    public MetaClassLoader(ClassLoader loader) {
        this.loader = loader;
        this.parent = get(loader.getParent());
    }

    public static MetaClassLoader get(ClassLoader cl) {
        if(cl ==null)     return null;
        synchronized(classMap) {
            MetaClassLoader mc = classMap.get(cl);
            if(mc==null) {
                mc = new MetaClassLoader(cl);
                classMap.put(cl,mc);
            }
            return mc;
        }
    }

    /**
     * All {@link MetaClass}es.
     *
     * Avoids class leaks by {@link WeakHashMap}.
     */
    private static final Map<ClassLoader,MetaClassLoader> classMap = new WeakHashMap<ClassLoader,MetaClassLoader>();
}
