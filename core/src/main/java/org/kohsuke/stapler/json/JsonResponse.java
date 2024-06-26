package org.kohsuke.stapler.json;

import jakarta.servlet.ServletException;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.StaplerRequest2;
import org.kohsuke.stapler.StaplerResponse2;
import org.kohsuke.stapler.interceptor.Interceptor;
import org.kohsuke.stapler.interceptor.InterceptorAnnotation;

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
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@InterceptorAnnotation(JsonResponse.Handler.class)
public @interface JsonResponse {
    class Handler extends Interceptor {
        private static final Logger logger = Logger.getLogger(Handler.class.getName());

        @Override
        public Object invoke(StaplerRequest2 request, StaplerResponse2 response, Object instance, Object[] arguments)
                throws IllegalAccessException, InvocationTargetException, ServletException {
            try {
                final Object r = target.invoke(request, response, instance, arguments);

                JSONObject j;
                if (r == null) {
                    j = null;
                } else if (r instanceof JSONObject) {
                    j = (JSONObject) r;
                } else {
                    // will fail in case of Array/List, please keep this behavior
                    // to prevent top-level json array that has a vulnerability in certain browser
                    // http://blog.jeremiahgrossman.com/2006/01/advanced-web-attack-techniques-using.html
                    j = JSONObject.fromObject(r);
                }

                return new JsonHttpResponse(j);
            } catch (InvocationTargetException e) {
                logger.log(Level.SEVERE, "Error processing request", e);
                Throwable target = e.getTargetException();
                if (target instanceof HttpResponse) {
                    return target;
                }
                return new JsonHttpResponse(target, 500);
            }
        }
    }
}
