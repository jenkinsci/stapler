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

import net.sf.json.JSONObject;
import org.kohsuke.stapler.bind.BoundObjectTable;
import org.kohsuke.stapler.lang.Klass;

import javax.servlet.ServletContext;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import java.util.WeakHashMap;
import java.util.Hashtable;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Object scoped to the entire webapp. Mostly used for configuring behavior of Stapler.
 *
 * <p>
 * In contrast, {@link Stapler} is a servlet, so there can be multiple instances per webapp.
 *
 * @author Kohsuke Kawaguchi
 * @see WebApp#get(ServletContext)
 * @see WebApp#getCurrent()  
 * @see Stapler#getWebApp()
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
    private final Map<Klass<?>,MetaClass> classMap = new WeakHashMap<Klass<?>,MetaClass>();

    /**
     * Handles objects that are exported.
     */
    public final BoundObjectTable boundObjectTable = new BoundObjectTable();

    private final CopyOnWriteArrayList<HttpResponseRenderer> responseRenderers = new CopyOnWriteArrayList<HttpResponseRenderer>();

    private CrumbIssuer crumbIssuer = CrumbIssuer.DEFAULT;

    public WebApp(ServletContext context) {
        this.context = context;
        // TODO: allow classloader to be given?
        facets.addAll(Facet.discoverExtensions(Facet.class, Thread.currentThread().getContextClassLoader(), getClass().getClassLoader()));
        responseRenderers.add(new HttpResponseRenderer.Default());
    }

    /**
     * Returns the 'app' object, which is the user-specified object that
     * sits at the root of the URL hierarchy and handles the request to '/'.
     */
    public Object getApp() {
        return context.getAttribute("app");
    }

    public void setApp(Object app) {
        context.setAttribute("app",app);
    }

    public CrumbIssuer getCrumbIssuer() {
        return crumbIssuer;
    }

    public void setCrumbIssuer(CrumbIssuer crumbIssuer) {
        this.crumbIssuer = crumbIssuer;
    }

    public CopyOnWriteArrayList<HttpResponseRenderer> getResponseRenderers() {
        return responseRenderers;
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
        return getMetaClass(Klass.java(c));
    }
    
    public MetaClass getMetaClass(Klass<?> c) {
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
     * Obtains a {@link MetaClass} that represents the type of the given object.
     *
     * <p>
     * This code consults all facets to handle scripting language objects correctly.
     */
    public MetaClass getMetaClass(Object o) {
        return getMetaClass(getKlass(o));
    }

    public Klass<?> getKlass(Object o) {
        for (Facet f : facets) {
            Klass<?> k = f.getKlass(o);
            if (k!=null)
                return k;
        }
        return Klass.java(o.getClass());
    }
    
    /**
     * Convenience maintenance method to clear all the cached scripts for the given tearoff type.
     *
     * <p>
     * This is useful when you want to have the scripts reloaded into the live system without
     * the performance penalty of {@link MetaClass#NO_CACHE}.
     *
     * @see MetaClass#NO_CACHE
     */
    public void clearScripts(Class<? extends AbstractTearOff> clazz) {
        synchronized (classMap) {
            for (MetaClass v : classMap.values()) {
                AbstractTearOff t = v.getTearOff(clazz);
                if (t!=null)
                    t.clearScripts();
            }
        }
    }

    /**
     * Gets the current {@link WebApp} that the calling thread is associated with.
     */
    public static WebApp getCurrent() {
        return Stapler.getCurrent().getWebApp();
    }
}
