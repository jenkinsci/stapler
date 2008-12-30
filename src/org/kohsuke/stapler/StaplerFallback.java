package org.kohsuke.stapler;

/**
 * An object can fall back to another object for a part of its UI processing,
 * by implementing this interface and designating another object from
 * {@link #getStaplerFallback()}.
 *
 * <p>
 * Compared to {@link StaplerProxy}, stapler handles this interface at the very end,
 * whereas {@link StaplerProxy} is handled at the very beginning.
 *
 * @author Kohsuke Kawaguchi
 * @see StaplerProxy
 */
public interface StaplerFallback {
    /**
     * Returns the object that is further searched for processing web requests.
     *
     * @return
     *      If null or {@code this} is returned, stapler behaves as if the object
     *      didn't implement this interface (which means the request processing
     *      failes with 404.)
     */
    Object getStaplerFallback();
}
