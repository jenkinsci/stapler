package org.kohsuke.stapler;

import javax.servlet.ServletException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controls the dispatching of incoming HTTP requests.
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class Dispatcher {
    /**
     * Trys to handle the given request and returns true
     * if it succeeds. Otherwise false.
     *
     * <p>
     * We have a few known strategies for handling requests
     * (for example, one is to try to treat the request as JSP invocation,
     * another might be try getXXX(), etc) So we use a list of
     * {@link Dispatcher} and try them one by one until someone
     * returns true.
     */
    public abstract boolean dispatch(RequestImpl req, ResponseImpl rsp, Object node)
        throws IOException, ServletException, IllegalAccessException, InvocationTargetException;

    public static boolean traceable() {
        return TRACE || LOGGER.isLoggable(Level.FINE);
    }

    public static void trace(StaplerResponse rsp, String msg) {
        if(TRACE)
            rsp.addHeader("Stapler-Trace",msg);
        if(LOGGER.isLoggable(Level.FINE))
            LOGGER.fine(msg);
    }

    /**
     * Trace option to show the parsing result in HTTP header.
     */
    public static boolean TRACE = Boolean.getBoolean("stapler.trace");

    private static final Logger LOGGER = Logger.getLogger(Dispatcher.class.getName());
}
