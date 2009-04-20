package org.kohsuke.stapler;

import net.sf.json.JSONObject;

import javax.servlet.ServletContext;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Vector;
import java.util.WeakHashMap;
import java.util.Hashtable;

/**
 * Object scoped to the entire webapp. Mostly used for configuring behavior of Stapler.
 *
 * <p>
 * In contrast, {@link Stapler} is a servlet, so there can be multiple instances per webapp.
 *
 * @author Kohsuke Kawaguchi
 */
public class WebApp {
    /**
     * Obtains the {@link WebApp} associated with the given {@link ServletContext}.
     */
    public static WebApp get(ServletContext context) {
        Object o = context.getAttribute(WebApp.class.getName());
        if(o==null) {
            synchronized (WebApp.class) {
                o = context.getAttribute(WebApp.class.getName());
                if(o==null) {
                    o = new WebApp(context);
                    context.setAttribute(WebApp.class.getName(),o);
                }
            }
        }
        return (WebApp)o;
    }

    /**
     * {@link ServletContext} for this webapp.
     */
    public final ServletContext context;

    /**
     * Duck-type wrappers for the given class.
     */
    public final Map<Class,Class[]> wrappers = new HashMap<Class,Class[]>();

    /**
     * MIME type -> encoding map that determines how static contents in the war file is served.
     */
    public final Map<String,String> defaultEncodingForStaticResources = new HashMap<String,String>();

    /**
     * Activated facets.
     *
     * TODO: is this really mutable?
     */
    public final List<Facet> facets = new Vector<Facet>();

    /**
     * MIME type mapping from extensions (like "txt" or "jpg") to MIME types ("foo/bar").
     *
     * This overrides whatever mappings given in the servlet as far as stapler is concerned.
     * This is case insensitive, and should be normalized to lower case.
     */
    public final Map<String,String> mimeTypes = new Hashtable<String,String>();

    private volatile ClassLoader classLoader;

    /**
     * All {@link MetaClass}es.
     *
     * Avoids class leaks by {@link WeakHashMap}.
     */
    private final Map<Class,MetaClass> classMap = new WeakHashMap<Class,MetaClass>();

    public WebApp(ServletContext context) {
        this.context = context;
        // TODO: allow classloader to be given?
        facets.addAll(Facet.discover(Thread.currentThread().getContextClassLoader()));
    }

    public ClassLoader getClassLoader() {
        ClassLoader cl = classLoader;
        if(cl==null)
            cl = Thread.currentThread().getContextClassLoader();
        if(cl==null)
            cl = Stapler.class.getClassLoader();
        return cl;
    }

    /**
     * If the facet of the given type exists, return it. Otherwise null.
     */
    public <T extends Facet> T getFacet(Class<T> type) {
        for (Facet f : facets)
            if(type==f.getClass())
                return type.cast(f);
        return null;
    }

    /**
     * Sets the classloader used by {@link StaplerRequest#bindJSON(Class, JSONObject)} and its sibling methods.
     */
    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public MetaClass getMetaClass(Class c) {
        if(c==null)     return null;
        synchronized(classMap) {
            MetaClass mc = classMap.get(c);
            if(mc==null) {
                mc = new MetaClass(this,c);
                classMap.put(c,mc);
            }
            return mc;
        }
    }

    /**
     * Gets the current {@link WebApp} that the calling thread is associated with.
     */
    public static WebApp getCurrent() {
        return Stapler.getCurrent().getWebApp();
    }
}
