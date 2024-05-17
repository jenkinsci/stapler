package org.kohsuke.stapler;

import java.lang.reflect.InvocationTargetException;
import javax.servlet.ServletException;
import org.kohsuke.stapler.interceptor.Interceptor;
import org.kohsuke.stapler.interceptor.InterceptorAnnotation;
import org.kohsuke.stapler.interceptor.Stage;

/**
 * Function that's wrapped by {@link Interceptor} for {@link Stage#PREINVOKE}
 *
 * @see InterceptorAnnotation
 */
final class PreInvokeInterceptedFunction extends ForwardingFunction {
    private final Interceptor interceptor;

    /*package*/ PreInvokeInterceptedFunction(Function next, Interceptor i) {
        super(next);
        this.interceptor = i;
        interceptor.setTarget(next);
    }

    @Override
    public Object invoke(StaplerRequest req, StaplerResponse rsp, Object o, Object... args)
            throws IllegalAccessException, InvocationTargetException, ServletException {
        return interceptor.invoke(req, rsp, o, args);
    }

    @Override
    public Function contextualize(Object usage) {
        Function f = next.contextualize(usage);
        if (f == next) {
            return this; // the base function didn't care
        }
        return new PreInvokeInterceptedFunction(f, interceptor);
    }
}
