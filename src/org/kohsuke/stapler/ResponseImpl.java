package org.kohsuke.stapler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;

/**
 * @author Kohsuke Kawaguchi
 */
class ResponseImpl extends HttpServletResponseWrapper implements StaplerResponse {
    private final Stapler stapler;
    private final HttpServletResponse response;

    public ResponseImpl(Stapler stapler, HttpServletResponse response) {
        super(response);
        this.stapler = stapler;
        this.response = response;
    }

    public void forward(Object it, String url, StaplerRequest request) throws ServletException, IOException {
        stapler.invoke(request,response,it,url);
    }
}
