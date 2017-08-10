/*
 * Copyright (c) 2017, Stephen Connolly, CloudBees, Inc.
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

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.servlet.ServletException;
import org.kohsuke.stapler.annotations.StaplerFacet;

/**
 * Wraps a {@link Facet} dispatcher with a guard to ensure that only {@link StaplerFacet} approved facets are
 * dispatched.
 *
 * @since TODO.
 */
public class StaplerFacetDispatcher extends Dispatcher {
    /**
     * The {@link Dispatcher} that we are guarding.
     */
    @Nonnull
    private final Dispatcher delegate;
    /**
     * The approved facet names.
     */
    @Nonnull
    private final Set<String> names;

    /**
     * Constructor.
     *
     * @param delegate the delegate.
     * @param names    the approved names.
     */
    private StaplerFacetDispatcher(@Nonnull Dispatcher delegate, @Nonnull Set<String> names) {
        this.delegate = delegate;
        this.names = names;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean dispatch(RequestImpl req, ResponseImpl rsp, Object node)
            throws IOException, ServletException, IllegalAccessException, InvocationTargetException {
        // check Jelly view
        String next = req.tokens.peek();
        if (next == null) {
            return false;
        }
        // only allow the guarded views
        if (!names.contains(next)) {
            return false;
        }
        return delegate.dispatch(req, rsp, node);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return delegate.toString();
    }

    /**
     * Guards the supplied {@link Dispatcher} using the supplied {@link StaplerFacet} details.
     *
     * @param dispatcher the {@link Dispatcher} to guard.
     * @param facets     the allowed {@link StaplerFacet}s.
     * @return a guarded {@link Dispatcher}.
     */
    @Nonnull
    public static Dispatcher guard(@Nonnull Dispatcher dispatcher, @Nonnull List<StaplerFacet> facets) {
        Set<String> names = new HashSet<>();
        for (StaplerFacet facet : facets) {
            names.add(facet.value());
        }
        return new StaplerFacetDispatcher(dispatcher, names);
    }

    /**
     * Guards the supplied {@link Dispatcher}s using the supplied {@link StaplerFacet} details.
     *
     * @param dispatchers the {@link Dispatcher}s to guard.
     * @param facets      the allowed {@link StaplerFacet}s.
     * @return a guarded {@link Dispatcher}.
     */
    @Nonnull
    public static List<Dispatcher> guard(@Nonnull List<Dispatcher> dispatchers, @Nonnull List<StaplerFacet> facets) {
        if (dispatchers.isEmpty()) {
            return Collections.emptyList();
        }
        List<Dispatcher> wrapped = new ArrayList<>(dispatchers.size());
        Set<String> names = new HashSet<>();
        for (StaplerFacet facet : facets) {
            names.add(facet.value());
        }
        for (Dispatcher dispatcher : dispatchers) {
            wrapped.add(new StaplerFacetDispatcher(dispatcher, names));
        }
        return wrapped;
    }
}
