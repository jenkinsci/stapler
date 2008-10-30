package org.kohsuke.stapler;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.annotation.Documented;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Indicates that this parameter is bound from HTTP query parameter.
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
}
