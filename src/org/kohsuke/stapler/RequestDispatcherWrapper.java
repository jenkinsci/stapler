package org.kohsuke.stapler;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.ServletException;
import java.io.IOException;

/**
 * {@link RequestDispatcher} that sets "it" before the invocation.
 *
 * @author Kohsuke Kawaguchi
 */
final class RequestDispatcherWrapper implements RequestDispatcher {
    private final RequestDispatcher core;
    private final Object it;

    public RequestDispatcherWrapper(RequestDispatcher core, Object it) {
        this.core = core;
        this.it = it;
    }

    public void forward(ServletRequest req, ServletResponse rsp) throws ServletException, IOException {
        req.setAttribute("it",it);
        core.forward(req,rsp);
    }

    public void include(ServletRequest req, ServletResponse rsp) throws ServletException, IOException {
        Object oldIt = req.getAttribute("it");
        req.setAttribute("it",it);
        try {
            core.include(req,rsp);
        } finally {
            req.setAttribute("it",oldIt);
        }
    }
}
