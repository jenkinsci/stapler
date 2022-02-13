package org.kohsuke.stapler.json;

import net.sf.json.JSONObject;
import org.kohsuke.stapler.AnnotationHandler;
import org.kohsuke.stapler.InjectedParameter;
import org.kohsuke.stapler.StaplerRequest;

import javax.servlet.ServletException;
import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

/**
 * Binds {@linkplain StaplerRequest#getSubmittedForm() the submitted form} to a parameter of a web-bound method.
 *
 * <p>
 * On a web-bound {@code doXyz} method, use this annotation on a parameter to get the submitted
 * structured form content and inject it as {@link JSONObject}.
 * For example,
 *
 * <pre>
 * public HttpResponse doConfigSubmit(@SubmittedForm JSONObject o) {
 *   ...
 * }
 * </pre>
 *
 * @author Kohsuke Kawaguchi
 */
@Target(PARAMETER)
@Retention(RUNTIME)
@Documented
@InjectedParameter(SubmittedForm.Handler.class)
public @interface SubmittedForm {
    class Handler extends AnnotationHandler {
        @Override
        public Object parse(StaplerRequest request, Annotation a, Class type, String parameterName) throws ServletException {
            return request.getSubmittedForm();
        }
    }
}
