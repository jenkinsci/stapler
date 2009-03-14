package org.kohsuke.stapler;

/**
 * @author Kohsuke Kawaguchi
 */
public class NoStaplerConstructorException extends IllegalArgumentException {
    public NoStaplerConstructorException(String s) {
        super(s);
    }

    public NoStaplerConstructorException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoStaplerConstructorException(Throwable cause) {
        super(cause);
    }
}
