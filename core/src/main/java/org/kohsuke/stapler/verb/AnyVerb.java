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
 * Explicitly allows any HTTP verb for {@link WebMethod}.
 * While conceptually similar to not have any annotation, it's friendlier for static analysis.
 * This isn't a no-op only in case it would be combined with a more restrictive verb annotation, as it will continue to allow any verb.
 *
 */
@Target(METHOD)
@Retention(RUNTIME)
@Documented
@InterceptorAnnotation(value = HttpVerbInterceptor.class, stage = Stage.SELECTION)
public @interface AnyVerb {
}
