package org.kohsuke.stapler;

/**
 * If an object delegates all its UI processing to another object,
 * it can implement this interface and return the designated object
 * from the {@link #getTarget()} method.
 *
 * @author Kohsuke Kawaguchi
 */
public interface StaplerProxy {
    Object getTarget();
}
