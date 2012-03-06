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

import org.apache.commons.jelly.Script;
import org.kohsuke.stapler.AbstractTearOff;
import org.kohsuke.stapler.Dispatcher;
import org.kohsuke.stapler.Facet;
import org.kohsuke.stapler.MetaClass;
import org.kohsuke.stapler.RequestImpl;
import org.kohsuke.stapler.ResponseImpl;
import org.kohsuke.stapler.TearOffSupport;
import org.kohsuke.stapler.WebApp;
import org.kohsuke.stapler.jelly.JellyCompatibleFacet;
import org.kohsuke.stapler.jelly.JellyFacet;
import org.kohsuke.MetaInfServices;
import org.kohsuke.stapler.lang.Klass;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * {@link Facet} that brings in Groovy support on top of Jelly.
 * 
 * @author Kohsuke Kawaguchi
 */
@MetaInfServices(Facet.class)
public class GroovyFacet extends Facet implements JellyCompatibleFacet {
    public void buildViewDispatchers(final MetaClass owner, List<Dispatcher> dispatchers) {
        dispatchers.add(new Dispatcher() {
            final GroovyClassTearOff tearOff = owner.loadTearOff(GroovyClassTearOff.class);

            public boolean dispatch(RequestImpl req, ResponseImpl rsp, Object node) throws IOException, ServletException {
                // check Groovy view
                String next = req.tokens.peek();
                if(next==null)  return false;

                // only match the end of the URL
                if (req.tokens.countRemainingTokens()>1)    return false;
                // and avoid serving both "foo" and "foo/" as relative URL semantics are drastically different
                if (req.getRequestURI().endsWith("/"))      return false;

                try {
                    Script script = tearOff.findScript(next+".groovy");
                    if(script==null)        return false;   // no Groovy script found

                    req.tokens.next();

                    if(traceable())
                        trace(req,rsp,"Invoking "+next+".groovy"+" on "+node+" for "+req.tokens);

                    WebApp.getCurrent().getFacet(JellyFacet.class).scriptInvoker.invokeScript(req, rsp, script, node);

                    return true;
                } catch (RuntimeException e) {
                    throw e;
                } catch (IOException e) {
                    throw e;
                } catch (Exception e) {
                    throw new ServletException(e);
                }
            }
            public String toString() {
                return "TOKEN.groovy for url=/TOKEN/...";
            }
        });
    }

    public Collection<Class<GroovyClassTearOff>> getClassTearOffTypes() {
        return TEAROFF_TYPES;
    }

    public Collection<String> getScriptExtensions() {
        return EXTENSION;
    }

    public RequestDispatcher createRequestDispatcher(RequestImpl request, Klass type, Object it, String viewName) throws IOException {
        TearOffSupport mc = request.stapler.getWebApp().getMetaClass(type);
        return mc.loadTearOff(GroovyClassTearOff.class).createDispatcher(it,viewName);
    }

    public boolean handleIndexRequest(RequestImpl req, ResponseImpl rsp, Object node, MetaClass nodeMetaClass) throws IOException, ServletException {
        return nodeMetaClass.loadTearOff(GroovyClassTearOff.class).serveIndexGroovy(req, rsp, node);
    }

    private static final Set<Class<GroovyClassTearOff>> TEAROFF_TYPES = Collections.singleton(GroovyClassTearOff.class);

    private static final Set<String> EXTENSION = Collections.singleton(".groovy");
}
