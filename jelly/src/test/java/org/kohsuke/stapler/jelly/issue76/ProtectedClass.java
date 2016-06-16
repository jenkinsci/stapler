package org.kohsuke.stapler.jelly.issue76;

import org.kohsuke.stapler.ForwardingFunction;
import org.kohsuke.stapler.Function;
import org.kohsuke.stapler.FunctionList;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.lang.FieldRef;
import org.kohsuke.stapler.lang.Klass;
import org.kohsuke.stapler.lang.KlassNavigator;
import org.kohsuke.stapler.lang.MethodRef;
import org.kohsuke.stapler.lang.util.FieldRefFilter;

import javax.servlet.ServletException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static javafx.scene.input.KeyCode.F;

/**
 * Used as 'C' of {@code Klass<C>} to represents a protected version of a {@link Class}.
 *
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

        private Object wrap(Object instance) {
            if (instance==null) return null;
            return new Protection(instance);
        }

        @Override
        public List<Function> getFunctions(ProtectedClass clazz) {
            List<Function> r = new ArrayList<Function>();
            // insert this at the top to make sure that shadows doIndex in subtypes
            r.addAll(new FunctionList(JAVA.getFunctions(Protection.class)).name("doIndex"));
            // expose all the functions from the base type
            for (Function f : JAVA.getFunctions(clazz.c)) {
                r.add(protect(f));
            }
            return r;
        }

        private Function protect(Function f) {
            return new ForwardingFunction(f) {
                @Override
                public Object invoke(StaplerRequest req, StaplerResponse rsp, Object o, Object... args) throws IllegalAccessException, InvocationTargetException, ServletException {
                    return wrap(super.invoke(req, rsp, unwrap(o), args));
                }
            };
        }
    };
}
