package org.kohsuke.stapler;

/**
 * Represents a T value or no value at all. Used to work around the lack of null value in the computing map.
 *
 * @author Kohsuke Kawaguchi
 */
final class Optional<T> {
    private final T value;

    Optional(T value) {
        this.value = value;
    }

    public T get() { return value; }


    private static Optional NONE = new Optional(null);

    public static <T> Optional<T> none() {
        return NONE;
    }

    public static <T> Optional<T> create(T value) {
        if (value==null)    return NONE;    // cache
        return new Optional<T>(value);
    }
}
