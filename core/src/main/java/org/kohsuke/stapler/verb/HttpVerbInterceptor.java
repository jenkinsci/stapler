package org.kohsuke.stapler.verb;

import org.kohsuke.stapler.CancelRequestHandlingException;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.interceptor.Interceptor;
import org.kohsuke.stapler.interceptor.InterceptorAnnotation;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;

/**
 * Restricts the routing to matching HTTP verbs.
 *
 * <h2>Usage</h2>
 * <p>
 * This package defines a number of HTTP verb (method) annotations that can be used to restrict
 * routing. For example,
 *
 * <pre>
 * &#64;WebMethod(name="") &#64;DELETE
 * public void delete() {
 *     // this method will be invoked only when the request is DELETE
 *     ...
 * }
 *
 * &#64;WebMethod(name="") &#64;POST
 * public void create(&#64;JsonBody Order order) {
 *     // this method will be invoked only when the request is POST
 *     ...
 * }
 * </pre>
 *
 * <p>
 * This class is the actual logic that implements this semantics on top of {@link Interceptor}.
 *
 * @author Kohsuke Kawaguchi
 * @see GET
 * @see POST
 * @see PUT
 * @see DELETE
 */
public class HttpVerbInterceptor extends Interceptor {
    @Override
    public Object invoke(StaplerRequest request, StaplerResponse response, Object instance, Object[] arguments) throws IllegalAccessException, InvocationTargetException {
        if (matches(request))
            return target.invoke(request,response,instance,arguments);
        else
            throw new CancelRequestHandlingException();
    }

    private boolean matches(StaplerRequest request) {
        String method = request.getMethod();

        for (Annotation a : target.getAnnotations()) {
            Class<? extends Annotation> t = a.annotationType();
            InterceptorAnnotation ia = t.getAnnotation(InterceptorAnnotation.class);
            if (ia !=null && ia.value()==HttpVerbInterceptor.class) {
                if (t.getName().endsWith(method))
                    return true;
            }
        }
        return false;
    }
}
