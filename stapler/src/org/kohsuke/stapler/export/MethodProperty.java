package org.kohsuke.stapler.export;

import java.beans.Introspector;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * {@link Property} based on {@link Method}.
 * @author Kohsuke Kawaguchi
 */
final class MethodProperty extends Property {
    private final Method method;
    MethodProperty(Parser owner, Method m, Exported exported) {
        super(owner,buildName(m.getName()), exported);
        this.method = m;
    }

    private static String buildName(String name) {
        if(name.startsWith("get"))
            name = name.substring(3);
        else
        if(name.startsWith("is"))
            name = name.substring(2);

        return Introspector.decapitalize(name);
    }


    protected Object getValue(Object object) throws IllegalAccessException, InvocationTargetException {
        return method.invoke(object);
    }
}
