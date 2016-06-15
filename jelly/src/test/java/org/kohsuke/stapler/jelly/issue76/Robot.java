package org.kohsuke.stapler.jelly.issue76;

/**
 * Root of the model object.
 *
 * <p>
 * Test ground of various traversal logic.
 *
 * @author Kohsuke Kawaguchi
 */
public class Robot {
    public Eye getEye(int i) {
        return new Eye(i);
    }

    public final Head head = new Head();

    // TODO: more of this
}
