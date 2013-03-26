package org.kohsuke.stapler;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Used on annotations to indicate that it signals a parameter injection in web-bound "doXyz" methods.
 *
 * @author Kohsuke Kawaguchi
 */
@Retention(RUNTIME)
@Target(ANNOTATION_TYPE)
@Documented
public @interface InjectedParameter {
    /**
     * Code that computes the actual value to inject.
     *
     * One instance of this is created lazily and reused concurrently.
     */
    Class<? extends AnnotationHandler> value();
}
