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

import org.apache.commons.jelly.JellyContext;
import org.apache.commons.jelly.JellyException;
import org.apache.commons.jelly.Script;
import org.apache.commons.jelly.Tag;
import org.apache.commons.jelly.TagLibrary;
import org.apache.commons.jelly.impl.TagScript;
import org.kohsuke.stapler.MetaClass;
import org.kohsuke.stapler.MetaClassLoader;
import org.xml.sax.Attributes;

import java.net.URL;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

/**
 * {@link TagLibrary} that loads tags from tag files in a directory.
 *
 * @author Kohsuke Kawaguchi
 */
public final class CustomTagLibrary extends TagLibrary {

    /**
     * Inherits values from this context.
     * This context would be shared by multiple threads,
     * but as long as we all just read it, it should be OK.
     */
    private final JellyContext master;

    private final ClassLoader classLoader;
    public final MetaClassLoader metaClassLoader;
    public final String nsUri;
    public final String basePath;

    /**
     * Compiled tag files.
     */
    private final Map<String,Script> scripts = new Hashtable<String,Script>();

    private final List<JellyTagFileLoader> loaders;

    public CustomTagLibrary(JellyContext master, ClassLoader classLoader, String nsUri, String basePath) {
        this.master = master;
        this.classLoader = classLoader;
        this.nsUri = nsUri;
        this.basePath = basePath;
        this.metaClassLoader = MetaClassLoader.get(classLoader);
        this.loaders = JellyTagFileLoader.discover(classLoader);
    }

    public TagScript createTagScript(String name, Attributes attributes) throws JellyException {
        final Script def = load(name);
        if(def==null) return null;

        return new CallTagLibScript() {
            protected Script resolveDefinition(JellyContext context) {
                return def;
            }
        };
    }

    public Tag createTag(String name, Attributes attributes) throws JellyException {
        // IIUC, this method is only used by static tag to discover the correct tag at runtime,
        // and since stapler taglibs are always resolved statically, we shouldn't have to implement this method
        // at all.

        // by not implementing this method, we can put all the login in the TagScript-subtype, which eliminates
        // the need of stateful Tag instances and their overheads.
        return null;
    }

    /**
     * Obtains the script for the given tag name. Loads if necessary.
     *
     * <p>
     * Synchronizing this method would have a potential race condition
     * if two threads try to load two tags that are referencing each other.
     *
     * <p>
     * So we synchronize {@link #scripts}, even though this means
     * we may end up compiling the same script twice.
     */
    private Script load(String name) throws JellyException {

        Script script = scripts.get(name);
        if(script!=null && !MetaClass.NO_CACHE)
            return script;

        script=null;
        if(MetaClassLoader.debugLoader!=null)
            script = load(name, MetaClassLoader.debugLoader.loader);
        if(script==null)
            script = load(name, classLoader);
        return script;
    }

    private Script load(String name, ClassLoader classLoader) throws JellyException {
        Script script;
        // prefer 'foo.jellytag' but for backward compatibility, support the plain .jelly extention as well.
        URL res = classLoader.getResource(basePath + '/' + name + ".jellytag");
        if (res==null)
            res = classLoader.getResource(basePath + '/' + name + ".jelly");
        if(res!=null) {
            script = loadJellyScript(res);
            scripts.put(name,script);
            return script;
        }

        for (JellyTagFileLoader loader : loaders) {
            Script s = loader.load(this, name, classLoader);
            if(s!=null) {
                scripts.put(name,s);
                return s;
            }
        }

        return null;
    }

    private Script loadJellyScript(URL res) throws JellyException {
        // compile script
        JellyContext context = new CustomJellyContext(master);
        context.setClassLoader(classLoader);
        return context.compileScript(res);
    }
}
