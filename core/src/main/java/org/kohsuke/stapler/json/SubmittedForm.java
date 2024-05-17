package org.kohsuke.stapler.json;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.servlet.ServletException;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.AnnotationHandler;
import org.kohsuke.stapler.InjectedParameter;
import org.kohsuke.stapler.StaplerRequest;

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
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@InjectedParameter(SubmittedForm.Handler.class)
public @interface SubmittedForm {
    class Handler extends AnnotationHandler {
        @Override
        public Object parse(StaplerRequest request, Annotation a, Class type, String parameterName)
                throws ServletException {
            return request.getSubmittedForm();
        }
    }
}
