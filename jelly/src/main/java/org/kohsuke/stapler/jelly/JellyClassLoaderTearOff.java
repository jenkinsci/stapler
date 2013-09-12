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

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.apache.commons.jelly.JellyContext;
import org.apache.commons.jelly.JellyException;
import org.apache.commons.jelly.TagLibrary;
import org.apache.commons.jelly.expression.ExpressionFactory;
import org.apache.commons.jelly.expression.jexl.JexlExpressionFactory;
import org.kohsuke.stapler.MetaClassLoader;

import java.lang.ref.WeakReference;
import java.net.URL;

/**
 * {@link MetaClassLoader} tear-off for Jelly support.
 *
 * @author Kohsuke Kawaguchi
 */
public class JellyClassLoaderTearOff {
    private final MetaClassLoader owner;

    /**
     * See {@link JellyClassTearOff#scripts} for why we use {@link WeakReference} here.
     */
    private volatile WeakReference<LoadingCache<String,TagLibrary>> taglibs;

    public static ExpressionFactory EXPRESSION_FACTORY = new JexlExpressionFactory();

    public JellyClassLoaderTearOff(MetaClassLoader owner) {
        this.owner = owner;
    }

    public TagLibrary getTagLibrary(String nsUri) {
        LoadingCache<String,TagLibrary> m=null;
        if(taglibs!=null)
            m = taglibs.get();
        if(m==null) {
            m = CacheBuilder.newBuilder().build(new CacheLoader<String,TagLibrary>() {
                public TagLibrary load(String nsUri) {
                    if(owner.parent!=null) {
                        // parent first
                        TagLibrary tl = owner.parent.loadTearOff(JellyClassLoaderTearOff.class).getTagLibrary(nsUri);
                        if(tl!=null)    return tl;
                    }

                    String taglibBasePath = trimHeadSlash(nsUri);
                    try {
                        URL res = owner.loader.getResource(taglibBasePath +"/taglib");
                        if(res!=null)
                        return new CustomTagLibrary(createContext(),owner.loader,nsUri,taglibBasePath);
                    } catch (IllegalArgumentException e) {
                        // if taglibBasePath doesn't even look like an URL, getResource throws IllegalArgumentException.
                        // see http://old.nabble.com/bug-1.331-to26145963.html
                    }

                    // support URIs like "this:it" or "this:instance". Note that "this" URI itself is registered elsewhere
                    if (nsUri.startsWith("this:"))
                        try {
                            return new ThisTagLibrary(EXPRESSION_FACTORY.createExpression(nsUri.substring(5)));
                        } catch (JellyException e) {
                            throw new IllegalArgumentException("Illegal expression in the URI: "+nsUri,e);
                        }

                    if (nsUri.equals("jelly:stapler"))
                        return new StaplerTagLibrary();

                    return NO_SUCH_TAGLIBRARY;    // "not found" is also cached.
                }
            });
            taglibs = new WeakReference<LoadingCache<String,TagLibrary>>(m);
        }

        TagLibrary tl = m.getUnchecked(nsUri);
        if (tl==NO_SUCH_TAGLIBRARY)     return null;
        return tl;
    }

    private String trimHeadSlash(String nsUri) {
        if(nsUri.startsWith("/"))
            return nsUri.substring(1);
        else
            return nsUri;
    }

    /**
     * Creates {@link JellyContext} for compiling view scripts
     * for classes in this classloader.
     */
    public JellyContext createContext() {
        JellyContext context = new CustomJellyContext(ROOT_CONTEXT);
        context.setClassLoader(owner.loader);
        context.setExportLibraries(false);
        return context;
    }

    /**
     * Used as the root context for compiling scripts.
     */
    private static final JellyContext ROOT_CONTEXT = new CustomJellyContext();

    /**
     * Place holder in the cache to indicate "no such taglib"
     */
    private static final TagLibrary NO_SUCH_TAGLIBRARY = new TagLibrary() {};

}
