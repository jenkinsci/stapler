package org.kohsuke.stapler.jelly.groovy;

import org.apache.commons.jelly.Script;
import org.kohsuke.stapler.MetaClass;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Kohsuke Kawaguchi
 */
public class GroovyClassTearOff {
    private final MetaClass owner;

    private final GroovyClassLoaderTearOff classLoader;

    /**
     * Compiled Groovy script views of this class.
     * Access needs to be synchronized.
     */
    private final Map<String, Script> scripts = new HashMap<String,Script>();

    public GroovyClassTearOff(MetaClass owner) {
        this.owner = owner;
        if(owner.classLoader!=null)
            classLoader = owner.classLoader.loadTearOff(GroovyClassLoaderTearOff.class);
        else
            classLoader = null;
    }

    public Script findScript(String name) throws IOException {
        Script script;

        synchronized(scripts) {
            script = scripts.get(name);
            if(script==null || MetaClass.NO_CACHE) {
                ClassLoader cl = owner.clazz.getClassLoader();
                if(cl!=null) {

                    URL res;

                    if(name.startsWith("/")) {
                        // try name as full path to the Jelly script
                        res = cl.getResource(name.substring(1));
                    } else {
                        // assume that it's a view of this class
                        res = cl.getResource(owner.clazz.getName().replace('.','/').replace('$','/')+'/'+name);
                    }

                    if(res!=null) {
                        script = classLoader.parse(res);
                        scripts.put(name,script);
                    }
                }
            }
        }
        if(script!=null)
            return script;

        // not found on this class, delegate to the parent
        if(owner.baseClass!=null)
            return owner.baseClass.loadTearOff(GroovyClassTearOff.class).findScript(name);

        return null;
    }
}
