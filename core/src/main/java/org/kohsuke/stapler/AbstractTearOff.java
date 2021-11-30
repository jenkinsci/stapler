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
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Partial default implementation of tear-off class, for convenience of derived classes.
 *
 * @param <CLT>
 *      ClassLoader tear-off.
 * @author Kohsuke Kawaguchi
 */
public abstract class AbstractTearOff<CLT,S,E extends Exception> extends CachingScriptLoader<S,E> {

    private static final Logger LOGGER = Logger.getLogger(AbstractTearOff.class.getName());

    protected final MetaClass owner;
    protected final CLT classLoader;

    private static final class ExpirableCacheHit<S> {
        private final long timestamp;
        private final Reference<S> script;
        ExpirableCacheHit(long timestamp, S script) {
            this.timestamp = timestamp;
            this.script = new SoftReference<>(script);
        }
    }
    private final Map<String, ExpirableCacheHit<S>> cachedScripts = new ConcurrentHashMap<>();

    protected AbstractTearOff(MetaClass owner, Class<CLT> cltClass) {
        this.owner = owner;
        if(owner.classLoader!=null)
            classLoader = owner.classLoader.loadTearOff(cltClass);
        else
            classLoader = null;
    }

    protected final WebApp getWebApp() {
        return owner.webApp;
    }

    /**
     * The file extension of this kind of scripts, such as ".jelly"
     */
    protected abstract String getDefaultScriptExtension();

    /**
     * Checks if the file name is allowed as a script of this type.
     *
     * This is necessary to have multiple facets co-exist peacefully
     * without them trying to load each other's scripts.
     */
    protected boolean hasAllowedExtension(String name) {
        return name.endsWith(getDefaultScriptExtension());
    }

    /**
     * Loads the script just from the target class without considering inherited scripts
     * from its base types.
     */
    public S resolveScript(String name) throws E {
        if (name.lastIndexOf('.')<=name.lastIndexOf('/'))   // no file extension provided
            name += getDefaultScriptExtension();
        if (!hasAllowedExtension(name))
            // for multiple Facets to co-exist peacefully, we need to be able to determine
            // which Facet is responsible for a given view just from the file name
            return null;

        URL res = getResource(name);
        if(res==null) {
            // look for 'defaults' file
            int dot = name.lastIndexOf('.');
            // foo/bar.groovy -> foo/bar.default.groovy
            // but don't do foo.bar/test -> foo.default.bar/test
            // as of 2010/9, this behaviour is considered deprecated, but left here for backward compatibility.
            // we need a better way to refer to the resource of the same name in the base type.
            if(name.lastIndexOf('/')<dot)
                res = getResource(name.substring(0, dot) + ".default" + name.substring(dot));
        }
        if (res != null) {
            if (MetaClass.NO_CACHE) {
                File file = fileOf(res);
                if (file == null) {
                    LOGGER.log(Level.FINE, "no file associated with {0}", res);
                    return parseScript(res);
                } else {
                    long timestamp = file.lastModified();
                    if (timestamp == 0) {
                        LOGGER.log(Level.FINE, "no timestamp associated with {0}", file);
                        return parseScript(res);
                    } else {
                        ExpirableCacheHit<S> cached = cachedScripts.get(res.toString());
                        if (cached == null) {
                            S script;
                            if (LOGGER.isLoggable(Level.FINE)) {
                                long start = System.nanoTime();
                                try {
                                    script = parseScript(res);
                                } finally {
                                    LOGGER.log(Level.FINE, "cache miss; took {0}ms to parse {1}", new Object[] {(System.nanoTime() - start) / 1_000_000, res});
                                }
                            } else {
                                LOGGER.log(Level.FINE, "cache miss on {0}", res);
                                script = parseScript(res);
                            }
                            cachedScripts.put(res.toString(), new ExpirableCacheHit<>(timestamp, script));
                            return script;
                        } else {
                            S script;
                            if (timestamp == cached.timestamp) {
                                script = cached.script.get();
                                if (script == null) {
                                    LOGGER.log(Level.FINE, "cache hit on {0} but value collected", res);
                                } else {
                                    LOGGER.log(Level.FINE, "cache hit on {0}", res);
                                }
                            } else {
                                LOGGER.log(Level.FINE, "expired cache hit on {0}", res);
                                script = null;
                            }
                            if (script == null) {
                                script = parseScript(res);
                                cachedScripts.put(res.toString(), new ExpirableCacheHit<>(timestamp, script));
                            }
                            return script;
                        }
                    }
                }
            } else {
                LOGGER.log(Level.FINE, "standard CachingScriptLoader logic applies to {0}", res);
                return parseScript(res);
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

    @Override
    protected final S loadScript(String name) throws E {
        S s = resolveScript(name);
        if (s!=null)    return s;

        // not found on this class, delegate to the parent
        if(owner.baseClass!=null)
            return ((AbstractTearOff<CLT,S,E>)owner.baseClass.loadTearOff(getClass())).findScript(name);

        return null;
    }

    /**
     * Compiles a script into the compiled form.
     */
    protected abstract S parseScript(URL res) throws E;

    @Override
    protected URL getResource(String name) {
        return owner.klass.getResource(name);
    }
}
