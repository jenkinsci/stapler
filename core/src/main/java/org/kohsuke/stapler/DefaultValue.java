package org.kohsuke.stapler;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Indicate value to be used when databound payload is missing property <em>or</em> provided value is null
 * @author <a href="mailto:nicolas.deloof@gmail.com">Nicolas De Loof</a>
 */
@Retention(RUNTIME)
@Target({FIELD, PARAMETER})
public @interface DefaultValue {

    String value();
}
