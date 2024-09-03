package org.kohsuke.stapler;

import jakarta.servlet.ServletException;
import java.lang.reflect.InvocationTargetException;
import org.kohsuke.stapler.interceptor.Interceptor;
import org.kohsuke.stapler.interceptor.InterceptorAnnotation;

/**
 * {@link Function} that uses {@link Interceptor} for method selection phase.
 *
 * @see InterceptorAnnotation
 * @author Kohsuke Kawaguchi
 */
/*package*/ class SelectionInterceptedFunction extends ForwardingFunction {
    private final Interceptor interceptor;

    /*package*/ SelectionInterceptedFunction(Function next, Interceptor i) {
        super(next);
        this.interceptor = i;
        interceptor.setTarget(new Adapter(next));
    }

    @Override
    Object bindAndInvoke(Object o, StaplerRequest2 req, StaplerResponse2 rsp, Object... headArgs)
            throws IllegalAccessException, InvocationTargetException, ServletException {
        return interceptor.invoke(req, rsp, o, headArgs);
    }

    private static final class Adapter extends ForwardingFunction {
        Adapter(Function next) {
            super(next);
        }

        @Override
        public Object invoke(StaplerRequest2 req, StaplerResponse2 rsp, Object o, Object... args)
                throws IllegalAccessException, InvocationTargetException, ServletException {
            return next.bindAndInvoke(o, req, rsp, args);
        }
    }
}
