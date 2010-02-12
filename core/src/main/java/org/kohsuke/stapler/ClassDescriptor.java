package org.kohsuke.stapler;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Reflection information of a {@link Class}.
 *
 * @author Kohsuke Kawaguchi
 */
public final class ClassDescriptor {
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
            functions.add(new Function.InstanceFunction(m).protectBy(m));
        }
        if(wrappers!=null) {
            for (Class w : wrappers) {
                for (Method m : w.getMethods()) {
                    if(!Modifier.isStatic(m.getModifiers()))
                        continue;
                    Class<?>[] p = m.getParameterTypes();
                    if(p.length==0)
                        continue;
                    if(p[0].isAssignableFrom(clazz))
                        continue;
                    functions.add(new Function.StaticFunction(m).protectBy(m));
                }
            }
        }
        this.methods = new FunctionList(functions);
    }

    /**
     * Loads the list of parameter names of the given method, by using a stapler-specific way of getting it.
     *
     * <p>
     * This is not the best place to expose this, but for now this would do.
     */
    public static String[] loadParameterNames(Method m) {
        CapturedParameterNames cpn = m.getAnnotation(CapturedParameterNames.class);
        if(cpn!=null)   return cpn.value();

        // otherwise check the .stapler file
        Class<?> c = m.getDeclaringClass();
            URL url = c.getClassLoader().getResource(
                    c.getName().replace('.', '/').replace('$','/') + '/' + m.getName() + ".stapler");
            if(url==null)    return EMPTY_ARRAY;
        try {
            return IOUtils.toString(url.openStream()).split(",");
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Failed to load "+url,e);
            return EMPTY_ARRAY;
        }
    }

    private static final Logger LOGGER = Logger.getLogger(ClassDescriptor.class.getName());
    private static final String[] EMPTY_ARRAY = new String[0];
}
