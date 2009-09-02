package org.kohsuke.stapler;

import javax.servlet.ServletException;

/**
 * {@link ServletException} that does proper exception chaining compatible with JDK1.4.
 *
 * @author Kohsuke Kawaguchi
 */
public class ServletException2 extends ServletException {
    public ServletException2() {
    }

    public ServletException2(String message) {
        super(message);
    }

    public ServletException2(String message, Throwable rootCause) {
        super(message, rootCause);
        if (getCause()==null)
            initCause(rootCause);
    }

    public ServletException2(Throwable rootCause) {
        super(rootCause);
        if (getCause()==null)
            initCause(rootCause);
    }
}
