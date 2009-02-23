package org.kohsuke.stapler;

/**
 * If an object delegates all its UI processing to another object,
 * it can implement this interface and return the designated object
 * from the {@link #getTarget()} method.
 *
 * <p>
 * Compared to {@link StaplerFallback}, stapler handles this interface at the very end,
 * whereas {@link StaplerProxy} is handled at the very beginning.
 *
 * <p>
 * By returning {@code this} from the {@link #getTarget()} method,
 * {@link StaplerProxy} can be also used just as an interception hook (for example
 * to perform authorization.) 
 *
 * @author Kohsuke Kawaguchi
 * @see StaplerFallback
 */
public interface StaplerProxy {
    /**
     * Returns the object that is responsible for processing web requests.
     *
     * @return
     *      If null is returned, it generates 404.
     *      If {@code this} object is returned, no further
     *      {@link StaplerProxy} look-up is done and {@code this} object
     *      processes the request.
     */
    Object getTarget();
}
