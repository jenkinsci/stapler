package org.kohsuke.stapler;

import org.kohsuke.stapler.bind.JavaScriptMethod;

/**
 * {@link Function#contextualize(Object)} parameter that indicates
 * the function is called to serve JavaScript method invocation from a proxy.
 *
 * @author Kohsuke Kawaguchi
 * @see JavaScriptMethod
 */
public final class JavaScriptMethodContext {
    private final String name;

    // instantiation restricted to this class
    /*package*/ JavaScriptMethodContext(String name) {
        this.name = name;
    }

    /**
     * Name of the web method. "" for index route.
     */
    public String getName() {
        return name;
    }
}

