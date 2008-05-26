package org.kohsuke.stapler.framework.errors;

import java.io.File;

/**
 * Model object used to display the error top page if
 * we couldn't create the home directory.
 *
 * <p>
 * <tt>index.jelly</tt> would display a nice friendly error page.
 *
 * @author Kohsuke Kawaguchi
 */
public class NoHomeDirError extends AbstractErrorObject {
    public final File home;

    public NoHomeDirError(File home) {
        this.home = home;
    }

    @Override
    public String getMessage() {
        return "Unable to create home directory: "+home;
    }
}
