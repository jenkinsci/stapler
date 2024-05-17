package org.kohsuke.stapler;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import javax.servlet.ServletException;

/**
 * {@link Dispatcher} for url=/ that handles the tail of an URL.
 *
 * @author Kohsuke Kawaguchi
 */
class IndexDispatcher extends Dispatcher {
    private final Function f;

    IndexDispatcher(Function f) {
        this.f = f;
    }

    @Override
    public boolean dispatch(RequestImpl req, ResponseImpl rsp, Object node)
            throws IllegalAccessException, InvocationTargetException, ServletException, IOException {
        if (req.tokens.hasMore()) {
            return false; // applicable only when there's no more token
        }

        Dispatcher.anonymizedTraceEval(req, rsp, node, "%s: Index: %s", f.getName());
        if (traceable()) {
            trace(req, rsp, "-> <%s>.%s(...)", node, f.getName());
        }

        return f.bindAndInvokeAndServeResponse(node, req, rsp);
    }

    @Override
    public String toString() {
        return f.getQualifiedName() + "(...) for url=/";
    }
}
