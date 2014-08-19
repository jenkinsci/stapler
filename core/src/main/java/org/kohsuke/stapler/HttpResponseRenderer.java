/*
 * Copyright (c) 2004-2010, Kohsuke Kawaguchi
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided
 * that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice, this list of
 *       conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright notice, this list of
 *       conditions and the following disclaimer in the documentation and/or other materials
 *       provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS
 * OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
 * AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER
 * IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF
 * THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.kohsuke.stapler;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONNull;
import net.sf.json.JSONObject;
import net.sf.json.util.JSONUtils;
import org.kohsuke.stapler.export.Flavor;

/**
 * Pluggable interface that takes the return value from request handling
 * methods and convert that to HTTP responses.
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class HttpResponseRenderer {

    private static final Logger LOGGER = Logger.getLogger(HttpResponseRenderer.class.getName());

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
                try {
                if (response instanceof String) {
                    w.print(quote((String) response));
                } else
                if (response instanceof Number || response instanceof Boolean) {
                    w.print(response);
                } else
                if (response instanceof Collection || (response!=null && response.getClass().isArray())) {
                    JSONArray.fromObject(response, rsp.getJsonConfig()).write(w);
                } else
                if (response==null) {
                    JSONNull.getInstance().write(w);
                } else if (response instanceof Throwable) {
                    // as caught by Function.bindAndInvokeAndServeResponse
                    LOGGER.log(Level.WARNING, "call to " + req.getRequestURI() + " failed", (Throwable) response);
                    return false;
                } else {
                    // last fall back
                    JSONObject.fromObject(response, rsp.getJsonConfig()).write(w);
                }
                } catch (JSONException x) {
                    LOGGER.log(Level.WARNING, "failed to serialize " + response + " for " + req.getRequestURI() + " given " + req.getAncestors(), x);
                    return false;
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
                try {
                    r.generateResponse(req,rsp,node);
                } catch (IOException e) {
                    if (!handleHttpResponse(req,rsp,node,e))
                        throw e;
                } catch (RuntimeException e) {
                    if (!handleHttpResponse(req,rsp,node,e))
                        throw e;
                } catch (ServletException e) {
                    if (!handleHttpResponse(req,rsp,node,e))
                        throw e;
                }
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

    static String quote(String text) {
        return JSONUtils.quote(text);
    }

}
