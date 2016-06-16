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
    public final Head head = new Head();

    public Object getDynamic(String part) {
        if (part.equals("arm"))
            return new Arm();
        return null;
    }
}
