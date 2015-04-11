package org.kohsuke.stapler.assets;

import javax.servlet.ServletException;
import java.io.IOException;
import java.net.URL;

/**
 * @author Kohsuke Kawaguchi
 */
public abstract class AssetLoader {
    /**
     * When {@link AssetsManager} is given path like "foo/bar/zot.js", this object
     * is method is responsible for resolving it into the actual content that we will serve.
     *
     * <p>
     * This enables access control, on-the-fly transformation, and other redirections.
     * {@link AssetLoader} is meant for static files, which do not change their content based
     * on any aspects of the request (except the request URI). So the returned {@link URL} is
     * cached for efficiency by {@link AssetsManager}.
     *
     * @param path
     *      Absolute resource path separated by '/', such as "org/kohsuke/foo.js"
     *      No leading '/'.
     * @return null
     *      if this {@link AssetLoader} do not find anything in the given path, to allow
     *      other {@link AssetLoader} to look at the path.
     */
    public abstract URL load(String path) throws IOException, ServletException;
}
