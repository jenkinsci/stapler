package org.kohsuke.stapler.bind;

import org.kohsuke.stapler.HttpResponse;

import java.lang.reflect.Method;

/**
 * Handles to the object bound via {@link BoundObjectTable}.
 *
 * As {@link HttpResponse}, this object generates a redirect to the URL that it points to.
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class Bound implements HttpResponse {
    /**
     * Explicitly unbind this object. The referenced object
     * won't be bound to URL anymore.
     */
    public abstract void release();

    /**
     * The URL where the object is bound to. This method
     * starts with '/' and thus always absolute within the current web server.
     */
    public abstract String getURL();

    /**
     * Gets the bound object.
     */
    public abstract Object getTarget();

    /**
     * Returns a JavaScript expression which evaluates to a JavaScript proxy that
     * talks back to the bound object that this handle represents.
     */
    public final String getProxyScript() {
        StringBuilder buf = new StringBuilder("makeStaplerProxy('").append(getURL()).append("',[");

        boolean first=true;
        for (Method m : getTarget().getClass().getMethods()) {
            if (!m.getName().startsWith("js"))   continue;  // not a JavaScript method

            if (first)  first = false;
            else        buf.append(',');
            buf.append('\'').append(camelize(m.getName().substring(2))).append('\'');
        }
        buf.append("])");
        
        return buf.toString();
    }

    private static String camelize(String name) {
        return Character.toLowerCase(name.charAt(0))+name.substring(1);
    }
}
