package org.kohsuke.stapler.assets;

import java.net.URL;

/**
 * Plain implementation of {@link AssetLoader} that looks for
 * static resources via known file extensions. It also allows
 * a package to be marked with {@code .adjunct} file to allow
 * any files in that directory to be served.
 *
 * @author Kohsuke Kawaguchi
 */
public class DefaultAssetLoader extends AssetLoader {
    /**
     * {@link ClassLoader} used to search and serve static resources.
     */
    private final ClassLoader classLoader;

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
     * <p>
     * {@link AssetsManager} is capable of serving all the resources visible
     * in the classloader by default. If the resource files need to be kept private,
     * return false, which causes the request to fail with 401.
     *
     * Otherwise return true, in which case the resource will be served.
     */
    protected boolean allowResourceToBeServed(String absolutePath) {
        // does it have an adjunct directory marker?
        int idx = absolutePath.lastIndexOf('/');
        if (idx>0 && classLoader.getResource(absolutePath.substring(0,idx)+"/.adjunct")!=null)
            return true;

        // backward compatible behaviour
        return absolutePath.endsWith(".gif")
            || absolutePath.endsWith(".png")
            || absolutePath.endsWith(".css")
            || absolutePath.endsWith(".js");
    }
}
