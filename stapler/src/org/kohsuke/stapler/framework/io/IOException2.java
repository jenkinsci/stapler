package org.kohsuke.stapler.framework.io;

import java.io.IOException;

/**
 * {@link IOException} with missing constructors.
 * @author Kohsuke Kawaguchi
 */
public class IOException2 extends IOException {
    public IOException2() {
    }

    public IOException2(String message) {
        super(message);
    }

    public IOException2(String message, Throwable cause) {
        super(message);
        initCause(cause);
    }

    public IOException2(Throwable cause) {
        this(cause.toString(),cause);
    }
}
