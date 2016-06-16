package org.kohsuke.stapler.jelly.issue76;

/**
 * @author Kohsuke Kawaguchi
 */
public class Head {
    public Eye getEye(int i) {
        return new Eye(i);
    }
}
