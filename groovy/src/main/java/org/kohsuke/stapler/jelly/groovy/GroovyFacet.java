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

package org.kohsuke.stapler.jelly.groovy;

import org.apache.commons.jelly.JellyException;
import org.kohsuke.MetaInfServices;
import org.kohsuke.stapler.Dispatcher;
import org.kohsuke.stapler.Facet;
import org.kohsuke.stapler.MetaClass;
import org.kohsuke.stapler.RequestImpl;
import org.kohsuke.stapler.ResponseImpl;
import org.kohsuke.stapler.jelly.JellyClassTearOff;
import org.kohsuke.stapler.jelly.JellyCompatibleFacet;
import org.kohsuke.stapler.jelly.JellyFacet;
import org.kohsuke.stapler.jelly.ScriptInvoker;
import org.kohsuke.stapler.lang.Klass;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

/**
 * {@link Facet} that brings in Groovy support on top of Jelly.
 * 
 * @author Kohsuke Kawaguchi
 */
@MetaInfServices(Facet.class)
public class GroovyFacet extends Facet implements JellyCompatibleFacet {

    @Override
    public void buildViewDispatchers(final MetaClass owner, List<Dispatcher> dispatchers) {
        ScriptInvoker scriptInvoker = owner.webApp.getFacet(JellyFacet.class).scriptInvoker;
        dispatchers.add(createValidatingDispatcher(owner.loadTearOff(GroovyClassTearOff.class), scriptInvoker));
        dispatchers.add(createValidatingDispatcher(owner.loadTearOff(GroovyServerPageTearOff.class), scriptInvoker));
    }

    @Override
    public Collection<Class<GroovyClassTearOff>> getClassTearOffTypes() {
        return TEAROFF_TYPES;
    }

    @Override
    public Collection<String> getScriptExtensions() {
        return EXTENSION;
    }

    @Override
    public RequestDispatcher createRequestDispatcher(RequestImpl request, Klass type, Object it, String viewName) throws IOException {
        MetaClass owner = request.getWebApp().getMetaClass(type);
        ScriptInvoker scriptExecutor = request.getWebApp().getFacet(JellyFacet.class).scriptInvoker;
        RequestDispatcher d = createRequestDispatcher(owner.loadTearOff(GroovyClassTearOff.class), scriptExecutor, it, viewName);
        if (d == null) {
            d = createRequestDispatcher(owner.loadTearOff(GroovyServerPageTearOff.class), scriptExecutor, it, viewName);
        }
        return d;
    }

    @Override
    public void buildIndexDispatchers(MetaClass owner, List<Dispatcher> dispatchers) {
        try {
            if (owner.loadTearOff(JellyClassTearOff.class).findScript("index")!=null) {
                super.buildIndexDispatchers(owner, dispatchers);
            }
        } catch (JellyException e) {
            LOGGER.log(Level.WARNING, "Failed to parse index.groovy for "+owner, e);
        }
    }

    @Override
    public boolean handleIndexRequest(RequestImpl req, ResponseImpl rsp, Object node, MetaClass nodeMetaClass) throws IOException, ServletException {
        ScriptInvoker scriptExecutor = req.getWebApp().getFacet(JellyFacet.class).scriptInvoker;
        return handleIndexRequest(nodeMetaClass.loadTearOff(GroovyClassTearOff.class), scriptExecutor, req, rsp, node) ||
                handleIndexRequest(nodeMetaClass.loadTearOff(GroovyServerPageTearOff.class), scriptExecutor, req, rsp, node);
    }

    private static final Set<Class<GroovyClassTearOff>> TEAROFF_TYPES = Collections.singleton(GroovyClassTearOff.class);

    private static final Set<String> EXTENSION = Collections.singleton(".groovy");
}
