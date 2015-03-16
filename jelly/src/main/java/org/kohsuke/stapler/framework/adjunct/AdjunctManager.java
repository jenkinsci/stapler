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

package org.kohsuke.stapler.framework.adjunct;

import org.kohsuke.stapler.*;

import javax.servlet.ServletException;
import javax.servlet.ServletContext;
import static javax.servlet.http.HttpServletResponse.SC_FORBIDDEN;
import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This application-scoped object that exposes djuncts to URL.
 *
 * <p>
 * Adjuncts are packaging of JavaScript, CSS, and other static assets in jar files with dependency
 * information between them. This allows JavaScript libraries and other static assets to be reused
 * across different projects through Maven/Ivy.
 *
 * <p>
 * To use {@link AdjunctManager} in your application, create one instance, and bind it to URL
 * (like you do any other objects.) The most typical way of doing this is to define it as a
 * field in your top-level object.
 *
 * <pre>
 * public class MyApplication {
 *     public final AdjunctManager adjuncts = new AdjunctManager(context,getClass().getClassLoader(),"/adjuncts");
 * }
 * </pre>
 *
 * <p>
 * How you include an adjunct will depend on your template language, but for example in Jelly you do:
 * <pre>{@code <st:adjunct includes="org.kohsuke.stapler.bootstrap"/>}</pre>
 *
 * Or from Groovy you do:
 * <pre>{@code adjunct "org.kohsuke.stapler.bootstrap"}</pre>
 *
 * <p>
 * ... and this produces a series of <tt>style</tt> and <tt>script</tt> tags that include all the
 * necessary JavaScript, CSS, and their dependencies.
 *
 * <p>
 * Internally, this class provides caching for {@link Adjunct}s.
 *
 * @author Kohsuke Kawaguchi
 * @see Adjunct
 */
public class AdjunctManager {
    private final ConcurrentHashMap<String, Adjunct> adjuncts = new ConcurrentHashMap<String,Adjunct>();

    /**
     * Map used as a set to remember which resources can be served.
     */
    private final ConcurrentHashMap<String,String> allowedResources = new ConcurrentHashMap<String,String>();

    private final ClassLoader classLoader;

    /**
     * Absolute URL of the {@link AdjunctManager} in the calling application where it is bound to.
     *
     * <p>
     * The path is treated relative from the context path of the application, and it
     * needs to end without '/'. So it needs to be something like "foo/adjuncts" or
     * just "adjuncts". Can be e.g. {@code adjuncts/uNiQuEhAsH} to improve caching behavior.
     */
    public final String rootURL;

    /**
     * Hint instructing adjuncts to load a debuggable non-minified version of the script,
     * as opposed to the production version.
     *
     * This is only a hint, and so the semantics of it isn't very well defined. The intention
     * is to assist JavaScript debugging.
     */
    public boolean debug = Boolean.getBoolean(AdjunctManager.class.getName()+".debug");

    public final WebApp webApp;
    private final long expiration;

    @Deprecated
    public AdjunctManager(ServletContext context,ClassLoader classLoader, String rootURL) {
        this(context, classLoader, rootURL, /* one day */24L * 60 * 60 * 1000);
    }

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
    public AdjunctManager(ServletContext context, ClassLoader classLoader, String rootURL, long expiration) {
        this.classLoader = classLoader;
        this.rootURL = rootURL;
        this.webApp = WebApp.get(context);
        this.expiration = expiration;
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
            a = new Adjunct(this,name,classLoader);
            adjuncts.put(name,a);
            return a;
        }
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
            throw HttpResponses.error(SC_NOT_FOUND,new IllegalArgumentException("No such adjunct found: "+path));
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
     * {@link AdjunctManager} is capable of serving all the resources visible
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

    /**
     * Key in {@link ServletContext} to look up {@link AdjunctManager}.
     */
    private static final String KEY = AdjunctManager.class.getName();
}
