package org.kohsuke.stapler.export;

import java.beans.Introspector;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.io.IOException;

/**
 * {@link Property} based on {@link Method}.
 * @author Kohsuke Kawaguchi
 */
final class MethodProperty extends Property {
    private final Method method;
    MethodProperty(Model owner, Method m, Exported exported) {
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

    public Type getGenericType() {
        return method.getGenericReturnType();
    }

    public Class getType() {
        return method.getReturnType();
    }

    public String getJavadoc() {
        return parent.getJavadoc().getProperty(method.getName()+"()");
    }

    protected Object getValue(Object object) throws IllegalAccessException, InvocationTargetException {
        return method.invoke(object);
    }
}
