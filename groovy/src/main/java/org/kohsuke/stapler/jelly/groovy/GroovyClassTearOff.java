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
import org.apache.commons.jelly.Script;
import org.kohsuke.stapler.AbstractTearOff;
import org.kohsuke.stapler.MetaClass;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.WebApp;
import org.kohsuke.stapler.jelly.JellyRequestDispatcher;
import org.kohsuke.stapler.jelly.JellyFacet;

import javax.servlet.ServletException;
import javax.servlet.RequestDispatcher;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Kohsuke Kawaguchi
 */
public final class GroovyClassTearOff extends AbstractTearOff<GroovyClassLoaderTearOff,GroovierJellyScript,IOException> {
    public GroovyClassTearOff(MetaClass owner) {
        super(owner,GroovyClassLoaderTearOff.class);
    }

    public GroovierJellyScript parseScript(URL res) throws IOException {
        return classLoader.parse(res);
    }

    // TODO: code duplication between JellyClassTearOff

    public boolean serveIndexGroovy(StaplerRequest req, StaplerResponse rsp, Object node) throws IOException, ServletException {
        try {
            Script script = findScript("index.groovy");
            if(script!=null) {
                if(LOGGER.isLoggable(Level.FINE))
                    LOGGER.fine("Invoking index.jelly on "+node);
                WebApp.getCurrent().getFacet(JellyFacet.class).scriptInvoker.invokeScript(req, rsp, script, node);
                return true;
            }
            return false;
        } catch (JellyException e) {
            throw new ServletException(e);
        }
    }

    /**
     * Creates a {@link RequestDispatcher} that forwards to the jelly view, if available.
     */
    public RequestDispatcher createDispatcher(Object it, String viewName) throws IOException {
        Script script = findScript(viewName+".groovy");
        if(script!=null)
            return new JellyRequestDispatcher(it,script);
        return null;
    }

    private static final Logger LOGGER = Logger.getLogger(GroovyClassTearOff.class.getName());
}
