package org.kohsuke.stapler.jelly;

/**
 * Factory for {@link ResourceBundle}s.
 *
 * @author Kohsuke Kawaguchi
 */
public class ResourceBundleFactory {
    public ResourceBundle create(String baseName) {
        return new ResourceBundle(baseName);
    }

    public static final ResourceBundleFactory INSTANCE = new ResourceBundleFactory();
}
