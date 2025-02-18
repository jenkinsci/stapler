package org.kohsuke.stapler.html;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A method which renders an HTML view.
 * The method must be public, take no arguments, and return a {@link Record}.
 * The fields of the record must align with the element {@code id}s
 * with names starting with the {@code st.} prefix.
 * The following field types are permitted:
 * <ul>
 * <li>{@link String}, to insert character data.
 * <li>{@link boolean}, to conditionally include a static subtree.
 * <li>Some other {@link Record} type, to include a nested structure, skipped if {@code null}.
 * <li>{@link List} of some {@link Record} type, to repeat a nested structure zero or more times.
 * </ul>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface HtmlView {
    /** View name, which should be {@code *.xhtml} base name. */
    String value();
}
