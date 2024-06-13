package org.kohsuke.stapler;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.net.URL;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

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
     */
    private final Map<String, Optional<Reference<S>>> scripts = new ConcurrentHashMap<>();

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
        if (MetaClass.NO_CACHE) {
            return loadScript(name);
        } else {
            Optional<Reference<S>> sr = scripts.get(name);
            S s;
            if (sr == null) { // never before computed
                s = null;
            } else if (sr.isEmpty()) { // cached as null
                return null;
            } else { // cached as non-null; may or may not still have value
                s = sr.get().get();
            }
            if (s == null) { // needs to be computed
                s = loadScript(name);
                scripts.put(name, s == null ? Optional.empty() : Optional.of(new SoftReference<>(s)));
            }
            return s;
        }
    }

    /**
     * Cache-less version of the {@link #findScript(String)} that provides the actual logic.
     */
    protected abstract S loadScript(String name) throws E;

    /**
     * Discards the cached script.
     */
    public void clearScripts() {
        scripts.clear();
    }

    protected abstract URL getResource(String name);
}
