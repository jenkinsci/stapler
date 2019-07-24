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

import org.apache.commons.discovery.ResourceNameIterator;
import org.apache.commons.discovery.resource.ClassLoaders;
import org.apache.commons.discovery.resource.names.DiscoverServiceNames;
import org.kohsuke.MetaInfServices;
import org.kohsuke.stapler.event.FilteredDispatchTriggerListener;
import org.kohsuke.stapler.lang.Klass;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Aspect of stapler that brings in an optional language binding.
 *
 * Put {@link MetaInfServices} on subtypes so that Stapler can discover them.
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class Facet {
    /**
     * Adds {@link Dispatcher}s that look at one token and binds that
     * to the views associated with the 'it' object.
     * @see #createValidatingDispatcher(AbstractTearOff, ScriptExecutor)
     */
    public abstract void buildViewDispatchers(MetaClass owner, List<Dispatcher> dispatchers);

    /**
     * Adds {@link Dispatcher}s that serves the likes of {@code index.EXT}
     *
     * The default implementation invokes {@link #handleIndexRequest(RequestImpl, ResponseImpl, Object, MetaClass)}
     * but facet implementations can improve runtime dispatch performance by testing the presence
     * of index view page upfront.
     */
    public void buildIndexDispatchers(MetaClass owner, List<Dispatcher> dispatchers) {
        dispatchers.add(new IndexViewDispatcher(owner,this));
    }

    /**
     * Adds {@link Dispatcher}s that do catch-all behaviours like "doDispatch" does.
     */
    public void buildFallbackDispatchers(MetaClass owner, List<Dispatcher> dispatchers) {}

    /**
     * Discovers all the facets in the classloader.
     */
    public static List<Facet> discover(ClassLoader cl) {
        return discoverExtensions(Facet.class, cl);
    }

    public static <T> List<T> discoverExtensions(Class<T> type, ClassLoader... cls) {
        List<T> r = new ArrayList<T>();
        Set<String> classNames = new HashSet<String>();

        for (ClassLoader cl : cls) {
            ClassLoaders classLoaders = new ClassLoaders();
            classLoaders.put(cl);
            DiscoverServiceNames dc = new DiscoverServiceNames(classLoaders);
            ResourceNameIterator itr = dc.findResourceNames(type.getName());
            while(itr.hasNext()) {
                String name = itr.nextResourceName();
                if (!classNames.add(name))  continue;   // avoid duplication
                
                Class<? extends T> c;
                try {
                    c = cl.loadClass(name).asSubclass(type);
                } catch (ClassNotFoundException e) {
                    LOGGER.log(Level.WARNING, "Failed to load "+name,e);
                    continue;
                }
                try {
                    r.add(c.newInstance());
                } catch (InstantiationException | IllegalAccessException e) {
                    LOGGER.log(Level.WARNING, "Failed to instantiate "+c,e);
                }
            }
        }
        return r;
    }

    public static final Logger LOGGER = Logger.getLogger(Facet.class.getName());

    public static boolean ALLOW_VIEW_NAME_PATH_TRAVERSAL = Boolean.getBoolean(Facet.class.getName() + ".allowViewNamePathTraversal");
    
    /**
     * Creates a {@link RequestDispatcher} that handles the given view, or
     * return null if no such view was found.
     *
     * @param type
     *      If "it" is non-null, {@code it.getClass()}. Otherwise the class
     *      from which the view is searched.
     * @see #createRequestDispatcher(AbstractTearOff, ScriptExecutor, Object, String)
     */
    @CheckForNull public RequestDispatcher createRequestDispatcher(RequestImpl request, Klass<?> type, Object it, String viewName) throws IOException {
        return null;    // should be really abstract, but not
    }

    @CheckForNull public RequestDispatcher createRequestDispatcher(RequestImpl request, Class type, Object it, String viewName) throws IOException {
        return createRequestDispatcher(request,Klass.java(type),it,viewName);
    }

    /**
     * Attempts to route the HTTP request to the 'index' page of the 'it' object.
     *
     * @return
     *      true if the processing succeeds. Otherwise false.
     * @see #handleIndexRequest(AbstractTearOff, ScriptExecutor, RequestImpl, ResponseImpl, Object)
     */
    public abstract boolean handleIndexRequest(RequestImpl req, ResponseImpl rsp, Object node, MetaClass nodeMetaClass) throws IOException, ServletException;

    /**
     * Maps an instance to a {@link Klass}. This is the generalization of {@code o.getClass()}.
     * 
     * This is for those languages that use something other than {@link Class} to represent the concept of a class.
     * Those facets that are fine with {@code o.getClass()} should return null so that it gives other facets a chance
     * to map it better.
     */
    public Klass<?> getKlass(Object o) {
        return null;
    }
    
    /**
     * Ensure the path that is passed is only the name of the file and not a path
     */
    protected boolean isBasename(String potentialPath){
        if (ALLOW_VIEW_NAME_PATH_TRAVERSAL) {
            return true;
        } else {
            if (potentialPath.contains("\\") || potentialPath.contains("/")) {
                // prevent absolute path and folder traversal to find scripts
                return false;
            }
            
            return true;
        }
    }

    /**
     * For Facets that require a particular file extension to be put in any case.
     * Just return an empty String if the Facet does not want to have such behavior.
     * 
     * If you do want to have an extension added, you must ensure you provide the dot at the first character position, 
     * see JellyFacet
     */
    protected @Nonnull String getExtensionSuffix() {
        return "";
    } 

    /**
     * Creates a Dispatcher that integrates {@link DispatchValidator} with the provided script loader and executor.
     * If an exception or one of its causes is a {@link CancelRequestHandlingException}, this will cause the
     * Dispatcher to cancel and return false, thus allowing for further dispatchers to attempt to handle the request.
     * This also requires validation to pass before any output can be written to the response.
     * In any error case, the configured {@link FilteredDispatchTriggerListener} will be notified.
     *
     * @param scriptLoader   tear-off script loader to find views
     * @param scriptExecutor script executor for rendering views
     * @param <S>            type of script
     * @return dispatcher that handles scripts
     * @see WebApp#setDispatchValidator(DispatchValidator)
     * @see WebApp#setFilteredDispatchTriggerListener(FilteredDispatchTriggerListener)
     * @since TODO
     */
    @Nonnull protected <S> Dispatcher createValidatingDispatcher(@Nonnull AbstractTearOff<?, ? extends S, ?> scriptLoader,
                                                                 @Nonnull ScriptExecutor<? super S> scriptExecutor) {
        return new Dispatcher() {
            @Override
            public boolean dispatch(@Nonnull RequestImpl req, @Nonnull ResponseImpl rsp, @CheckForNull Object node) throws ServletException {
                String next = req.tokens.peek();
                if (next == null) {
                    return false;
                }
                // only match end of URL
                if (req.tokens.countRemainingTokens() > 1) {
                    return false;
                }
                // avoid serving both foo and foo/ as they have different URL semantics
                if (req.tokens.endsWithSlash) {
                    return false;
                }
                // prevent potential path traversal
                if (!isBasename(next)) {
                    return false;
                }
                DispatchValidator validator = req.getWebApp().getDispatchValidator();
                FilteredDispatchTriggerListener listener = req.getWebApp().getFilteredDispatchTriggerListener();
                Boolean valid = validator.isDispatchAllowed(req, rsp, next, node);
                if (valid != null && !valid) {
                    return listener.onDispatchTrigger(req, rsp, node, next);
                }
                S script;
                try {
                    script = scriptLoader.findScript(next + getExtensionSuffix());
                } catch (Exception e) {
                    throw new ServletException(e);
                }
                if (script == null) {
                    return false;
                }
                req.tokens.next();
                anonymizedTraceEval(req, rsp, node, "%s: View: %s%s", next, scriptLoader.getDefaultScriptExtension());
                if (traceable()) {
                    trace(req, rsp, "-> %s on <%s>", next, node);
                }
                try {
                    scriptExecutor.execute(req, rsp, script, node);
                    return true;
                } catch (Exception e) {
                    req.tokens.prev();
                    for (Throwable cause = e; cause != null; cause = cause.getCause()) {
                        if (cause instanceof CancelRequestHandlingException) {
                            return listener.onDispatchTrigger(req, rsp, node, next);
                        }
                    }
                    throw new ServletException(e);
                }
            }

            @Override
            public String toString() {
                return "VIEW" + scriptLoader.getDefaultScriptExtension() + " for url=/VIEW";
            }
        };
    }

    /**
     * Handles an index request by dispatching a script.
     * @since TODO
     */
    protected <S> boolean handleIndexRequest(@Nonnull AbstractTearOff<?, ? extends S, ?> scriptLoader,
                                             @Nonnull ScriptExecutor<? super S> scriptExecutor,
                                             @Nonnull RequestImpl req,
                                             @Nonnull ResponseImpl rsp,
                                             @CheckForNull Object node)
            throws ServletException, IOException {
        S script;
        try {
            script = scriptLoader.findScript("index");
        } catch (Exception e) {
            throw new ServletException(e);
        }
        if (script == null) {
            return false;
        }
        Dispatcher.anonymizedTraceEval(req, rsp, node, "Index: index%s", scriptLoader.getDefaultScriptExtension());
        if (Dispatcher.traceable()) {
            Dispatcher.trace(req, rsp, "-> index on <%s>", node);
        }
        try {
            scriptExecutor.execute(req, rsp, script, node);
            return true;
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    /**
     * Creates a RequestDispatcher that integrates with {@link DispatchValidator} and
     * {@link FilteredDispatchTriggerListener}.
     *
     * @param scriptLoader   tear-off script loader for finding views
     * @param scriptExecutor script executor for rendering views
     * @param it             the model node being dispatched against
     * @param viewName       name of the view to load and execute
     * @param <S>            view type
     * @return a RequestDispatcher that performs similar validation to {@link #createValidatingDispatcher(AbstractTearOff, ScriptExecutor)}
     * @see WebApp#setDispatchValidator(DispatchValidator)
     * @see WebApp#setFilteredDispatchTriggerListener(FilteredDispatchTriggerListener)
     * @since TODO
     */
    @CheckForNull protected <S> RequestDispatcher createRequestDispatcher(@Nonnull AbstractTearOff<?, ? extends S, ?> scriptLoader,
                                                                          @Nonnull ScriptExecutor<? super S> scriptExecutor,
                                                                          @CheckForNull Object it,
                                                                          @Nonnull String viewName) {
        return ScriptRequestDispatcher.newRequestDispatcher(scriptLoader, scriptExecutor, viewName, it);
    }
}
