package org.kohsuke.stapler.jelly.issue76;

/**
 * @author Kohsuke Kawaguchi
 */
public class Leg {
    public final String name;

    public Leg(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name + " leg";
    }
}
