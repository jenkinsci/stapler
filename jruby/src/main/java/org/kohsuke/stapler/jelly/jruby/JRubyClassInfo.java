package org.kohsuke.stapler.jelly.jruby;

import org.apache.commons.jelly.Script;
import org.jruby.RubyClass;
import org.kohsuke.stapler.CachingScriptLoader;
import org.kohsuke.stapler.MetaClass;
import org.kohsuke.stapler.WebApp;

import java.io.IOException;
import java.net.URL;
import java.util.Locale;

/**
 * {@link MetaClass}-equivalent for {@link RubyClass}
 *
 * @author Kohsuke Kawaguchi
 * @see JRubyFacet#getClassInfo(RubyClass)
 */
public final class JRubyClassInfo extends CachingScriptLoader<Script,IOException> {
    /**
     * Facet that we belong to.
     */
    private final JRubyFacet facet;

    public final RubyClass clazz;
    
    private final RubyKlassNavigator navigator;

    JRubyClassInfo(JRubyFacet facet, RubyClass clazz, RubyKlassNavigator navigator) {
        this.facet = facet;
        this.clazz = clazz;
        this.navigator = navigator;
    }

    public JRubyClassInfo getSuperClass() {
        return facet.getClassInfo(clazz.getSuperClass());
    }

    @Override
    protected Script loadScript(String name) throws IOException {
        ClassLoader cl = WebApp.getCurrent().getClassLoader();

        if(cl!=null) {
            for (RubyTemplateLanguage l : facet.languages) {
                URL res = getResource(name + l.getScriptExtension());
                if(res!=null)
                    return facet.parseScript(res);
            }
        }

        // not found on this class, delegate to the parent
        JRubyClassInfo sc = getSuperClass();
        if(sc!=null)
            return sc.findScript(name);

        return null;
    }

    @Override
    protected URL getResource(String name) {
        return navigator.getResource(clazz,name);
    }

    /**
     * Converts "FooBarZot" to "foo_bar_zot"
     */
    static String decamelize(String s) {
        return s.replaceAll("(\\p{javaLetterOrDigit})(\\p{javaUpperCase}\\p{javaLowerCase})","$1_$2")
                .replaceAll("(\\p{javaLowerCase})(\\p{javaUpperCase})","$1_$2")
                .toLowerCase(Locale.ENGLISH);
    }
}
