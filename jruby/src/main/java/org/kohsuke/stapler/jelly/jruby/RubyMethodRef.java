package org.kohsuke.stapler.jelly.jruby;

import org.jruby.RubyModule;
import org.jruby.internal.runtime.methods.DynamicMethod;
import org.jruby.javasupport.Java;
import org.jruby.runtime.builtin.IRubyObject;
import org.kohsuke.stapler.lang.MethodRef;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * @author Kohsuke Kawaguchi
 */
public class RubyMethodRef extends MethodRef {
    @NonNull
    private final RubyModule klass;
    @NonNull
    private final DynamicMethod method;


    public RubyMethodRef(@NonNull RubyModule klass, @NonNull DynamicMethod method) {
        this.klass = klass;
        this.method = method;
    }

    /**
     * Retrieves the Ruby module (aka class), for which the method is declared.
     * @return Ruby module, which stores the method reference
     * @since 1.248
     */
    @NonNull
    public RubyModule getKlass() {
        return klass;
    }
    
    /**
     * Retrieves the referenced method.
     * @return Referenced method
     * @since 1.248
     */
    @NonNull
    public DynamicMethod getMethod() {
        return method;
    }

    @Override
    public String getName() {
        return method.getName();
    }
    
    @Override
    public <T extends Annotation> T getAnnotation(Class<T> type) {
        // TODO: what's the equivalent in JRuby?
        return null;
    }

    @Override
    public Object invoke(Object _this, Object... args) throws InvocationTargetException, IllegalAccessException {
        IRubyObject[] argList = new IRubyObject[args.length];
        for (int i=0; i<args.length; i++)
            argList[i] = Java.getInstance(klass.getRuntime(),args[i]);
        return method.call(klass.getRuntime().getCurrentContext() , (IRubyObject)_this, klass, method.getName(), argList);
    }
}
