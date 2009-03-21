package org.kohsuke.stapler.framework.adjunct;

import java.io.IOException;

/**
 * @author Kohsuke Kawaguchi
 */
public class NoSuchAdjunctException extends IOException {
    public NoSuchAdjunctException() {
    }

    public NoSuchAdjunctException(String message) {
        super(message);
    }

    public NoSuchAdjunctException(String message, Throwable cause) {
        super(message);
        initCause(cause);
    }

    public NoSuchAdjunctException(Throwable cause) {
        super();
        initCause(cause);
    }
}
