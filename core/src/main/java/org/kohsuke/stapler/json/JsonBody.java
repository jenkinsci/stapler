package org.kohsuke.stapler.json;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.servlet.ServletException;
import net.sf.json.JSONObject;
import org.apache.commons.io.IOUtils;
import org.kohsuke.stapler.AnnotationHandler;
import org.kohsuke.stapler.InjectedParameter;
import org.kohsuke.stapler.StaplerRequest;

/**
 * Binds the body payload into POJO via json-lib.
 *
 * <p>
 * On a web-bound {@code doXyz} method, use this method on a parameter to get the content of the request
 * data-bound to a bean through {@link JSONObject#fromObject(Object)} and inject it as a parameter.
 * For example,
 *
 * <pre>
 * &#64;JsonResponse
 * public Point doDouble(@JsonBody Point p) {
 *   Point pt = new Point();
 *   pt.x = p.x*2;
 *   pt.y = p.y*2;
 *   return pt;
 * }
 *
 * public static class Point { public int x, y; }
 * </pre>
 *
 * Request:
 * <pre>
 * POST ..../double
 * Content-Type: application/json
 *
 * {"x":10,"y":5}
 * </pre>
 *
 * Response:
 * <pre>
 * 200 OK
 * Content-Type: application/json;charset=UTF-8
 *
 * {"x":20,"y":10}
 * </pre>
 *
 * @author Kohsuke Kawaguchi
 * @see JsonResponse
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@InjectedParameter(JsonBody.Handler.class)
public @interface JsonBody {
    class Handler extends AnnotationHandler {
        @Override
        public Object parse(StaplerRequest request, Annotation a, Class type, String parameterName) throws ServletException {
            String ct = request.getContentType();
            if (ct == null || !ct.startsWith("application/json"))
                throw new ServletException("Expected application/json but got "+ct);

            try {
                // TODO: exception thrown here results in error page rendered in HTML.
                JSONObject o = JSONObject.fromObject(IOUtils.toString(request.getReader()));
                return JSONObject.toBean(o,type);
            } catch (IOException e) {
                throw new ServletException("Failed to read JSON",e);
            }
        }
    }
}
