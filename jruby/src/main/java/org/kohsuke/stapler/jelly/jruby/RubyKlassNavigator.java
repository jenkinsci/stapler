package org.kohsuke.stapler.jelly.jruby;

import org.jruby.Ruby;
import org.jruby.RubyClass;
import org.jruby.RubyModule;
import org.jruby.RubyObject;
import org.jruby.internal.runtime.methods.DynamicMethod;
import org.kohsuke.stapler.ClassDescriptor;
import org.kohsuke.stapler.Function;
import org.kohsuke.stapler.MetaClassLoader;
import org.kohsuke.stapler.lang.FieldRef;
import org.kohsuke.stapler.lang.Klass;
import org.kohsuke.stapler.lang.KlassNavigator;
import org.kohsuke.stapler.lang.MethodRef;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * {@link KlassNavigator} implementation for JRuby.
 *
 * @author Kohsuke Kawaguchi
 */
public class RubyKlassNavigator extends KlassNavigator<RubyModule> {
    private final Ruby ruby;
    /**
     * ClassLoader to load resources from.
     */
    private final ClassLoader classLoader;

    public RubyKlassNavigator(Ruby ruby, ClassLoader cl) {
        this.ruby = ruby;
        this.classLoader = cl;
    }
    

    @Override
    public URL getResource(RubyModule clazz, String resourceName) {
        String fullName;
        if (resourceName.startsWith("/"))
            fullName = resourceName.substring(1);
        else
            fullName = decamelize(clazz.getName().replace("::","/")+'/'+resourceName);

        if (MetaClassLoader.debugLoader!=null) {
            URL res = MetaClassLoader.debugLoader.loader.getResource(fullName);
            if (res!=null)  return res;
        }
        return classLoader.getResource(fullName);
    }

    @Override
    public Iterable<Klass<?>> getAncestors(RubyModule clazz) {
        List<Klass<?>> r = new ArrayList<>();
        for (RubyModule anc : (List<RubyModule>)(List)clazz.getAncestorList()) {
            r.add(wrap(anc));
        }
        return r;
    }

    @Override
    public Klass<?> getSuperClass(RubyModule clazz) {
        // TODO: what happens when a Ruby class extends from Java class?
        return wrap(clazz.getSuperClass());
    }

    @Override
    public List<MethodRef> getDeclaredMethods(RubyModule clazz) {
        List<MethodRef> r = new ArrayList<>();
        for (DynamicMethod m : clazz.getMethods().values()) {
            // TODO: not sure if this is entirely correct
            if (m.getImplementationClass()==clazz)
                r.add(new RubyMethodRef(clazz,m));
        }
        return r;
    }

    @Override
    public List<FieldRef> getDeclaredFields(RubyModule clazz) {
        // IIUC, Ruby doesn't have statically defined instance fields
        return Collections.emptyList();
    }

    @Override
    public List<Function> getFunctions(RubyModule clazz) {
        // implemented as a fallback to Java through reified class, but maybe there's a better way to do this
        return new ClassDescriptor(toJavaClass(clazz)).methods;
    }

    @Override
    public Class toJavaClass(RubyModule clazz) {
        if (clazz instanceof RubyClass) {
            RubyClass rc = (RubyClass) clazz;
            Class c = rc.getReifiedClass();
            if (c!=null)    return c;   // is this right?
        }
        return RubyObject.class;
    }
    
    public Klass<RubyModule> wrap(RubyModule m) {
        return m==null ? null : new Klass<>(m,this);
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
