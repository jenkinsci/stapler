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

import org.apache.commons.jelly.Script;
import org.apache.commons.jelly.expression.ExpressionFactory;
import org.kohsuke.MetaInfServices;
import org.kohsuke.stapler.Dispatcher;
import org.kohsuke.stapler.Facet;
import org.kohsuke.stapler.MetaClass;
import org.kohsuke.stapler.RequestImpl;
import org.kohsuke.stapler.ResponseImpl;
import org.kohsuke.stapler.TearOffSupport;
import org.kohsuke.stapler.lang.Klass;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * {@link Facet} that adds Jelly as the view.
 * 
 * @author Kohsuke Kawaguchi
 */
@MetaInfServices(Facet.class)
public class JellyFacet extends Facet implements JellyCompatibleFacet {
    /**
     * Used to invoke Jelly script. Can be replaced to the custom object.
     */
    public volatile ScriptInvoker scriptInvoker = new DefaultScriptInvoker();

    /**
     * Used to load {@link ResourceBundle}s.
     */
    public volatile ResourceBundleFactory resourceBundleFactory = ResourceBundleFactory.INSTANCE;

    public void buildViewDispatchers(final MetaClass owner, List<Dispatcher> dispatchers) {
        dispatchers.add(new Dispatcher() {
            final JellyClassTearOff tearOff = owner.loadTearOff(JellyClassTearOff.class);

            public boolean dispatch(RequestImpl req, ResponseImpl rsp, Object node) throws IOException, ServletException {
                // check Jelly view
                String next = req.tokens.peek();
                if(next==null)  return false;

                // only match the end of the URL
                if (req.tokens.countRemainingTokens()>1)    return false;

                try {
                    Script script = tearOff.findScript(next+".jelly");

                    if(script==null)        return false;   // no Jelly script found

                    req.tokens.next();

                    if (traceable()) {
                        // Null not expected here
                        String src = next+".jelly";
                        if (script instanceof JellyViewScript) {
                            JellyViewScript jvs = (JellyViewScript) script;
                            src = jvs.getName();
                        }
                        trace(req,rsp,"-> %s on <%s>", src, node);
                    }

                    scriptInvoker.invokeScript(req, rsp, script, node);

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
                return "VIEW.jelly for url=/VIEW";
            }
        });
    }

    public Collection<Class<JellyClassTearOff>> getClassTearOffTypes() {
        return TEAROFF_TYPES;
    }

    public Collection<String> getScriptExtensions() {
        return EXTENSION;
    }

    public RequestDispatcher createRequestDispatcher(RequestImpl request, Klass<?> type, Object it, String viewName) throws IOException {
        TearOffSupport mc = request.stapler.getWebApp().getMetaClass(type);
        return mc.loadTearOff(JellyClassTearOff.class).createDispatcher(it,viewName);
    }

    public boolean handleIndexRequest(RequestImpl req, ResponseImpl rsp, Object node, MetaClass nodeMetaClass) throws IOException, ServletException {
        return nodeMetaClass.loadTearOff(JellyClassTearOff.class).serveIndexJelly(req,rsp,node);
    }

    /**
     * Sets the Jelly {@link ExpressionFactory} to be used to parse views.
     *
     * <p>
     * This method should be invoked from your implementation of
     * {@link ServletContextListener#contextInitialized(ServletContextEvent)}.
     *
     * <p>
     * Once views are parsed, they won't be re-parsed just because you called
     * this method to override the expression factory.
     *
     * <p>
     * The primary use case of this feature is to customize the behavior
     * of JEXL evaluation.
     */
    public static void setExpressionFactory( ServletContextEvent event, ExpressionFactory factory ) {
        JellyClassLoaderTearOff.EXPRESSION_FACTORY = factory;
    }

    /**
     * This flag will activate the Jelly evaluation trace.
     * It generates extra comments into HTML, indicating where the fragment was rendered.
     */
    public static boolean TRACE = Boolean.getBoolean("stapler.jelly.trace");

    private static final Set<Class<JellyClassTearOff>> TEAROFF_TYPES = Collections.singleton(JellyClassTearOff.class);

    private static final Set<String> EXTENSION = Collections.singleton(".jelly");
}
