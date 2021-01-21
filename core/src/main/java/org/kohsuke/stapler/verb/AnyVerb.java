package org.kohsuke.stapler.verb;

import org.kohsuke.stapler.WebMethod;
import org.kohsuke.stapler.interceptor.InterceptorAnnotation;
import org.kohsuke.stapler.interceptor.Stage;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Explicitly allows any HTTP verb for {@link WebMethod}. This isn't just a
 * no-op in case it would be combined with a more restrictive verb, but
 * conceptually identical to not have any annotation, but friendlier for static
 * analysis.
 *
 */
@Target(METHOD)
@Retention(RUNTIME)
@Documented
@InterceptorAnnotation(value = HttpVerbInterceptor.class, stage = Stage.SELECTION)
public @interface AnyVerb {
}
