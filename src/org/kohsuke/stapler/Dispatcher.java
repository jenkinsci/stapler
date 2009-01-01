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

    public static void traceEval(StaplerRequest req, StaplerResponse rsp, Object node) {
        trace(req,rsp,String.format("-> evaluate(<%s>%s,\"%s\")",
                node,
                node==null?"":" :"+node.getClass().getName(),
                ((RequestImpl)req).tokens.assembleRestOfPath()));
    }

    public static void traceEval(StaplerRequest req, StaplerResponse rsp, Object node, String prefix, String suffix) {
        trace(req,rsp,String.format("-> evaluate(%s<%s>%s,\"%s\")",
                prefix,node,suffix,
                ((RequestImpl)req).tokens.assembleRestOfPath()));
    }

    public static void traceEval(StaplerRequest req, StaplerResponse rsp, Object node, String expression) {
        trace(req,rsp,String.format("-> evaluate(<%s>.%s,\"%s\")",
                node,expression,
                ((RequestImpl)req).tokens.assembleRestOfPath()));
    }

    public static void trace(StaplerRequest req, StaplerResponse rsp, String msg, Object... args) {
        trace(req,rsp,String.format(msg,args));
    }

    public static void trace(StaplerRequest req, StaplerResponse rsp, String msg) {
        if(TRACE) {
            // Firefox Live HTTP header plugin cannot nicely render multiple headers
            // with the same name, so give each one unique name.
            Integer count = (Integer) req.getAttribute(TRACE_KEY);
            if(count==null) count=1;
            else            count+=1;
            req.setAttribute(TRACE_KEY,count);
            rsp.addHeader(String.format("Stapler-Trace-%03d",count),msg);
        }
        if(LOGGER.isLoggable(Level.FINE))
            LOGGER.fine(msg);
    }

    /**
     * This flag will activate the evaluation trace.
     * It adds the evaluation process as HTTP headers,
     * and when the evaluation failed, special diagnostic 404 page will be rendered.
     * Useful for developer assistance.
     */
    public static boolean TRACE = Boolean.getBoolean("stapler.trace");

    /**
     * Used for counting trace header.
     */
    private static final String TRACE_KEY = Dispatcher.class.getName() + ".trace-count";

    private static final Logger LOGGER = Logger.getLogger(Dispatcher.class.getName());
}
