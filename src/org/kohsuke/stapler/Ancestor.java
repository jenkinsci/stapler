package org.kohsuke.stapler;

/**
 * Information about ancestor of the "it" node.
 *
 * @author Kohsuke Kawaguchi
 */
public interface Ancestor {
    /**
     * Gets the model object.
     */
    Object getObject();

    /**
     * Gets the URL to this ancestor.
     */
    String getUrl();

    /**
     * Gets the previous ancestor, or null if none.
     */
    Ancestor getPrev();

    /**
     * Gets the next ancestor, or null if none.
     */
    Ancestor getNext();
}
