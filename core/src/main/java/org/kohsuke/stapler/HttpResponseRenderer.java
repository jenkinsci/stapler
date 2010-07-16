package org.kohsuke.stapler;

import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.export.Flavor;

import javax.servlet.ServletException;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.util.Collection;

/**
 * Pluggable interface that takes the return value from request handling
 * methods and convert that to HTTP responses.
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class HttpResponseRenderer {
    /**
     *
     * @param node
     *      Object that handled the request.
     * @param response
     *      The return value or the exception from the method.
     * @return
     *      true if the response object was understood and rendered by this method.
     *      false otherwise, in which case the next {@link HttpResponseRenderer}
     *      will be consulted.
     */
    public abstract boolean generateResponse(StaplerRequest req, StaplerResponse rsp, Object node, Object response) throws IOException, ServletException;

    /**
     * Default {@link HttpResponseRenderer}.
     */
    public static class Default extends HttpResponseRenderer {
        @Override
        public boolean generateResponse(StaplerRequest req, StaplerResponse rsp, Object node, Object response) throws IOException, ServletException {
            return handleHttpResponse(req, rsp, node, response)
                || handleJSON(rsp, response)
                || handleJavaScriptProxyMethodCall(req,rsp,response)
                || handlePrimitive(rsp, response);
        }

        protected boolean handleJavaScriptProxyMethodCall(StaplerRequest req, StaplerResponse rsp, Object response) throws IOException {
            if (req.isJavaScriptProxyCall()) {
                rsp.setContentType(Flavor.JSON.contentType);
                PrintWriter w = rsp.getWriter();

                // handle other primitive types as JSON response
                if (response instanceof String) {
                    w.print('"'+response.toString().replace("\"","\\\"")+'"');
                } else
                if (response instanceof Number || response instanceof Boolean) {
                    w.print(response);
                } else
                if (response instanceof Collection || (response!=null && response.getClass().isArray())) {
                    JSONArray.fromObject(response).write(w);
                } else {
                    // last fall back
                    JSONObject.fromObject(response).write(w);
                }
                return true;
            }
            return false;
        }

        protected boolean handlePrimitive(StaplerResponse rsp, Object response) throws IOException {
            if (response instanceof String || response instanceof Integer) {
                rsp.setContentType("text/plain;charset=UTF-8");
                rsp.getWriter().print(response);
                return true;
            }
            return false;
        }

        protected boolean handleHttpResponse(StaplerRequest req, StaplerResponse rsp, Object node, Object response) throws IOException, ServletException {
            if (response instanceof HttpResponse) {
                // let the result render the response
                HttpResponse r = (HttpResponse) response;
                r.generateResponse(req,rsp,node);
                return true;
            }
            return false;
        }

        protected boolean handleJSON(StaplerResponse rsp, Object response) throws IOException {
            if (response instanceof JSON) {
                rsp.setContentType(Flavor.JSON.contentType);
                ((JSON)response).write(rsp.getWriter());
                return true;
            }
            return false;
        }
    }
}
