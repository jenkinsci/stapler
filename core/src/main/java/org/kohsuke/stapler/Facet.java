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
import org.kohsuke.stapler.lang.Klass;

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
     */
    public abstract void buildViewDispatchers(MetaClass owner, List<Dispatcher> dispatchers);

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
                
                Class<?> c;
                try {
                    c = cl.loadClass(name);
                } catch (ClassNotFoundException e) {
                    LOGGER.log(Level.WARNING, "Failed to load "+name,e);
                    continue;
                }
                try {
                    r.add((T)c.newInstance());
                } catch (InstantiationException e) {
                    LOGGER.log(Level.WARNING, "Failed to instanticate "+c,e);
                } catch (IllegalAccessException e) {
                    LOGGER.log(Level.WARNING, "Failed to instanticate "+c,e);
                }
            }
        }
        return r;
    }

    public static final Logger LOGGER = Logger.getLogger(Facet.class.getName());

    /**
     * Creates a {@link RequestDispatcher} that handles the given view, or
     * return null if no such view was found.
     *
     * @param type
     *      If "it" is non-null, {@code it.getClass()}. Otherwise the class
     *      from which the view is searched.
     */
    public RequestDispatcher createRequestDispatcher(RequestImpl request, Klass<?> type, Object it, String viewName) throws IOException {
        return null;    // should be really abstract, but not
    }

    public RequestDispatcher createRequestDispatcher(RequestImpl request, Class type, Object it, String viewName) throws IOException {
        return createRequestDispatcher(request,Klass.java(type),it,viewName);
    }

    /**
     * Attempts to route the HTTP request to the 'index' page of the 'it' object.
     *
     * @return
     *      true if the processing succeeds. Otherwise false.
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
}
