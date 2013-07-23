package org.kohsuke.stapler.interceptor;

import org.kohsuke.stapler.Function;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import java.lang.reflect.InvocationTargetException;
import org.kohsuke.stapler.HttpResponses;

/**
 * Intercepts the domain method call from Stapler.
 *
 * @author Kohsuke Kawaguchi
 * @see InterceptorAnnotation
 */
public abstract class Interceptor {
    protected Function target;

    /**
     * Called by Stapler to set up the target of the interceptor.
     * This function object represents a method on which your annotation is placed.
     * 
     * This happens once before this instance takes any calls.
     */
    public void setTarget(Function target) {
        this.target = target;
    }

    /**
     * Intercepts the call.
     *
     * <p>
     * The minimal no-op interceptor would do {@code target.invoke(request,response,instance,arguments)},
     * but the implementation is free to do additional pre/post processing.
     *
     * @param request
     *      The current request we are processing.
     * @param response
     *      The current response object.
     * @param instance
     *      The domain object instance whose method we are about to invoke.
     * @param arguments
     *      Arguments of the method call.
     *
     * @return
     *      Return value from the method.
     * @throws InvocationTargetException if you want to send e.g. something from {@link HttpResponses}
     */
    public abstract Object invoke(StaplerRequest request, StaplerResponse response,
                                  Object instance, Object[] arguments)
            throws IllegalAccessException, InvocationTargetException;
}
