package org.kohsuke.stapler;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import java.net.URL;

/**
 * Convenient base class for caching loaded scripts.
 * @author Kohsuke Kawaguchi
 */
public abstract class CachingScriptLoader<S, E extends Exception> {
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
    private final LoadingCache<String,Optional<S>> scripts = CacheBuilder.newBuilder().softValues().build(new CacheLoader<String, Optional<S>>() {
        public Optional<S> load(String from) {
            try {
                return Optional.create(loadScript(from));
            } catch (RuntimeException e) {
                throw e;    // pass through
            } catch (Exception e) {
                throw new ScriptLoadException(e);
            }
        }
    });

    /**
     * Locates the view script of the given name.
     *
     * @param name
     *      if this is a relative path, such as "foo.jelly" or "foo/bar.groovy",
     *      then it is assumed to be relative to this class, so
     *      "org/acme/MyClass/foo.jelly" or "org/acme/MyClass/foo/bar.groovy"
     *      will be searched.
     *      <p>
     *      If the extension is omitted, the default extension will be appended.
     *      This is useful for some loaders that support loading multiple file extension types
     *      (such as Jelly support.)
     *      <p>
     *      If this starts with "/", then it is assumed to be absolute,
     *      and that name is searched from the classloader. This is useful
     *      to do mix-in.
     * @return null if none was found.
     */
    public S findScript(String name) throws E {
        if (MetaClass.NO_CACHE) 
            return loadScript(name);
        else
            return scripts.getUnchecked(name).get();
    }

    /**
     * Cache-less version of the {@link #findScript(String)} that provides the actual logic.
     */
    protected abstract S loadScript(String name) throws E;

    /**
     * Discards the cached script.
     */
    public synchronized void clearScripts() {
        scripts.invalidateAll();
    }

    protected abstract URL getResource(String name);
}
