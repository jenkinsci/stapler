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

import java.util.Collection;

/**
 * A web-bound object can implement this interface to allow designated objects to selectively override
 * URL mappings.
 *
 * <p>
 * If the designated override objects does not have a handler for the request,
 * the host object (the one that's implementing {@link StaplerOverridable}) will handle the request.
 *
 * <p>
 * In override objects, {@link CancelRequestHandlingException} is an useful mechanism to programmatically
 * inspect a request and then selecctively return the request back to the original object (that implements
 * {@link StaplerOverridable}.
 *
 * @author Kohsuke Kawaguchi
 * @see CancelRequestHandlingException
 */
public interface StaplerOverridable {
    /**
     * Returns a list of overridables.
     * Override objects are tried in their iteration order.
     *
     * @return can be null, which is treated as the same thing as returning an empty collection.
     */
    Collection<?> getOverrides();
}
