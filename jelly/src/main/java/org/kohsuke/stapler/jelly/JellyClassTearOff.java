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

package org.kohsuke.stapler.jelly;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Logger;
import org.apache.commons.jelly.JellyException;
import org.apache.commons.jelly.Script;
import org.kohsuke.stapler.AbstractTearOff;
import org.kohsuke.stapler.Dispatcher;
import org.kohsuke.stapler.Facet;
import org.kohsuke.stapler.MetaClass;
import org.kohsuke.stapler.StaplerRequest2;
import org.kohsuke.stapler.StaplerResponse2;

/**
 * @author Kohsuke Kawaguchi
 */
public class JellyClassTearOff extends AbstractTearOff<JellyClassLoaderTearOff, Script, JellyException> {
    private JellyFacet facet;

    public JellyClassTearOff(MetaClass owner) {
        super(owner, JellyClassLoaderTearOff.class);
        facet = owner.webApp.getFacet(JellyFacet.class);
    }

    @Override
    protected Script parseScript(URL res) throws JellyException {
        return new JellyViewScript(owner.klass, res, classLoader.createContext().compileScript(res));
    }

    @Override
    protected String getDefaultScriptExtension() {
        return ".jelly";
    }

    @Override
    protected boolean hasAllowedExtension(String name) {
        return name.endsWith(".jelly") || name.endsWith(".jellytag");
    }

    /**
     * Aside from looking into our own, consult other facets that can handle Jelly-compatible scripts.
     */
    @Override
    public Script resolveScript(String name) throws JellyException {
        // cut off the extension so that we can search all the extensions
        String shortName;
        int dot = name.lastIndexOf('.');
        if (dot > name.lastIndexOf('/')) {
            shortName = name.substring(0, dot);
        } else {
            shortName = name;
        }

        for (Facet f : owner.webApp.facets) {
            if (f instanceof JellyCompatibleFacet jcf && !(f instanceof JellyFacet)) {
                for (Class<? extends AbstractTearOff<?, ? extends Script, ?>> ct : jcf.getClassTearOffTypes()) {
                    try {
                        Script s = owner.loadTearOff(ct).resolveScript(shortName);
                        if (s != null) {
                            return s;
                        }
                    } catch (Exception e) {
                        throw new JellyException("Failed to load " + shortName + " from " + jcf, e);
                    }
                }
            }
        }

        return super.resolveScript(name);
    }

    /**
     * Serves {@code index.jelly} if it's available, and returns true.
     *
     * @deprecated removed without replacement
     */
    @Deprecated
    public boolean serveIndexJelly(StaplerRequest2 req, StaplerResponse2 rsp, Object node)
            throws ServletException, IOException {
        try {
            Script script = findScript("index.jelly");
            if (script != null) {
                String src = "index.jelly";
                if (script instanceof JellyViewScript jvs) {
                    src = jvs.getName();
                }
                Dispatcher.anonymizedTraceEval(req, rsp, node, "%s: Jelly index: %s", src);
                if (Dispatcher.traceable()) {
                    Dispatcher.trace(req, rsp, "-> %s on <%s>", src, node);
                }
                facet.scriptInvoker.invokeScript(req, rsp, script, node);
                return true;
            }
            return false;
        } catch (JellyException e) {
            throw new ServletException(e);
        }
    }

    /**
     * Creates a {@link RequestDispatcher} that forwards to the jelly view, if available.
     *
     * @deprecated removed without replacement
     */
    @Deprecated
    public RequestDispatcher createDispatcher(Object it, String viewName) throws IOException {
        try {
            // backward compatible behavior that expects full file name including ".jelly"
            Script script = findScript(viewName);
            if (script != null) {
                return new JellyRequestDispatcher(it, script);
            }

            // this is what the look up was really supposed to be.
            script = findScript(viewName + ".jelly");
            if (script != null) {
                return new JellyRequestDispatcher(it, script);
            }
            return null;
        } catch (JellyException e) {
            throw new IOException(e.getMessage(), e);
        }
    }

    private static final Logger LOGGER = Logger.getLogger(JellyClassTearOff.class.getName());
}
