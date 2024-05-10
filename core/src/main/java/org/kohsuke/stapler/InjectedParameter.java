package org.kohsuke.stapler;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used on annotations to indicate that it signals a parameter injection in web-bound "doXyz" methods.
 *
 * @author Kohsuke Kawaguchi
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
@Documented
public @interface InjectedParameter {
    /**
     * Code that computes the actual value to inject.
     *
     * One instance of this is created lazily and reused concurrently.
     */
    Class<? extends AnnotationHandler> value();
}
