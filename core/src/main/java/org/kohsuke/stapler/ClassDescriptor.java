package org.kohsuke.stapler;

import org.apache.commons.io.IOUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.EmptyVisitor;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static java.util.logging.Level.FINE;
import static java.util.logging.Level.WARNING;

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

        // debug information, if present, is more trustworthy
        try {
            return ASM.loadParametersFromAsm(m);
        } catch (LinkageError e) {
            LOGGER.log(FINE, "Incompatible ASM", e);
        } catch (IOException e) {
            LOGGER.log(WARNING, "Failed to load a class file", e);
        }

        // otherwise check the .stapler file
        Class<?> c = m.getDeclaringClass();
        URL url = c.getClassLoader().getResource(
                    c.getName().replace('.', '/').replace('$','/') + '/' + m.getName() + ".stapler");
        if(url!=null) {
            try {
                return IOUtils.toString(url.openStream()).split(",");
            } catch (IOException e) {
                LOGGER.log(WARNING, "Failed to load "+url,e);
                return EMPTY_ARRAY;
            }
        }

        // couldn't find it
        return EMPTY_ARRAY;
    }

    /**
     * 
     */
    private static class ASM {
        /**
         * Try to load parameter names from the debug info by using ASM.
         */
        private static String[] loadParametersFromAsm(final Method m) throws IOException {
            Class<?> c = m.getDeclaringClass();
            URL clazz = c.getClassLoader().getResource(c.getName().replace('.', '/').replace('$', '/') + ".class");
            if (clazz==null)    return EMPTY_ARRAY;

            final String[] paramNames = new String[m.getParameterTypes().length];

            ClassReader r = new ClassReader(clazz.openStream());
            r.accept(new EmptyVisitor() {
                final String md = Type.getMethodDescriptor(m);
                public MethodVisitor visitMethod(int access, String methodName, String desc, String signature, String[] exceptions) {
                    if (methodName.equals(m.getName())  && desc.equals(md))
                        return new EmptyVisitor() {
                            public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
                                if (index!=0 && index<=paramNames.length) {
                                    paramNames[index-1] = name;
                                }
                            }
                        };
                    else
                        return this; // ignore this method
                }
            }, false);

            return paramNames;
        }
    }

    private static final Logger LOGGER = Logger.getLogger(ClassDescriptor.class.getName());
    private static final String[] EMPTY_ARRAY = new String[0];
}
