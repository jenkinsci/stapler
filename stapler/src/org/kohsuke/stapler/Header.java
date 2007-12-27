package org.kohsuke.stapler;

import static java.lang.annotation.ElementType.PARAMETER;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;
import java.lang.annotation.Documented;

/**
 * Indicates that this parameter is bound from HTTP header.
 *
 * @author Kohsuke Kawaguchi
 */
@Retention(RUNTIME)
@Target(PARAMETER)
@Documented
public @interface Header {
    /**
     * HTTP header name.
     */
    String value();

    /**
     * If true, request without this header will be rejected.
     */
    boolean required() default false;
}
