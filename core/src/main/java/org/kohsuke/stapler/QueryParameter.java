package org.kohsuke.stapler;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.annotation.Documented;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Indicates that this parameter is injected from HTTP query parameter.
 *
 * @author Kohsuke Kawaguchi
 */
@Retention(RUNTIME)
@Target(PARAMETER)
@Documented
public @interface QueryParameter {
    /**
     * query parameter name. By default, name of the parameter.
     */
    String value() default "";

    /**
     * If true, request without this header will be rejected.
     */
    boolean required() default false;

    /**
     * If true, and the actual value of this parameter is "",
     * null is passed instead. This is useful to unify the treatment of
     * the absence of the value vs the empty value.
     */
    boolean fixEmpty() default false;
}
