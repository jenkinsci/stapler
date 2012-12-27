package org.kohsuke.stapler.config;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

/**
 * Customizes how the property retrieval is handled.
 *
 * @author Kohsuke Kawaguchi
 * @see ConfigurationLoader
 */
@Retention(RUNTIME)
@Target(METHOD)
@Documented
public @interface Configuration {

    /**
     * Name of the property.
     */
    String name() default UNSPECIFIED;

    /**
     * Default value to be applied if the actual configuration source doesn't specify this property.
     */
    String defaultValue() default UNSPECIFIED;

    static String UNSPECIFIED = "\u0000";
}
