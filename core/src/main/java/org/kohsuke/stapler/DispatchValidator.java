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

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Validates dispatch requests. This validator is configured through {@link WebApp#setDispatchValidator(DispatchValidator)}
 * and is automatically used by dispatchers created through {@link Facet#createValidatingDispatcher(AbstractTearOff, ScriptExecutor)}.
 * Extends {@linkplain Facet#createValidatingDispatcher(AbstractTearOff, ScriptExecutor) facet dispatchers} to provide validation
 * of views before they dispatch, thus allowing a final veto before dispatch begins writing any response body to the
 * client.
 *
 * @see WebApp#setDispatchValidator(DispatchValidator)
 * @since TODO
 */
public interface DispatchValidator {

    /**
     * Checks if the given request and response should be allowed to dispatch. Returns {@code null} to indicate an
     * unknown or neutral result.
     *
     * @param req the HTTP request to validate
     * @param rsp the HTTP response
     * @return true if the request should be dispatched, false if not, or null if unknown or neutral
     */
    @CheckForNull Boolean isDispatchAllowed(@Nonnull StaplerRequest req, @Nonnull StaplerResponse rsp);

    /**
     * Checks if the given request and response should be allowed to dispatch a view on an optionally present node
     * object. Returns {@code null} to indicate an unknown or neutral result.
     *
     * @param req      the HTTP request to validate
     * @param rsp      the HTTP response
     * @param viewName the name of the view to dispatch
     * @param node     the node being dispatched if present
     * @return true if the view should be allowed to dispatch, false if it should not, or null if unknown
     */
    default @CheckForNull Boolean isDispatchAllowed(@Nonnull StaplerRequest req, @Nonnull StaplerResponse rsp, @Nonnull String viewName, @CheckForNull Object node) {
        return isDispatchAllowed(req, rsp);
    }

    /**
     * Allows the given request to be dispatched. Further calls to {@link #isDispatchAllowed(StaplerRequest, StaplerResponse)}
     * should return true for the same request.
     */
    void allowDispatch(@Nonnull StaplerRequest req, @Nonnull StaplerResponse rsp);

    /**
     * Throws a {@link CancelRequestHandlingException} if the given request is not
     * {@linkplain #isDispatchAllowed(StaplerRequest, StaplerResponse) allowed}.
     */
    default void requireDispatchAllowed(@Nonnull StaplerRequest req, @Nonnull StaplerResponse rsp) throws CancelRequestHandlingException {
        Boolean allowed = isDispatchAllowed(req, rsp);
        if (allowed == null || !allowed) {
            throw new CancelRequestHandlingException();
        }
    }

    /**
     * Default validator implementation that explicitly allows all dispatch requests to proceed.
     */
    DispatchValidator DEFAULT = new DispatchValidator() {
        @Override
        public Boolean isDispatchAllowed(@Nonnull StaplerRequest req, @Nonnull StaplerResponse rsp) {
            return true;
        }

        @Override
        public void allowDispatch(@Nonnull StaplerRequest req, @Nonnull StaplerResponse rsp) {
            // no-op
        }
    };
}
