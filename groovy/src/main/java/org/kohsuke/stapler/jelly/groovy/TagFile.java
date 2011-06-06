package org.kohsuke.stapler.jelly.groovy;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

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
@Retention(RUNTIME)
@Documented
@Target(METHOD)
public @interface TagFile {
    String value();
}
