package org.kohsuke.stapler.export;

/**
 * Marks an iterable whose ranges should be selected using its iterator, even if it is a
 * {@link java.util.List}. This avoids calling expensive list operations such as
 * {@link java.util.List#size()} when exporting a bounded range.
 *
 * <p>The {@link Range#apply(java.util.List)} overload materializes the selected range to
 * preserve its list return type. The iterable and collection overloads remain lazy.
 */
public interface AlwaysIterate {}
