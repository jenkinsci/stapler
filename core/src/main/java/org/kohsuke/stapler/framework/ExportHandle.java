package org.kohsuke.stapler.framework;

import org.kohsuke.stapler.HttpResponse;

/**
 * Handles to the object exported via {@link ExportedObjectTable}.
 *
 * As {@link HttpResponse}, this object generates a redirect to the URL that it points to.
 *
 * @author Kohsuke Kawaguchi
 */
public interface ExportHandle extends HttpResponse {
    /**
     * Explicitly unexport this object. The referenced object
     * won't be bound to URL anymore.
     */
    void release();

    /**
     * The URL where the exported object is bound to. This method
     * starts with '/' and thus always absolute within the current web server.
     */
    String getURL();
}
