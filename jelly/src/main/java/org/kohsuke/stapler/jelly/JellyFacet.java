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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.jelly.JellyException;
import org.apache.commons.jelly.expression.ExpressionFactory;
import org.kohsuke.MetaInfServices;
import org.kohsuke.stapler.Dispatcher;
import org.kohsuke.stapler.Facet;
import org.kohsuke.stapler.MetaClass;
import org.kohsuke.stapler.RequestImpl;
import org.kohsuke.stapler.ResponseImpl;
import org.kohsuke.stapler.lang.Klass;

import edu.umd.cs.findbugs.annotations.NonNull;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

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

    @Override
    public void buildViewDispatchers(final MetaClass owner, List<Dispatcher> dispatchers) {
        dispatchers.add(createValidatingDispatcher(owner.loadTearOff(JellyClassTearOff.class), scriptInvoker));
    }

    @Override
    public void buildIndexDispatchers(MetaClass owner, List<Dispatcher> dispatchers) {
        try {
            if (owner.loadTearOff(JellyClassTearOff.class).findScript("index.jelly")!=null) {
                super.buildIndexDispatchers(owner, dispatchers);
            }
        } catch (JellyException e) {
            LOGGER.log(Level.WARNING, "Failed to parse index.jelly for "+owner, e);
        }
    }

    @Override 
    protected @NonNull String getExtensionSuffix() {
        return ".jelly";
    }

    @Override
    public Collection<Class<JellyClassTearOff>> getClassTearOffTypes() {
        return TEAROFF_TYPES;
    }

    @Override
    public Collection<String> getScriptExtensions() {
        return EXTENSION;
    }

    @Override
    public RequestDispatcher createRequestDispatcher(RequestImpl request, Klass<?> type, Object it, String viewName) throws IOException {
        JellyClassTearOff scriptLoader = request.getWebApp().getMetaClass(type).loadTearOff(JellyClassTearOff.class);
        return createRequestDispatcher(scriptLoader, scriptInvoker, it, viewName);
    }

    @Override
    public boolean handleIndexRequest(RequestImpl req, ResponseImpl rsp, Object node, MetaClass nodeMetaClass) throws IOException, ServletException {
        return handleIndexRequest(nodeMetaClass.loadTearOff(JellyClassTearOff.class), scriptInvoker, req, rsp, node);
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
    @SuppressFBWarnings(value = "MS_SHOULD_BE_FINAL", justification = "Legacy switch.")
    public static boolean TRACE = Boolean.getBoolean("stapler.jelly.trace");

    private static final Set<Class<JellyClassTearOff>> TEAROFF_TYPES = Collections.singleton(JellyClassTearOff.class);

    private static final Set<String> EXTENSION = Collections.singleton(".jelly");
}
