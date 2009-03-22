package org.kohsuke.stapler;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.annotation.Documented;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Indicates that this parameter is injected by evaluating
 * {@link StaplerRequest#findAncestorObject(Class)} with the parameter type.
 *
 * @author Kohsuke Kawaguchi
 */
@Retention(RUNTIME)
@Target(PARAMETER)
@Documented
public @interface AncestorInPath {
}
