package org.kohsuke.stapler.interceptor;

import io.jenkins.servlet.ServletExceptionWrapper;
import jakarta.servlet.ServletException;
import java.lang.reflect.InvocationTargetException;
import org.kohsuke.stapler.CancelRequestHandlingException;
import org.kohsuke.stapler.Function;
import org.kohsuke.stapler.HttpResponses;
import org.kohsuke.stapler.ReflectionUtils;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerRequest2;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.StaplerResponse2;

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
     * @throws CancelRequestHandlingException
     *      to cancel this request handling and moves on to the next available dispatch mechanism.
     */
    public /* abstract */ Object invoke(
            StaplerRequest2 request, StaplerResponse2 response, Object instance, Object[] arguments)
            throws IllegalAccessException, InvocationTargetException, ServletException {
        if (ReflectionUtils.isOverridden(
                Interceptor.class,
                getClass(),
                "invoke",
                StaplerRequest.class,
                StaplerResponse.class,
                Object.class,
                Object[].class)) {
            try {
                return invoke(
                        StaplerRequest.fromStaplerRequest2(request),
                        StaplerResponse.fromStaplerResponse2(response),
                        instance,
                        arguments);
            } catch (javax.servlet.ServletException e) {
                throw ServletExceptionWrapper.toJakartaServletException(e);
            }
        } else {
            throw new AbstractMethodError("The class " + getClass().getName() + " must override at least one of the "
                    + Interceptor.class.getSimpleName() + ".invoke methods");
        }
    }

    /**
     * @deprecated use {@link #invoke(StaplerRequest2, StaplerResponse2, Object, Object[])}
     */
    @Deprecated
    public Object invoke(StaplerRequest request, StaplerResponse response, Object instance, Object[] arguments)
            throws IllegalAccessException, InvocationTargetException, javax.servlet.ServletException {
        if (ReflectionUtils.isOverridden(
                Interceptor.class,
                getClass(),
                "invoke",
                StaplerRequest2.class,
                StaplerResponse2.class,
                Object.class,
                Object[].class)) {
            try {
                return invoke(
                        StaplerRequest.toStaplerRequest2(request),
                        StaplerResponse.toStaplerResponse2(response),
                        instance,
                        arguments);
            } catch (ServletException e) {
                throw ServletExceptionWrapper.fromJakartaServletException(e);
            }
        } else {
            throw new AbstractMethodError("The class " + getClass().getName() + " must override at least one of the "
                    + Interceptor.class.getSimpleName() + ".invoke methods");
        }
    }
}
