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

    /**
     * Diagnostic string that explains this dispatch rule.
     */
    public abstract String toString();


    public static boolean traceable() {
        return TRACE || TRACE_PER_REQUEST || LOGGER.isLoggable(Level.FINE);
    }

    public static void traceEval(StaplerRequest req, StaplerResponse rsp, Object node) {
        trace(req,rsp,String.format("-> evaluate(%s%s,\"%s\")",
                node==null?"null":'<'+node.toString()+'>',
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
        if(isTraceEnabled(req))
            EvaluationTrace.get(req).trace(rsp,msg);
        if(LOGGER.isLoggable(Level.FINE))
            LOGGER.fine(msg);
    }

    /**
     * Checks if tracing is enabled for the given request. Tracing can be
     * enabled globally with the "stapler.trace=true" system property. Tracing
     * can be enabled per-request by setting "stapler.trace.per-request=true"
     * and sending an "X-Stapler-Trace" header set to "true" with the request.
     */
    public static boolean isTraceEnabled(StaplerRequest req) {
        if (TRACE)
            return true;

        if (TRACE_PER_REQUEST && "true".equals(req.getHeader("X-Stapler-Trace")))
            return true;

        return false;
    }

    /**
     * This flag will activate the evaluation trace.
     * It adds the evaluation process as HTTP headers,
     * and when the evaluation failed, special diagnostic 404 page will be rendered.
     * Useful for developer assistance.
     */
    public static boolean TRACE = Boolean.getBoolean("stapler.trace");

    /**
     * This flag will activate the per-request evaluation trace for requests
     * that have X-Stapler-Trace set to "true".
     * It adds the evaluation process as HTTP headers,
     * and when the evaluation failed, special diagnostic 404 page will be rendered.
     * Useful for developer assistance.
     */
    public static boolean TRACE_PER_REQUEST = Boolean.getBoolean("stapler.trace.per-request");

    private static final Logger LOGGER = Logger.getLogger(Dispatcher.class.getName());
}
