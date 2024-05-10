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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * The stapler version of the {@link ClassLoader} object,
 * that retains some useful cache about a class loader.
 *
 * @author Kohsuke Kawaguchi
 */
public class MetaClassLoader extends TearOffSupport {
    public final MetaClassLoader parent;
    public final ClassLoader loader;

    public MetaClassLoader(ClassLoader loader) {
        this.loader = loader;
        this.parent = get(loader.getParent());
    }

    public static MetaClassLoader get(ClassLoader cl) {
        if(cl ==null)
            return null; // if no parent, delegate to the debug loader if available.
        
        return classMap.get(cl);
    }

    /**
     * If non-null, delegate to this classloader.
     */
    @SuppressFBWarnings(value = "MS_SHOULD_BE_REFACTORED_TO_BE_FINAL", justification = "Legacy switch.")
    public static MetaClassLoader debugLoader = null;

    /**
     * All {@link MetaClass}es.
     */
    private static final ClassLoaderValue<MetaClassLoader> classMap = new ClassLoaderValue<MetaClassLoader>() {
        @Override
        protected MetaClassLoader computeValue(ClassLoader cl) {
            return new MetaClassLoader(cl);
        }
    };

    static {
        debugLoader = createDebugLoader();
    }

    @SuppressFBWarnings(value = "DP_CREATE_CLASSLOADER_INSIDE_DO_PRIVILEGED", justification = "Not used with an installed security manager.")
    private static MetaClassLoader createDebugLoader() {
        try {
            String path = System.getProperty("stapler.resourcePath");
            if(path!=null) {
                String[] tokens = path.split(";");
                URL[] urls = new URL[tokens.length];
                for (int i=0; i<tokens.length; i++)
                    urls[i] = new File(tokens[i]).toURI().toURL();
                return new MetaClassLoader(new URLClassLoader(urls));
            }
        } catch (MalformedURLException e) {
            throw new Error(e);
        }
        return null;
    }
}
