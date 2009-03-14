package org.kohsuke.stapler;

import javax.servlet.ServletException;
import java.io.IOException;

/**
 * Marks the object that can handle HTTP DELETE.
 *
 * @author Kohsuke Kawaguchi
 */
public interface HttpDeletable {
    /**
     * Called when HTTP DELETE method is invoked.
     */
    void delete( StaplerRequest req, StaplerResponse rsp ) throws IOException, ServletException;
}
