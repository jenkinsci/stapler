package org.kohsuke.stapler;

/**
 * Information about ancestor of the "it" node.
 *
 * @author Kohsuke Kawaguchi
 */
public interface Ancestor {
    /**
     * Gets the model object of the application.
     */
    Object getObject();

    /**
     * Gets the URL to this ancestor.
     */
    String getUrl();

    /**
     * Gets the previous ancestor, or null if none (meaning
     * this is the root object.)
     */
    Ancestor getPrev();

    /**
     * Gets the next ancestor, or null if none (meaning
     * this is the 'it' object.
     */
    Ancestor getNext();
}
