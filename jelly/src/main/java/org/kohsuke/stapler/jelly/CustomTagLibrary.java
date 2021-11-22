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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.jelly.JellyContext;
import org.apache.commons.jelly.JellyException;
import org.apache.commons.jelly.Script;
import org.apache.commons.jelly.Tag;
import org.apache.commons.jelly.TagLibrary;
import org.apache.commons.jelly.impl.TagScript;
import org.kohsuke.stapler.MetaClass;
import org.kohsuke.stapler.MetaClassLoader;
import org.xml.sax.Attributes;

import java.io.File;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    private final Map<String,ExpirableCacheHit<Script>> scripts = new ConcurrentHashMap<>();

    private static final class ExpirableCacheHit<S> {
        private final long timestamp;
        private final Reference<S> script;
        ExpirableCacheHit(long timestamp, S script) {
            this.timestamp = timestamp;
            this.script = new SoftReference<>(script);
        }
    }

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

        ExpirableCacheHit<Script> cachedScript = scripts.get(name);

        if (cachedScript != null) {
            if (!MetaClass.NO_CACHE) {
                return cachedScript.script.get();
            }

            URL res = classLoader.getResource(basePath + '/' + name + ".jellytag");
            if (res == null) {
                res = classLoader.getResource(basePath + '/' + name + ".jelly");
            }
            if (res != null) {
                File file = fileOf(res);
                if (file != null) {
                    long timestamp = file.lastModified();
                    if (timestamp == cachedScript.timestamp) {
                        return cachedScript.script.get();
                    }
                }
            }
        }

        Script script =null;
        if(MetaClassLoader.debugLoader!=null)
            script = load(name, MetaClassLoader.debugLoader.loader);
        if(script==null)
            script = load(name, classLoader);
        return script;
    }

    private Script load(String name, ClassLoader classLoader) throws JellyException {
        Script script;
        // prefer 'foo.jellytag' but for backward compatibility, support the plain .jelly extension as well.
        URL res = classLoader.getResource(basePath + '/' + name + ".jellytag");
        if (res==null)
            res = classLoader.getResource(basePath + '/' + name + ".jelly");
        if(res!=null) {
            script = loadJellyScript(res);
            File file = fileOf(res);
            if (file == null) {
                throw new IllegalStateException("file should not be null here");
            }
            scripts.put(name, new ExpirableCacheHit<>(file.lastModified(), script));
            return script;
        }

        for (JellyTagFileLoader loader : loaders) {
            Script s = loader.load(this, name, classLoader);
            if(s!=null) {
                // can't determine timestamp here, will be cached in production mode
                scripts.put(name, new ExpirableCacheHit<>(0, s));
                return s;
            }
        }

        return null;
    }

    private static final Pattern JAR_URL = Pattern.compile("jar:(file:.+)!/.*");

    @SuppressFBWarnings(value = "PATH_TRAVERSAL_IN", justification = "Files are read from approved plugins, not from user input.")
    private static File fileOf(URL res) {
        try {
            switch (res.getProtocol()) {
                case "file":
                    return new File(res.toURI());
                case "jar":
                    Matcher m = JAR_URL.matcher(res.toString());
                    if (m.matches()) {
                        return new File(new URI(m.group(1)));
                    } else {
                        return null;
                    }
                default:
                    return null;
            }
        } catch (URISyntaxException | IllegalArgumentException x) {
            return null; // caching is a best effort
        }
    }


    private Script loadJellyScript(URL res) throws JellyException {
        // compile script
        JellyContext context = new CustomJellyContext(master);
        context.setClassLoader(classLoader);
        return context.compileScript(res);
    }
}
