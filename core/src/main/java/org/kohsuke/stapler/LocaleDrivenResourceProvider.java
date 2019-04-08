/*
 * Copyright (c) 2019 Daniel Beck
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

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service provider interface allowing to hook into webapp resource lookup.
 *
 * This cannot be made a property of WebApp as other behavior customizations, as webapp resource lookup is done before we have StaplerRequest/StaplerResponse.
 */
public abstract class LocaleDrivenResourceProvider {
    /**
     * Returns the URL corresponding to the specified resource path.
     *
     * The path can take two forms:
     *
     * <ul>
     *     <li>A full URL represented as string. In Jenkins this is typically plugin webapp resources.</li>
     *     <li>A path with leading slash. These are actual webapp resources, with that being the implicit base directory.</li>
     * </ul>
     *
     * @param path the path to the resource
     * @return the URL to the file, if it is to be overridden, null otherwise.
     */
    @CheckForNull
    abstract public URL lookup(@Nonnull String path);

    private static List<LocaleDrivenResourceProvider> localeDrivenResourceProviders;

    private static final Logger LOGGER = Logger.getLogger(LocaleDrivenResourceProvider.class.getName());

    private static synchronized List<LocaleDrivenResourceProvider> getLocaleDrivenResourceProviders() {
        if (localeDrivenResourceProviders == null) {
            localeDrivenResourceProviders = new ArrayList<>();
            for (LocaleDrivenResourceProvider provider : ServiceLoader.load(LocaleDrivenResourceProvider.class)) {
                localeDrivenResourceProviders.add(provider);
                LOGGER.log(Level.INFO, "Registered LocaleDrivenResourceProvider: " + provider);
            }
        }
        return localeDrivenResourceProviders;
    }

    /* package */ static URL lookupResource(String path) {
        for (LocaleDrivenResourceProvider provider : getLocaleDrivenResourceProviders()) {
            try {
                URL url = provider.lookup(path);
                if (url != null) {
                    return url;
                }
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Failed to look up URL for " + path + " from " + provider, e);
            }
        }
        return null;
    }
}
