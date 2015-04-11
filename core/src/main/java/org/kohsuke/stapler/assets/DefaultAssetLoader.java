package org.kohsuke.stapler.assets;

import java.net.URL;

/**
 * Plain implementation of {@link AssetLoader} that looks for
 * static resources via straight {@link ClassLoader#getResource(String)} call.
 *
 * @author Kohsuke Kawaguchi
 */
public class DefaultAssetLoader extends AssetLoader {
    /**
     * {@link ClassLoader} used to search and serve static resources.
     */
    protected final ClassLoader classLoader;

    public DefaultAssetLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    @Override
    public URL load(String path) {
        if(!allowResourceToBeServed(path))
            return null;
        return classLoader.getResource(path);
    }

    /**
     * Controls whether the given resource can be served to browsers.
     *
     * <p>
     * This method can be overridden by the sub classes to change the access control behavior.
     *
     * Otherwise return true, in which case the resource will be served.
     */
    protected boolean allowResourceToBeServed(String absolutePath) {
        return true;
    }
}
