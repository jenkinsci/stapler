package org.kohsuke.stapler;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.ArrayList;

/**
 * Reflection information of a {@link Class}.
 *
 * @author Kohsuke Kawaguchi
 */
final class ClassDescriptor {
    public final Class clazz;
    public final FunctionList methods;
    public final Field[] fields;

    /**
     * @param clazz
     *      The class to build a descriptor around.
     * @param wrappers
     *      Optional wrapper duck-typing classes.
     *      Static methods on this class that has the first parameter
     *      as 'clazz' will be handled as if it's instance methods on
     *      'clazz'. Useful for adding view/controller methods on
     *      model classes.
     */
    public ClassDescriptor(Class clazz, Class... wrappers) {
        this.clazz = clazz;
        this.fields = clazz.getFields();

        // instance methods
        List<Function> functions = new ArrayList<Function>();
        for (Method m : clazz.getMethods()) {
            functions.add(new Function.InstanceFunction(m));
        }
        for (Class w : wrappers) {
            for (Method m : w.getMethods()) {
                if(!Modifier.isStatic(m.getModifiers()))
                    continue;
                Class<?>[] p = m.getParameterTypes();
                if(p.length==0)
                    continue;
                if(p[0].isAssignableFrom(clazz))
                    continue;
                functions.add(new Function.StaticFunction(m));
            }
        }
        this.methods = new FunctionList(functions);
    }
}
