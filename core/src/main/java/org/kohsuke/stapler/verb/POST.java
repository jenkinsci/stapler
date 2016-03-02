package org.kohsuke.stapler.verb;

import org.kohsuke.stapler.WebMethod;
import org.kohsuke.stapler.interceptor.InterceptorAnnotation;
import org.kohsuke.stapler.interceptor.RequirePOST;
import org.kohsuke.stapler.interceptor.Stage;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

/**
 * Restricts a {@link WebMethod} to a specific HTTP method 'POST'.
 *
 * <p>
 * Unlike {@link RequirePOST}, this annotation simply skips routing the current request
 * to the current web method, and continues searching other available routes.
 *
 * @author Kohsuke Kawaguchi
 */
@Target(METHOD)
@Retention(RUNTIME)
@Documented
@InterceptorAnnotation(value=HttpVerbInterceptor.class, stage= Stage.SELECTION)
public @interface POST {
}
