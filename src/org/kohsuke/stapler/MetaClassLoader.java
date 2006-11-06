package org.kohsuke.stapler;

import java.util.Map;
import java.util.WeakHashMap;
import java.io.File;
import java.net.URLClassLoader;
import java.net.URL;
import java.net.MalformedURLException;

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
        if(cl ==null)
            return debugLoader; // if no parent, delegate to the debug loader if available.
        
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
     * If non-null, delegate to this classloader.
     */
    public static MetaClassLoader debugLoader = null;

    /**
     * All {@link MetaClass}es.
     *
     * Avoids class leaks by {@link WeakHashMap}.
     */
    private static final Map<ClassLoader,MetaClassLoader> classMap = new WeakHashMap<ClassLoader,MetaClassLoader>();

    static {
        try {
            String path = System.getProperty("stapler.resourcePath");
            if(path!=null) {
                debugLoader = new MetaClassLoader(
                    new URLClassLoader(new URL[]{new File(path).toURL()}));
            }
        } catch (MalformedURLException e) {
            throw new Error(e);
        }
    }
}
