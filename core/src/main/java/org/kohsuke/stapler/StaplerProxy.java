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

/**
 * If an object delegates all its UI processing to another object,
 * it can implement this interface and return the designated object
 * from the {@link #getTarget()} method.
 *
 * <p>
 * Compared to {@link StaplerFallback}, stapler handles this interface at the very beginning,
 * whereas {@link StaplerFallback} is handled at the very end.
 *
 * <p>
 * By returning {@code this} from the {@link #getTarget()} method,
 * {@link StaplerProxy} can be also used just as an interception hook (for example
 * to perform authorization.) 
 *
 * @author Kohsuke Kawaguchi
 * @see StaplerFallback
 */
public interface StaplerProxy {
    /**
     * Returns the object that is responsible for processing web requests.
     *
     * @return
     *      If null is returned, it generates 404.
     *      If {@code this} object is returned, no further
     *      {@link StaplerProxy} look-up is done and {@code this} object
     *      processes the request.
     */
    Object getTarget();
}
