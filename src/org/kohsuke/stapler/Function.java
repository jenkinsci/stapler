package org.kohsuke.stapler;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

/**
 * Abstracts the difference between normal instance methods and
 * static duck-typed methods.
 *
 * @author Kohsuke Kawaguchi
 */
interface Function {
    String getName();
    Class[] getParameterTypes();
    Object invoke(Object o, Object... args) throws IllegalAccessException, InvocationTargetException;

    /**
     * Normal instance methods.
     */
    final class InstanceFunction implements Function {
        private final Method m;

        public InstanceFunction(Method m) {
            this.m = m;
        }

        public String getName() {
            return m.getName();
        }

        public Class[] getParameterTypes() {
            return m.getParameterTypes();
        }

        public Object invoke(Object o, Object... args) throws IllegalAccessException, InvocationTargetException {
            return m.invoke(o,args);
        }
    }

    /**
     * Static methods on the wrapper type.
     */
    final class StaticFunction implements Function {
        private final Method m;

        public StaticFunction(Method m) {
            this.m = m;
        }

        public String getName() {
            return m.getName();
        }

        public Class[] getParameterTypes() {
            Class[] p = m.getParameterTypes();
            Class[] r = new Class[p.length-1];
            System.arraycopy(p,1,r,0,r.length);
            return r;
        }

        public Object invoke(Object o, Object... args) throws IllegalAccessException, InvocationTargetException {
            Object[] r = new Object[args.length+1];
            r[0] = o;
            System.arraycopy(args,0,r,1,args.length);
            return m.invoke(null,r);
        }
    }
}
