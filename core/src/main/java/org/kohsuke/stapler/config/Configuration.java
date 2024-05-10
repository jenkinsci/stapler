package org.kohsuke.stapler.config;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Customizes how the property retrieval is handled.
 *
 * @author Kohsuke Kawaguchi
 * @see ConfigurationLoader
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
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

    String UNSPECIFIED = "\u0000";
}
