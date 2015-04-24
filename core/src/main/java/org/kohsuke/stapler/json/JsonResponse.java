package org.kohsuke.stapler.json;

import net.sf.json.JSONObject;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.interceptor.Interceptor;
import org.kohsuke.stapler.interceptor.InterceptorAnnotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Used for web methods that return POJO that should be sent across as JSON.
 *
 * <p>
 * See {@link JsonBody} for more comprehensive documentation.
 *
 * @author Kohsuke Kawaguchi
 * @author Carlos Sanchez
 * @see JsonBody
 */
@Target(METHOD)
@Retention(RUNTIME)
@Documented
@InterceptorAnnotation(JsonResponse.Handler.class)
public @interface JsonResponse {
    public static class Handler extends Interceptor {
        private static final Logger logger = Logger.getLogger(Handler.class.getName());

        @Override
        public Object invoke(StaplerRequest request, StaplerResponse response, Object instance, Object[] arguments)
                throws IllegalAccessException, InvocationTargetException {
            try {
                final Object r = target.invoke(request, response, instance, arguments);

                JSONObject j;
                if (r==null)
                    j = null;
                else
                if (r instanceof JSONObject)
                    j = (JSONObject)r;
                else
                    j = JSONObject.fromObject(r);

                return new JsonHttpResponse(j);
            } catch (InvocationTargetException e) {
                logger.log(Level.SEVERE, "Error processing request", e);
                Throwable target = e.getTargetException();
                if (target instanceof HttpResponse)
                    return target;
                return new JsonHttpResponse(target, 500);
            }
        }
    }

}
