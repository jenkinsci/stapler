package org.kohsuke.stapler;

/**
 * {@link Function#contextualize(Object)} parameter that indicates
 * the function is called to traverse an object graph.
 *
 * @author Kohsuke Kawaguchi
 * @see WebMethod
 */
public final class TraversalMethodContext {
    private final String name;

    // instantiation restricted to this class
    /*package*/ TraversalMethodContext(String name) {
        this.name = name;
    }

    /**
     * Name of the web method. "" for index route.
     */
    public String getName() {
        return name;
    }

    /**
     * Used as a special name that represents {@code getDynamic(...)} that does dynamic traversal.
     */
    public static final String DYNAMIC = "\u0000";
}
