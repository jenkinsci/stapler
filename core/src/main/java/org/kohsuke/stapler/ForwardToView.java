package org.kohsuke.stapler;

import javax.servlet.ServletException;
import javax.servlet.RequestDispatcher;
import java.io.IOException;

/**
 * {@link HttpResponse} that forwards to a {@link RequestDispatcher}, such as a view.
 * @author Kohsuke Kawaguchi
 */
public class ForwardToView implements HttpResponse {
    private final DispatcherFactory factory;

    private interface DispatcherFactory {
        RequestDispatcher get(StaplerRequest req) throws IOException;
    }

    public ForwardToView(final RequestDispatcher dispatcher) {
        this.factory = new DispatcherFactory() {
            public RequestDispatcher get(StaplerRequest req) {
                return dispatcher;
            }
        };
    }

    public ForwardToView(final Object it, final String view) {
        this.factory = new DispatcherFactory() {
            public RequestDispatcher get(StaplerRequest req) throws IOException {
                return req.getView(it,view);
            }
        };
    }

    public ForwardToView(final Class c, final String view) {
        this.factory = new DispatcherFactory() {
            public RequestDispatcher get(StaplerRequest req) throws IOException {
                return req.getView(c,view);
            }
        };
    }

    public void generateResponse(StaplerRequest req, StaplerResponse rsp, Object node) throws IOException, ServletException {
        factory.get(req).forward(req,rsp);
    }
}
