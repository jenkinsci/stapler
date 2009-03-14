package org.kohsuke.stapler;

import javax.servlet.ServletException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

/**
 * @author Kohsuke Kawaguchi
 */
abstract class NameBasedDispatcher extends Dispatcher {
    protected final String name;
    private final int argCount;

    protected NameBasedDispatcher(String name, int argCount) {
        this.name = name;
        this.argCount = argCount;
    }

    protected NameBasedDispatcher(String name) {
        this(name,0);
    }

    public final boolean dispatch(RequestImpl req, ResponseImpl rsp, Object node)
        throws IOException, ServletException, IllegalAccessException, InvocationTargetException {
        if(!req.tokens.hasMore() || !req.tokens.peek().equals(name))
            return false;
        if(req.tokens.countRemainingTokens()<=argCount)
            return false;
        req.tokens.next();
        doDispatch(req,rsp,node);
        return true;
    }

    protected abstract void doDispatch(RequestImpl req, ResponseImpl rsp, Object node)
        throws IOException, ServletException, IllegalAccessException, InvocationTargetException;
}
