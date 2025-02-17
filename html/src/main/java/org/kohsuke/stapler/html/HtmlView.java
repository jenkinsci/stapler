package org.kohsuke.stapler.html;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A method which renders an HTML view.
 * The method must be public, take no arguments, and return {@link HtmlViewRenderer}.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface HtmlView {
    /** View name, which should be {@code *.xhtml} base name. */
    String value();
}
