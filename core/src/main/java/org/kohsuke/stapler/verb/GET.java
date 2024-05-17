package org.kohsuke.stapler.verb;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.kohsuke.stapler.WebMethod;
import org.kohsuke.stapler.interceptor.InterceptorAnnotation;
import org.kohsuke.stapler.interceptor.Stage;

/**
 * Restricts a {@link WebMethod} to a specific HTTP method 'GET'.
 *
 * @author Kohsuke Kawaguchi
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@InterceptorAnnotation(value = HttpVerbInterceptor.class, stage = Stage.SELECTION)
public @interface GET {}
