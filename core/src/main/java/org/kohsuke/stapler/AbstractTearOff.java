package org.kohsuke.stapler;

import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Partial default implementation of tear-off class, for convenience of derived classes.
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class AbstractTearOff<CLT,S,E extends Exception> {
    protected final MetaClass owner;
    protected final CLT classLoader;

    protected AbstractTearOff(MetaClass owner, Class<CLT> cltClass) {
        this.owner = owner;
        if(owner.classLoader!=null)
            classLoader = owner.classLoader.loadTearOff(cltClass);
        else
            classLoader = null;
    }

    /**
     * Locates the view script of the given name.
     *
     * @param name
     *      if this is a relative path, such as "foo.jelly" or "foo/bar.groovy",
     *      then it is assumed to be relative to this class, so
     *      "org/acme/MyClass/foo.jelly" or "org/acme/MyClass/foo/bar.groovy"
     *      will be searched.
     *      <p>
     *      If this starts with "/", then it is assumed to be absolute,
     *      and that name is searched from the classloader. This is useful
     *      to do mix-in.
     */
    public S findScript(String name) throws E {
        S script;

        synchronized(this) {
            script = getScripts().get(name);
            if(script==null || MetaClass.NO_CACHE) {
                ClassLoader cl = owner.clazz.getClassLoader();
                if(cl!=null) {

                    URL res = findResource(name, cl);
                    if(res==null) {
                        // look for 'defaults' file
                        int dot = name.lastIndexOf('.');
                        // foo/bar.groovy -> foo/bar.default.groovy
                        // but don't do foo.bar/test -> foo.default.bar/test 
                        if(name.lastIndexOf('/')<dot)
                            res = findResource(name.substring(0,dot)+".default"+name.substring(dot),cl);
                    }
                    if(res!=null) {
                        script = parseScript(res);
                        getScripts().put(name,script);
                    }
                }
            }
        }
        if(script!=null)
            return script;

        // not found on this class, delegate to the parent
        if(owner.baseClass!=null)
            return ((AbstractTearOff<CLT,S,E>)owner.baseClass.loadTearOff(getClass())).findScript(name);

        return null;
    }

    /**
     * Discards the cached script.
     */
    public synchronized void clearScripts() {
        getScripts().clear();
    }

    /**
     * Compiles a script into the compiled form.
     */
    protected abstract S parseScript(URL res) throws E;

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
    private volatile WeakReference<Map<String,S>> scripts;

    private Map<String,S> getScripts() {
        Map<String,S> r=null;
        if(scripts!=null)
            r = scripts.get();

        if(r!=null)
            return r;

        r = new HashMap<String,S>();
        scripts = new WeakReference<Map<String,S>>(r);
        return r;
    }

    protected final URL findResource(String name, ClassLoader cl) {
        URL res = null;
        if (MetaClassLoader.debugLoader != null)
            res = getResource(name, MetaClassLoader.debugLoader.loader);
        if (res == null)
            res = getResource(name, cl);
        return res;
    }

    private URL getResource(String name, ClassLoader cl) {
        URL res;
        if(name.startsWith("/")) {
            // try name as full path to the Jelly script
            res = cl.getResource(name.substring(1));
        } else {
            // assume that it's a view of this class
            res = cl.getResource(owner.clazz.getName().replace('.','/').replace('$','/')+'/'+name);
        }
        return res;
    }
}
