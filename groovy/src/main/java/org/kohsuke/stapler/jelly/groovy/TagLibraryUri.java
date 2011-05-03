package org.kohsuke.stapler.jelly.groovy;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

/**
 * Used on subtypes of {@link TypedTagLibrary} to associate the namespace URI to look up tags from.
 *
 * @author Kohsuke Kawaguchi
 */
@Retention(RUNTIME)
@Documented
@Target(TYPE)
public @interface TagLibraryUri {
    String value();
}
