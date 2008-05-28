package org.kohsuke.stapler.framework.adjunct;

import org.kohsuke.stapler.MetaClass;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import javax.servlet.ServletException;
import javax.servlet.ServletContext;
import static javax.servlet.http.HttpServletResponse.SC_FORBIDDEN;
import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This application-scoped object works like a factory for {@link Adjunct}s and provides caching.
 *
 * <p>
 * This object can be UI-bound by stapler, and adjunct CSS and JavaScript can be accessed like
 *
 * <pre>
 * &lt;link rel="stylesheet" href=".../css/org/example/style" type="text/css" />
 * &lt;script                href=".../js/org/example/style">&lt;/script>
 * </pre>
 * @author Kohsuke Kawaguchi
 */
public class AdjunctManager {
    private final ConcurrentHashMap<String,Adjunct> adjuncts = new ConcurrentHashMap<String,Adjunct>();

    /**
     * Map used as a set to remember which resources can be served.
     */
    private final ConcurrentHashMap<String,String> allowedResources = new ConcurrentHashMap<String,String>();

    private final ClassLoader classLoader;

    /**
     * Absolute URL of the {@link AdjunctManager} in the calling application where it is bound to.
     *
     * <p>
     * This must not be relative, and ends without '/'. So it needs to be something like "/contextPath/adjuncts" 
     */
    public final String rootURL;

    /**
     * @param classLoader
     *      ClassLoader to load adjuncts from.
     * @param rootURL
     *      See {@link #rootURL} for the meaning of this parameter.
     */
    public AdjunctManager(ServletContext context,ClassLoader classLoader, String rootURL) {
        this.classLoader = classLoader;
        this.rootURL = rootURL;
        // register this globally
        context.setAttribute(KEY,this);
    }

    public static AdjunctManager get(ServletContext context) {
        return (AdjunctManager) context.getAttribute(KEY);
    }

    /**
     * Obtains the adjunct.
     *
     * @return
     *      always non-null.
     * @throws IOException
     *      if failed to locate {@link Adjunct}.
     */
    public Adjunct get(String name) throws IOException {
        Adjunct a = adjuncts.get(name);
        if(a!=null) return a;   // found it

        synchronized (this) {
            a = adjuncts.get(name);
            if(a!=null) return a;   // one more check before we start loading
            a = new Adjunct(name,classLoader);
            adjuncts.put(name,a);
            return a;
        }
    }

    /**
     * Serves resources in the class loader.
     */
    public void doDynamic(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
        String path = req.getRestOfPath();
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
            rsp.sendError(SC_NOT_FOUND);
        } else {
            long expires = MetaClass.NO_CACHE ? 0 : 24L * 60 * 60 * 1000; /*1 day*/
            rsp.serveFile(req,res,expires);
        }
    }

    /**
     * Controls whether the given resource can be served to browsers.
     *
     * <p>
     * This method can be overrided by the sub classes to change the access control behavior.
     *
     * <p>
     * {@link AdjunctManager} is capable of serving all the resources visible
     * in the classloader by default. If the resource files need to be kept private,
     * return false, which causes the request to fail with 401. 
     *
     * Otherwise return true, in which case the resource will be served.
     */
    protected boolean allowResourceToBeServed(String absolutePath) {
        return absolutePath.endsWith(".gif")
            || absolutePath.endsWith(".png")
            || absolutePath.endsWith(".js");
    }

    /**
     * Key in {@link ServletContext} to look up {@link AdjunctManager}.
     */
    private static final String KEY = AdjunctManager.class.getName();
}
