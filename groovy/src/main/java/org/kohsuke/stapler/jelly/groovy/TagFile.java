package org.kohsuke.stapler.jelly.groovy;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used on methods of {@link TypedTagLibrary} to
 * indicate a real tag file name.
 *
 * <p>
 * This is used when the real tag file name is not
 * a valid Java identifier.
 *
 * @author Kohsuke Kawaguchi
 */
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Target(ElementType.METHOD)
public @interface TagFile {
    String value();
}
