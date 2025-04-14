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

import jakarta.servlet.Filter;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.bind.BoundObjectTable;
import org.kohsuke.stapler.event.FilteredDispatchTriggerListener;
import org.kohsuke.stapler.event.FilteredDoActionTriggerListener;
import org.kohsuke.stapler.event.FilteredFieldTriggerListener;
import org.kohsuke.stapler.event.FilteredGetterTriggerListener;
import org.kohsuke.stapler.lang.FieldRef;
import org.kohsuke.stapler.lang.KInstance;
import org.kohsuke.stapler.lang.Klass;
import org.kohsuke.stapler.lang.KlassNavigator;

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
        if (o == null) {
            synchronized (WebApp.class) {
                o = context.getAttribute(WebApp.class.getName());
                if (o == null) {
                    o = new WebApp(context);
                    context.setAttribute(WebApp.class.getName(), o);
                }
            }
        }
        return (WebApp) o;
    }

    /**
     * {@link ServletContext} for this webapp.
     */
    private final ServletContext servletContext;

    /**
     * @deprecated Unused?
     */
    @Deprecated
    public final Map<Class, Class[]> wrappers = new HashMap<>();

    /**
     * MIME type → encoding map that determines how static contents in the war file is served.
     */
    public final Map<String, String> defaultEncodingForStaticResources = new HashMap<>();

    /**
     * Activated facets.
     *
     * TODO: is this really mutable?
     */
    public final List<Facet> facets = new Vector<>();

    /**
     * Global {@link BindInterceptor}s.
     *
     * These are consulted after {@link StaplerRequest2#getBindInterceptor()} is consulted.
     * Global bind interceptors are useful to register webapp-wide conversion logic local to the application.
     * @since 1.220
     */
    public final List<BindInterceptor> bindInterceptors = new CopyOnWriteArrayList<>();

    /**
     * MIME type mapping from extensions (like "txt" or "jpg") to MIME types ("foo/bar").
     *
     * This overrides whatever mappings given in the servlet as far as stapler is concerned.
     * This is case insensitive, and should be normalized to lower case.
     *
     * @deprecated removed without replacement
     */
    @Deprecated
    public final Map<String, String> mimeTypes = new Hashtable<>();

    private volatile ClassLoader classLoader;

    /**
     * All {@link MetaClass}es.
     */
    private volatile ClassValue<MetaClass> classMap;

    /**
     * Handles objects that are exported.
     */
    public final BoundObjectTable boundObjectTable = new BoundObjectTable();

    private final CopyOnWriteArrayList<HttpResponseRenderer> responseRenderers = new CopyOnWriteArrayList<>();

    private CrumbIssuer crumbIssuer = CrumbIssuer.DEFAULT;

    /**
     * Provides access to {@link Stapler} servlet instances. This is useful
     * for sending a request over to stapler from a context outside Stapler,
     * such as in {@link Filter}.
     *
     * Keyed by {@link ServletConfig#getServletName()}.
     */
    private final ConcurrentMap<String, Stapler> servlets = new ConcurrentHashMap<>();

    /**
     * Give the application a possibility to filter the getterMethods
     */
    private FunctionList.Filter filterForGetMethods = FunctionList.Filter.ALWAYS_OK;

    private FunctionList.Filter filterForDoActions = FunctionList.Filter.ALWAYS_OK;
    private FieldRef.Filter filterForFields = FieldRef.Filter.ALWAYS_OK;

    private DispatchersFilter dispatchersFilter;
    private FilteredDoActionTriggerListener filteredDoActionTriggerListener = FilteredDoActionTriggerListener.JUST_WARN;
    private FilteredGetterTriggerListener filteredGetterTriggerListener = FilteredGetterTriggerListener.JUST_WARN;
    private FilteredFieldTriggerListener filteredFieldTriggerListener = FilteredFieldTriggerListener.JUST_WARN;

    private DispatchValidator dispatchValidator = DispatchValidator.DEFAULT;
    private FilteredDispatchTriggerListener filteredDispatchTriggerListener = FilteredDispatchTriggerListener.JUST_WARN;

    /**
     * Give the application a way to customize the JSON before putting it inside Stacktrace when something wrong happened.
     * By default it just returns the given JSON.
     */
    private JsonInErrorMessageSanitizer jsonInErrorMessageSanitizer;

    public WebApp(ServletContext context) {
        this.servletContext = context;
        // TODO: allow classloader to be given?
        facets.addAll(Facet.discoverExtensions(
                Facet.class,
                Thread.currentThread().getContextClassLoader(),
                getClass().getClassLoader()));
        responseRenderers.add(new HttpResponseRenderer.Default());
    }

    /**
     * Returns the 'app' object, which is the user-specified object that
     * sits at the root of the URL hierarchy and handles the request to '/'.
     */
    public Object getApp() {
        return servletContext.getAttribute("app");
    }

    public void setApp(Object app) {
        servletContext.setAttribute("app", app);
    }

    public ServletContext getServletContext() {
        return servletContext;
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
        if (cl == null) {
            cl = Thread.currentThread().getContextClassLoader();
        }
        if (cl == null) {
            cl = Stapler.class.getClassLoader();
        }
        return cl;
    }

    /**
     * If the facet of the given type exists, return it. Otherwise null.
     */
    public <T extends Facet> T getFacet(Class<T> type) {
        for (Facet f : facets) {
            if (type == f.getClass()) {
                return type.cast(f);
            }
        }
        return null;
    }

    /**
     * Sets the classloader used by {@link StaplerRequest2#bindJSON(Class, JSONObject)} and its sibling methods.
     */
    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    private ClassValue<MetaClass> getClassMap() {
        ClassValue<MetaClass> _classMap = classMap;
        if (_classMap == null) {
            synchronized (this) {
                _classMap = classMap;
                if (_classMap == null) {
                    classMap = _classMap = new ClassValue<>() {
                        @Override
                        protected MetaClass computeValue(Class<?> c) {
                            return new MetaClass(WebApp.this, Klass.java(c));
                        }
                    };
                }
            }
        }
        return _classMap;
    }

    public MetaClass getMetaClass(Class c) {
        return getMetaClass(Klass.java(c));
    }

    public MetaClass getMetaClass(Klass<?> c) {
        if (c == null) {
            return null;
        }
        if (c.navigator == KlassNavigator.JAVA) {
            return getClassMap().get(c.toJavaClass());
        } else {
            // probably now impossible outside tests
            return new MetaClass(this, c);
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
        if (o instanceof KInstance ki) {
            Klass k = ki.getKlass();
            if (k != null) {
                return k;
            }
        }

        for (Facet f : facets) {
            Klass<?> k = f.getKlass(o);
            if (k != null) {
                return k;
            }
        }
        return Klass.java(o.getClass());
    }

    /**
     * @deprecated Unused?
     */
    @Deprecated
    public void clearScripts(Class<? extends AbstractTearOff> clazz) {
        // ClassValue cannot enumerate entries.
        // If really needed, could be implemented by keeping a WeakHashMap<Class, Boolean>
        // of classes added to classMap by getMetaClass that we could enumerate.
        clearMetaClassCache();
    }

    /**
     * Convenience maintenance method to clear all the cached information. It will force the MetaClass to be rebuilt.
     *
     * <p>
     * Take care that the generation of MetaClass information takes a bit of time and so
     * this call should not be called too often
     */
    public synchronized void clearMetaClassCache() {
        // No ClassValue.clear() method, so need to just null it out instead.
        classMap = null;
    }

    void addStaplerServlet(String servletName, Stapler servlet) {
        if (servletName == null) {
            servletName = ""; // be defensive
        }
        servlets.put(servletName, servlet);
    }

    /**
     * Gets a reference to some {@link Stapler} servlet in this webapp.
     *
     * <p>
     * Most Stapler webapps will have one {@code <servlet>} entry in web.xml
     * and if that's the case, that'd be returned. In a fully general case,
     * a webapp can have multiple servlets and more than one of them can be
     * {@link Stapler}. This method returns one of those. Which one gets
     * returned is unspecified.
     *
     * <p>
     * This method is useful if you are in a {@link Filter} and using
     * Stapler to handle the current request. For example,
     *
     * <pre>
     * WebApp.get(servletContext).getSomeStapler().invoke(
     *     request,response,
     *     someJavaObject,
     *     "/path/to/dispatch/request");
     * </pre>
     */
    public Stapler getSomeStapler() {
        return servlets.values().iterator().next();
    }

    /**
     * Gets the current {@link WebApp} that the calling thread is associated with.
     */
    public static WebApp getCurrent() {
        return Stapler.getCurrent().getWebApp();
    }

    public FunctionList.Filter getFilterForGetMethods() {
        return filterForGetMethods;
    }

    /**
     * Allow the underlying application to filter the getXxx methods
     */
    public void setFilterForGetMethods(FunctionList.Filter filterForGetMethods) {
        this.filterForGetMethods = filterForGetMethods;
    }

    public FunctionList.Filter getFilterForDoActions() {
        return filterForDoActions;
    }

    /**
     * Allow the underlying application to filter the doXxx actions
     */
    public void setFilterForDoActions(FunctionList.Filter filterForDoActions) {
        this.filterForDoActions = filterForDoActions;
    }

    public FieldRef.Filter getFilterForFields() {
        return filterForFields;
    }

    public void setFilterForFields(FieldRef.Filter filterForFields) {
        this.filterForFields = filterForFields;
    }

    public DispatchersFilter getDispatchersFilter() {
        return dispatchersFilter;
    }

    public void setDispatchersFilter(DispatchersFilter dispatchersFilter) {
        this.dispatchersFilter = dispatchersFilter;
    }

    public FilteredDoActionTriggerListener getFilteredDoActionTriggerListener() {
        return filteredDoActionTriggerListener;
    }

    public void setFilteredDoActionTriggerListener(FilteredDoActionTriggerListener filteredDoActionTriggerListener) {
        if (filteredDoActionTriggerListener == null) {
            this.filteredDoActionTriggerListener = FilteredDoActionTriggerListener.JUST_WARN;
        } else {
            this.filteredDoActionTriggerListener = filteredDoActionTriggerListener;
        }
    }

    public FilteredGetterTriggerListener getFilteredGetterTriggerListener() {
        return filteredGetterTriggerListener;
    }

    public void setFilteredGetterTriggerListener(FilteredGetterTriggerListener filteredGetterTriggerListener) {
        if (filteredGetterTriggerListener == null) {
            this.filteredGetterTriggerListener = FilteredGetterTriggerListener.JUST_WARN;
        } else {
            this.filteredGetterTriggerListener = filteredGetterTriggerListener;
        }
    }

    public FilteredFieldTriggerListener getFilteredFieldTriggerListener() {
        return filteredFieldTriggerListener;
    }

    public void setFilteredFieldTriggerListener(FilteredFieldTriggerListener filteredFieldTriggerListener) {
        if (filteredFieldTriggerListener == null) {
            this.filteredFieldTriggerListener = FilteredFieldTriggerListener.JUST_WARN;
        } else {
            this.filteredFieldTriggerListener = filteredFieldTriggerListener;
        }
    }

    public JsonInErrorMessageSanitizer getJsonInErrorMessageSanitizer() {
        if (jsonInErrorMessageSanitizer == null) {
            return JsonInErrorMessageSanitizer.NOOP;
        }
        return jsonInErrorMessageSanitizer;
    }

    /**
     * Allow the application to customize the way the JSON are rendered in the stack trace in case of binding exception.
     */
    public void setJsonInErrorMessageSanitizer(JsonInErrorMessageSanitizer jsonInErrorMessageSanitizer) {
        this.jsonInErrorMessageSanitizer = jsonInErrorMessageSanitizer;
    }

    public DispatchValidator getDispatchValidator() {
        if (dispatchValidator == null) {
            dispatchValidator = DispatchValidator.DEFAULT;
        }
        return dispatchValidator;
    }

    /**
     * Sets the validator used with facet dispatchers.
     */
    public void setDispatchValidator(DispatchValidator dispatchValidator) {
        this.dispatchValidator = dispatchValidator;
    }

    public FilteredDispatchTriggerListener getFilteredDispatchTriggerListener() {
        if (filteredDispatchTriggerListener == null) {
            filteredDispatchTriggerListener = FilteredDispatchTriggerListener.JUST_WARN;
        }
        return filteredDispatchTriggerListener;
    }

    /**
     * Sets the event listener used for reacting to filtered dispatch requests.
     */
    public void setFilteredDispatchTriggerListener(FilteredDispatchTriggerListener filteredDispatchTriggerListener) {
        this.filteredDispatchTriggerListener = filteredDispatchTriggerListener;
    }
}
