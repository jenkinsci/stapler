/*
 * The MIT License
 *
 * Copyright (c) 2019 CloudBees, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.kohsuke.stapler;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implements a RequestDispatcher for a given model node and view. Unlike dispatchers created through
 * {@link Facet#createValidatingDispatcher(AbstractTearOff, ScriptExecutor)}, these dispatchers always allow scripts
 * to be dispatched.
 * @param <S> script view type
 * @since TODO
 */
class ScriptRequestDispatcher<S> implements RequestDispatcher {

    private static final Logger LOGGER = Logger.getLogger(ScriptRequestDispatcher.class.getName());

    @CheckForNull static <S> ScriptRequestDispatcher<S> newRequestDispatcher(@Nonnull AbstractTearOff<?, ? extends S, ?> scriptLoader,
                                                                             @Nonnull ScriptExecutor<? super S> scriptExecutor,
                                                                             @Nonnull String viewName,
                                                                             @CheckForNull Object node) {
        S script;
        try {
            script = scriptLoader.findScript(viewName);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, e, () -> "Could not load requested view " + viewName + " on model class " + (node == null ? null : node.getClass().getName()));
            return null;
        }
        if (script == null) {
            return null;
        }
        return new ScriptRequestDispatcher<>(scriptLoader.getDefaultScriptExtension(), scriptExecutor, viewName, script, node);
    }

    private final @Nonnull String defaultScriptExtension;
    private final @Nonnull ScriptExecutor<? super S> scriptExecutor;
    private final @Nonnull String viewName;
    private final @Nonnull S script;
    private final @CheckForNull Object node;

    private ScriptRequestDispatcher(@Nonnull String defaultScriptExtension,
                                    @Nonnull ScriptExecutor<? super S> scriptExecutor,
                                    @Nonnull String viewName,
                                    @Nonnull S script,
                                    @CheckForNull Object node) {
        this.defaultScriptExtension = defaultScriptExtension;
        this.scriptExecutor = scriptExecutor;
        this.viewName = viewName;
        this.script = script;
        this.node = node;
    }


    @Override
    public void forward(ServletRequest request, ServletResponse response) throws ServletException, IOException {
        StaplerRequest req = (StaplerRequest) request;
        StaplerResponse rsp = (StaplerResponse) response;
        DispatchValidator validator = req.getWebApp().getDispatchValidator();
        validator.allowDispatch(req, rsp);
        try {
            Dispatcher.anonymizedTraceEval(req, rsp, node, "%s: View: %s%s", viewName, defaultScriptExtension);
            if (Dispatcher.traceable()) {
                Dispatcher.trace(req, rsp, "-> %s on <%s>", viewName, node);
            }
            scriptExecutor.execute(req, rsp, script, node);
        } catch (ServletException | IOException | RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new ServletException(e);
        }

    }

    @Override
    @SuppressFBWarnings(value = "REQUESTDISPATCHER_FILE_DISCLOSURE", justification = "Forwarding the request to be handled correctly.")
    public void include(ServletRequest request, ServletResponse response) throws ServletException, IOException {
        forward(request, response);
    }
}
