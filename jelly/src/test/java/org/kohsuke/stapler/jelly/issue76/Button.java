package org.kohsuke.stapler.jelly.issue76;

/**
 * @author Kohsuke Kawaguchi
 */
public class Button {
    public final String color;
    public Button(String color) {
        this.color = color;
    }

    @Override
    public String toString() {
        return color+" button";
    }
}
