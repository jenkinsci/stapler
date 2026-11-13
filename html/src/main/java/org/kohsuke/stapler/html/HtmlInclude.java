package org.kohsuke.stapler.html;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Denotes that a record field should include another view.
 * The field type should be Stapler-dispatchable.
 */
@Target(ElementType.RECORD_COMPONENT)
@Retention(RetentionPolicy.RUNTIME)
public @interface HtmlInclude {
    /**
     * The name of the view to include.
     * No file extension should be used, so for example use {@code index} or {@code config}.
     */
    String value();
}
