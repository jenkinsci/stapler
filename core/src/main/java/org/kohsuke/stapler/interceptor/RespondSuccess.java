package org.kohsuke.stapler.interceptor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationTargetException;
import javax.servlet.ServletException;
import org.kohsuke.stapler.HttpResponses;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

/**
 * Used on the web-bound doXyz method to indicate that the successful return of the method should
 * result in HTTP 200 Success status.
 *
 * @author Kohsuke Kawaguchi
 * @see HttpResponses#ok()
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD,ElementType.FIELD})
@InterceptorAnnotation(RespondSuccess.Processor.class)
public @interface RespondSuccess {
    class Processor extends Interceptor {
        @Override
        public Object invoke(StaplerRequest request, StaplerResponse response, Object instance, Object[] arguments)
                throws IllegalAccessException, InvocationTargetException, ServletException {
            target.invoke(request, response, instance, arguments);
            // TODO does this actually do anything?
            // Function.bindAndInvokeAndServeResponse ignores return value if the method is declared to return void.
            // And it seems Stapler will send a 200 by default anyway.
            return HttpResponses.ok();
        }
    }
}
