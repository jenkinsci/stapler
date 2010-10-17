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

import com.google.common.collect.MapMaker;

import java.net.URL;
import java.util.Map;

/**
 * Partial default implementation of tear-off class, for convenience of derived classes.
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class AbstractTearOff<CLT,S,E extends Exception> {
    protected final MetaClass owner;
    protected final CLT classLoader;

    protected AbstractTearOff(MetaClass owner, Class<CLT> cltClass) {
        this.owner = owner;
        if(owner.classLoader!=null)
            classLoader = owner.classLoader.loadTearOff(cltClass);
        else
            classLoader = null;
    }

    /**
     * Locates the view script of the given name.
     *
     * @param name
     *      if this is a relative path, such as "foo.jelly" or "foo/bar.groovy",
     *      then it is assumed to be relative to this class, so
     *      "org/acme/MyClass/foo.jelly" or "org/acme/MyClass/foo/bar.groovy"
     *      will be searched.
     *      <p>
     *      If this starts with "/", then it is assumed to be absolute,
     *      and that name is searched from the classloader. This is useful
     *      to do mix-in.
     */
    public S findScript(String name) throws E {
        if (MetaClass.NO_CACHE)
            return loadScript(name);
        else
            return scripts.get(name).get();
    }

    private S loadScript(String name) throws E {
        ClassLoader cl = owner.clazz.getClassLoader();
        if(cl!=null) {
            URL res = findResource(name, cl);
            if(res==null) {
                // look for 'defaults' file
                int dot = name.lastIndexOf('.');
                // foo/bar.groovy -> foo/bar.default.groovy
                // but don't do foo.bar/test -> foo.default.bar/test
                // as of 2010/9, this behaviour is considered deprecated, but left here for backward compatibility.
                // we need a better way to refer to the resource of the same name in the base type.
                if(name.lastIndexOf('/')<dot)
                    res = findResource(name.substring(0,dot)+".default"+name.substring(dot),cl);
            }
            if(res!=null)
                return parseScript(res);
        }

        // not found on this class, delegate to the parent
        if(owner.baseClass!=null)
            return ((AbstractTearOff<CLT,S,E>)owner.baseClass.loadTearOff(getClass())).findScript(name);

        return null;
    }

    /**
     * Discards the cached script.
     */
    public synchronized void clearScripts() {
        scripts.clear();
    }

    /**
     * Compiles a script into the compiled form.
     */
    protected abstract S parseScript(URL res) throws E;

    /**
     * Compiled scripts of this class.
     * Access needs to be synchronized.
     *
     * <p>
     * Jelly leaks memory (because Scripts hold on to Tag)
     * which usually holds on to JellyContext that was last used to run it,
     * which often holds on to some big/heavy objects.)
     *
     * So it's important to allow Scripts to be garbage collected.
     * This is not an ideal fix, but it works.
     *
     * {@link Optional} is used as Google Collection doesn't allow null values in a map.
     */
    private final Map<String,Optional<S>> scripts = new MapMaker().softValues().makeComputingMap(new com.google.common.base.Function<String, Optional<S>>() {
        public Optional<S> apply(String from) {
            try {
                return Optional.create(loadScript(from));
            } catch (RuntimeException e) {
                throw e;    // pass through
            } catch (Exception e) {
                throw new ScriptLoadException(e);
            }
        }
    });

    protected final URL findResource(String name, ClassLoader cl) {
        URL res = null;
        if (MetaClassLoader.debugLoader != null)
            res = getResource(name, MetaClassLoader.debugLoader.loader);
        if (res == null)
            res = getResource(name, cl);
        return res;
    }

    private URL getResource(String name, ClassLoader cl) {
        URL res;
        if(name.startsWith("/")) {
            // try name as full path to the Jelly script
            res = cl.getResource(name.substring(1));
        } else {
            // assume that it's a view of this class
            res = cl.getResource(owner.clazz.getName().replace('.','/').replace('$','/')+'/'+name);
        }
        return res;
    }
}
