package org.kohsuke.stapler;

import javax.servlet.ServletException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

/**
 * @author Kohsuke Kawaguchi
 */
interface Dispatcher {
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
    boolean dispatch(RequestImpl req, ResponseImpl rsp, Object node)
        throws IOException, ServletException, IllegalAccessException, InvocationTargetException;
}
