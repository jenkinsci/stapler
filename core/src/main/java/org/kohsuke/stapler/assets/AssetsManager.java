package org.kohsuke.stapler.assets;

import org.kohsuke.stapler.HttpResponses;
import org.kohsuke.stapler.MetaClass;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.WebApp;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.ConcurrentHashMap;

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
    private final ConcurrentHashMap<String,String> allowedResources = new ConcurrentHashMap<String,String>();

    private final ClassLoader classLoader;

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

    private final WebApp webApp;

    /**
     * @param classLoader
     *      ClassLoader to load adjuncts from.
     * @param rootURL
     *      See {@link #rootURL} for the meaning of this parameter.
     * @param expiration milliseconds from service time until expiration, for {@link #doDynamic}
     *                    (as in {@link StaplerResponse#serveFile(StaplerRequest, URL, long)});
     *                    if {@link #rootURL} is unique per session then this can be very long;
     *                    otherwise a day might be reasonable
     */
    public AssetsManager(ServletContext context, ClassLoader classLoader, String rootURL, long expiration) {
        this.classLoader = classLoader;
        this.rootURL = rootURL;
        this.webApp = WebApp.get(context);
        this.expiration = expiration;
    }

    /**
     * {@link ClassLoader} used to search and serve static resources.
     */
    public ClassLoader getClassLoader() {
        return classLoader;
    }

    /**
     * {@link WebApp} for which this assets manager is working.
     */
    public WebApp getWebApp() {
        return webApp;
    }

    /**
     * Serves resources in the class loader.
     */
    public void doDynamic(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
        String path = req.getRestOfPath();
        if (path.charAt(0)=='/') path = path.substring(1);

        if(!allowedResources.containsKey(path)) {
            if(!allowResourceToBeServed(path)) {
                rsp.sendError(SC_FORBIDDEN);
                return;
            }
            // remember URLs that we can serve. but don't remember error ones, as it might be unbounded
            allowedResources.put(path,path);
        }

        URL res = classLoader.getResource(path);
        if(res==null) {
            throw HttpResponses.error(SC_NOT_FOUND, new IllegalArgumentException("No such adjunct found: " + path));
        } else {
            long expires = MetaClass.NO_CACHE ? 0 : expiration;
            rsp.serveFile(req,res,expires);
        }
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
