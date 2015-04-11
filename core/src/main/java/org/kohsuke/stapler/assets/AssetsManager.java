package org.kohsuke.stapler.assets;

import org.kohsuke.stapler.HttpResponses;
import org.kohsuke.stapler.MetaClass;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import javax.servlet.ServletException;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import static javax.servlet.http.HttpServletResponse.*;

/**
 * Serves static contents, such as CSS, JavaScript, images, fonts, and etc
 * from classpath.
 *
 * <p>
 * This allows JavaScript libraries and other static assets to be packaged in a jar and reused
 * across different projects through Maven/Ivy/etc.
 *
 * <p>
 * To use {@link AssetsManager} in your application, create one instance, and bind it to URL
 * (like you do any other objects.) The most typical way of doing this is to define it as a
 * field in your top-level object.
 *
 * <pre>
 * public class MyApplication {
 *     public final ClassPathStaticResourceManager assets = new ClassPathStaticResourceManager(context,getClass().getClassLoader(),"/assets");
 * }
 * </pre>
 *
 * <p>
 * If {@code foo.jar} contains {@code abc/def/ghi.js}, this file is now accessible
 * through {@code /assets/abc/def/ghi.js}
 *
 * @author Kohsuke Kawaguchi
 */
public class AssetsManager {
    /**
     * Map used as a set to remember which resources can be served.
     */
    private final ConcurrentHashMap<String,Asset> resources = new ConcurrentHashMap<String,Asset>();

    private final CopyOnWriteArrayList<AssetLoader> loaders = new CopyOnWriteArrayList<AssetLoader>();

    /**
     * Absolute URL of the {@link AssetsManager} in the calling application where it is bound to.
     *
     * <p>
     * The path is treated relative from the context path of the application, and it
     * needs to end without '/'. So it needs to be something like "foo/adjuncts" or
     * just "adjuncts". Can be e.g. {@code adjuncts/uNiQuEhAsH} to improve caching behavior.
     */
    public final String rootURL;

    private final long expiration;

    /**
     * @param rootURL
     *      See {@link #rootURL} for the meaning of this parameter.
     * @param expiration milliseconds from service time until expiration, for {@link #doDynamic}
     *                    (as in {@link StaplerResponse#serveFile(StaplerRequest, URL, long)});
     *                    if {@link #rootURL} is unique per session then this can be very long;
     *                    otherwise a day might be reasonable
     */
    public AssetsManager(String rootURL, long expiration, AssetLoader... loaders) {
        this.rootURL = rootURL;
        this.expiration = expiration;
        this.loaders.addAll(Arrays.asList(loaders));
    }

    /**
     * Returns the live list of {@link AssetLoader} for direct manipulation.
     */
    public List<AssetLoader> getLoaders() {
        return loaders;
    }

    /**
     * Serves resources in the class loader.
     */
    public void doDynamic(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
        String path = req.getRestOfPath();
        if (path.charAt(0)=='/') path = path.substring(1);

        Asset res = resources.get(path);
        if (res == null || res.isStale()) {
            for (AssetLoader l : loaders) {
                res = l.load(path);
                if (res != null) {
                    // remember URLs that we can serve. but don't remember error ones, as it might be unbounded
                    resources.put(path, res);
                    break;
                }
            }
        }

        if (res == null) {
            throw HttpResponses.error(SC_NOT_FOUND, new IllegalArgumentException("No such adjunct found: " + path));
        } else {
            long expires = MetaClass.NO_CACHE ? 0 : expiration;
            rsp.serveFile(req, res.getURL(), expires);
        }
    }
}
