package org.kohsuke.stapler;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotated field or parameter will be automatically trimmed to null before value is used for data-binding.
 * @author <a href="mailto:nicolas.deloof@gmail.com">Nicolas De Loof</a>
 */
@Retention(RUNTIME)
@Target({FIELD, PARAMETER})
public @interface Trim {
}
