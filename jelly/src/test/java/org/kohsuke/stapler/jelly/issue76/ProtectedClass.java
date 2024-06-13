package org.kohsuke.stapler.jelly.issue76;

import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletException;
import org.kohsuke.stapler.ForwardingFunction;
import org.kohsuke.stapler.Function;
import org.kohsuke.stapler.FunctionList;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.TraversalMethodContext;
import org.kohsuke.stapler.lang.FieldRef;
import org.kohsuke.stapler.lang.Klass;
import org.kohsuke.stapler.lang.KlassNavigator;
import org.kohsuke.stapler.lang.MethodRef;
import org.kohsuke.stapler.lang.util.FieldRefFilter;

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

    public static KlassNavigator<ProtectedClass> NAVIGATOR = new KlassNavigator<>() {

        @Override
        public boolean isArray(ProtectedClass clazz) {
            return JAVA.isArray(clazz.c);
        }

        @Override
        public Object getArrayElement(Object o, int index) throws IndexOutOfBoundsException {
            return w(JAVA.getArrayElement(u(o), index));
        }

        @Override
        public boolean isMap(ProtectedClass clazz) {
            return JAVA.isMap(clazz.c);
        }

        @Override
        public Object getMapElement(Object o, String key) {
            return w(JAVA.getMapElement(u(o), key));
        }

        private Klass<ProtectedClass> protect(Klass<? /*should be Class*/> c) {
            if (c == null) {
                return null;
            }
            return new Klass<>(new ProtectedClass((Class) c.clazz), NAVIGATOR);
        }

        // no view
        @Override
        public URL getResource(ProtectedClass clazz, String resourceName) {
            return null;
        }

        @Override
        public Iterable<Klass<?>> getAncestors(ProtectedClass clazz) {
            List<Klass<?>> r = new ArrayList<>();
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
            List<FieldRef> r = new ArrayList<>();
            for (final FieldRef f : JAVA.getDeclaredFields(clazz.c)) {
                r.add(new FieldRefFilter() {
                    @Override
                    protected FieldRef getBase() {
                        return f;
                    }

                    @Override
                    public String getSignature() {
                        return f.getQualifiedName(); // doesn't really matter
                    }

                    @Override
                    public Object get(Object instance) throws IllegalAccessException {
                        // as we route requests, keep protecting objects
                        return w(super.get(u(instance)));
                    }

                    @Override
                    public Class<?> getReturnType() {
                        return f.getReturnType();
                    }
                });
            }
            return r;
        }

        private Object u(Object instance) {
            if (instance == null) {
                return null;
            }
            return ((Protection) instance).o;
        }

        private Protection w(Object instance) {
            if (instance == null) {
                return null;
            }
            return new Protection(instance);
        }

        @Override
        public List<Function> getFunctions(ProtectedClass clazz) {
            // insert this at the top to make sure that shadows doIndex in subtypes
            List<Function> r = new ArrayList<>(new FunctionList(JAVA.getFunctions(Protection.class)).name("doIndex"));
            // expose all the functions from the base type
            for (Function f : JAVA.getFunctions(clazz.c)) {
                r.add(protect(f));
            }
            return r;
        }

        /**
         * Decorates {@link Function} so that it can be invoked on {@link Protection} and the
         * return value gets protected as well.
         *
         * <p>
         * If the function is used for object traversal, the return value needs to be wrapped to {@link Protection}.
         */
        private Function protect(Function f) {
            final Function traversal = new ForwardingFunction(f) {
                @Override
                public Object invoke(StaplerRequest req, StaplerResponse rsp, Object o, Object... args)
                        throws IllegalAccessException, InvocationTargetException, ServletException {
                    return w(super.invoke(req, rsp, u(o), args));
                }
            };
            final Function service = new ForwardingFunction(f) {
                @Override
                public Function contextualize(Object usage) {
                    if (usage instanceof TraversalMethodContext) {
                        return traversal;
                    } else {
                        return super.contextualize(usage);
                    }
                }

                @Override
                public Object invoke(StaplerRequest req, StaplerResponse rsp, Object o, Object... args)
                        throws IllegalAccessException, InvocationTargetException, ServletException {
                    return super.invoke(req, rsp, u(o), args);
                }
            };

            return service;
        }
    };
}
