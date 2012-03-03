package org.kohsuke.stapler.interceptor;

import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationTargetException;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

/**
 * Requires the request to be a POST.
 *
 * @author Kohsuke Kawaguchi
 */
@Retention(RUNTIME)
@Target({METHOD,FIELD})
@InterceptorAnnotation(RequirePOST.Processor.class)
public @interface RequirePOST {
    public static class Processor extends Interceptor {
        @Override
        public Object invoke(StaplerRequest request, StaplerResponse response, Object instance, Object[] arguments)
                throws IllegalAccessException, InvocationTargetException {
            if(!request.getMethod().equals("POST"))
                throw new IllegalAccessException("POST is required for "+target.getDisplayName());
            return target.invoke(request, response, instance, arguments);
        }
    }
}
