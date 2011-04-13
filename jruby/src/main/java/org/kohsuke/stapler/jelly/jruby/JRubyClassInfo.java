package org.kohsuke.stapler.jelly.jruby;

import org.apache.commons.jelly.Script;
import org.jruby.RubyClass;
import org.kohsuke.stapler.CachingScriptLoader;
import org.kohsuke.stapler.MetaClass;
import org.kohsuke.stapler.WebApp;

import java.io.IOException;
import java.net.URL;

/**
 * {@link MetaClass}-equivalent for {@link RubyClass}
 *
 * @author Kohsuke Kawaguchi
 * @see JRubyFacet#getClassInfo(RubyClass)
 */
public class JRubyClassInfo extends CachingScriptLoader<Script,IOException> {
    /**
     * Facet that we belong to.
     */
    private final JRubyFacet facet;

    public final RubyClass clazz;

    JRubyClassInfo(JRubyFacet facet, RubyClass clazz) {
        this.facet = facet;
        this.clazz = clazz;
    }

    public JRubyClassInfo getSuperClass() {
        return facet.getClassInfo(clazz.getSuperClass());
    }

    @Override
    protected Script loadScript(String name) throws IOException {
        ClassLoader cl = WebApp.getCurrent().getClassLoader();

        if(cl!=null) {
            URL res = findResource(name, cl);
            if(res!=null)
                return facet.parse(res);
        }

        // not found on this class, delegate to the parent
        JRubyClassInfo sc = getSuperClass();
        if(sc!=null)
            return sc.findScript(name);

        return null;
    }

    @Override
    protected URL getResource(String name, ClassLoader cl) {
        URL res;
        if(name.startsWith("/")) {
            // try name as full path to the Jelly script
            res = cl.getResource(name.substring(1));
        } else {
            // assume that it's a view of this class
            res = cl.getResource(clazz.getName().replace("::","/")+'/'+name);
        }
        return res;
    }
}
