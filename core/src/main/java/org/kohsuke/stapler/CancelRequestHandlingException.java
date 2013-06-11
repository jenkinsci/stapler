package org.kohsuke.stapler;

/**
 * Signals that the request dispatching to the current method is cancelled,
 * and that Stapler should resume the search for the next request dispatcher
 * and dispatch the request accordingly.
 *
 * <p>
 * This is useful in conjunction with {@link StaplerOverridable} to delegate
 * requests selectively to original object after examining the request,
 * or in a request handling method like {@code doXyz()} method to then fall
 * back to {@code getDynamic()} or anything else.
 *
 * @author Kohsuke Kawaguchi
 * @since 1.210
 */
public class CancelRequestHandlingException extends RuntimeException {
}
