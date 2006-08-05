package org.kohsuke.stapler;

import org.apache.commons.jelly.JellyContext;
import org.apache.commons.jelly.TagLibrary;
import org.kohsuke.stapler.jelly.CustomTagLibrary;
import org.kohsuke.stapler.jelly.StaplerTagLibrary;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * The stapler version of the {@link ClassLoader} object,
 * that retains some useful cache about a class loader.
 *
 * @author Kohsuke Kawaguchi
 */
public class MetaClassLoader {
    private final MetaClassLoader parent;

    private final ClassLoader loader;

    private final Map<String,TagLibrary> taglibs = new HashMap<String,TagLibrary>();

    public MetaClassLoader(ClassLoader loader) {
        this.loader = loader;
        this.parent = get(loader.getParent());
    }

    public synchronized TagLibrary getTagLibrary(String nsUri) {
        TagLibrary tl=null;

        if(parent!=null)        // parent first
            tl = parent.getTagLibrary(nsUri);

        if(tl==null)        // then see if it's cached
            tl = taglibs.get(nsUri);

        if(tl==null) { // can we load them here?
            URL res = loader.getResource(nsUri+"/taglib");
            if(res!=null) {
                tl = new CustomTagLibrary(ROOT_CONTEXT,loader,nsUri);
                taglibs.put(nsUri,tl);
            }
        }

        return tl;
    }

    /**
     * Creates {@link JellyContext} for compiling view scripts
     * for classes in this classloader.
     */
    public JellyContext craeteContext() {
        JellyContext context = new JellyContext(ROOT_CONTEXT) {
            public TagLibrary getTagLibrary(String namespaceURI) {
                TagLibrary tl = super.getTagLibrary(namespaceURI);
                // attempt to resolve nsUri from taglibs
                if(tl==null) {
                    tl = MetaClassLoader.this.getTagLibrary(namespaceURI);
                    if(tl!=null)
                        registerTagLibrary(namespaceURI,tl);
                }
                return tl;
            }
        };
        context.setExportLibraries(false);
        return context;
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


    /**
     * Used as the root context for compiling scripts.
     */
    private static final JellyContext ROOT_CONTEXT = new JellyContext();

    static {
        ROOT_CONTEXT.registerTagLibrary("jelly:stapler",new StaplerTagLibrary());
    }
}
