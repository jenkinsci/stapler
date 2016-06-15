package org.kohsuke.stapler.jelly.issue76;

import org.kohsuke.stapler.Function;
import org.kohsuke.stapler.lang.FieldRef;
import org.kohsuke.stapler.lang.Klass;
import org.kohsuke.stapler.lang.KlassNavigator;
import org.kohsuke.stapler.lang.MethodRef;
import org.kohsuke.stapler.lang.util.FieldRefFilter;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Kohsuke Kawaguchi
 */
public class ProtectedClass {
    private final Class c;

    public ProtectedClass(Class c) {
        this.c = c;
    }

    public static KlassNavigator<ProtectedClass> NAVIGATOR = new KlassNavigator<ProtectedClass>() {
        private Klass<ProtectedClass> protect(Klass<?/*should be Class*/> c) {
            if (c==null)    return null;
            return new Klass<ProtectedClass>(new ProtectedClass((Class)c.clazz), NAVIGATOR);
        }

        // no view
        @Override
        public URL getResource(ProtectedClass clazz, String resourceName) {
            return null;
        }

        @Override
        public Iterable<Klass<?>> getAncestors(ProtectedClass clazz) {
            List<Klass<?>> r = new ArrayList<Klass<?>>();
            for (Klass<?> c : JAVA.getAncestors(clazz.c)) {
                r.add(protect(c));
            }
            return r;
        }

        @Override
        public Klass<ProtectedClass> getSuperClass(ProtectedClass clazz) {
            return protect(JAVA.getSuperClass(clazz.c));
        }

        @Override
        public Class toJavaClass(ProtectedClass clazz) {
            return ProtectedClass.class;
        }

        @Override
        public List<MethodRef> getDeclaredMethods(ProtectedClass clazz) {
            return JAVA.getDeclaredMethods(clazz.c);
        }

        @Override
        public List<FieldRef> getDeclaredFields(ProtectedClass clazz) {
            List<FieldRef> r = new ArrayList<FieldRef>();
            for (final FieldRef f : JAVA.getDeclaredFields(clazz.c)) {
                r.add(new FieldRefFilter() {
                    @Override
                    protected FieldRef getBase() {
                        return f;
                    }

                    @Override
                    public Object get(Object instance) throws IllegalAccessException {
                        // as we route requests, keep protecting objects
                        Object o = super.get(unwrap(instance));
                        if (o!=null)
                            o = new Protection(o);
                        return o;
                    }
                });
            }
            return r;
        }

        private Object unwrap(Object instance) {
            if (instance==null) return null;
            return ((Protection)instance).o;
        }

        @Override
        public List<Function> getFunctions(ProtectedClass clazz) {
            return JAVA.getFunctions(clazz.c);
        }
    };
}
