package org.kohsuke.stapler;

/**
 * Argument to expressions that indicates this value is raw HTML
 * and therefore should not be further escaped.
 */
public class RawHtmlArgument {
    private final Object value;

    public RawHtmlArgument(Object value) {
        this.value = value;
    }

    public Object getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value==null?"null":value.toString();
    }
}
