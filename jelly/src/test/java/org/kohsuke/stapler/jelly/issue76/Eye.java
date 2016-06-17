package org.kohsuke.stapler.jelly.issue76;

/**
 * @author Kohsuke Kawaguchi
 */
public class Eye {
    public final int i;

    public Eye(int i) {
        this.i = i;
    }

    @Override
    public String toString() {
        return "eye #"+i;
    }
}
