package org.kohsuke.stapler;

/**
 * Indicates a failure to load a script.
 *
 * @author Kohsuke Kawaguchi
 */
public class ScriptLoadException extends RuntimeException {
    public ScriptLoadException(String message, Throwable cause) {
        super(message, cause);
    }

    public ScriptLoadException(Throwable cause) {
        super(cause);
    }
}
