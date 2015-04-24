package org.kohsuke.stapler.json;

import net.sf.json.JSONObject;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.HttpResponses.HttpResponseException;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import javax.annotation.Nullable;
import javax.servlet.ServletException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * {@link JSONObject} as a response.
 *
 * @author Carlos Sanchez
 */
public class JsonHttpResponse extends HttpResponseException {
    private final @Nullable JSONObject responseJson;
    private final int status;

    public JsonHttpResponse(JSONObject o) {
        this(o, o == null ? 204 : 200);
    }

    public JsonHttpResponse(JSONObject o, int status) {
        this.responseJson = o;
        this.status = status;
    }

    public JsonHttpResponse(Throwable t, int status) {
        StringWriter sw = new StringWriter();
        t.printStackTrace(new PrintWriter(sw));
        this.responseJson = new JSONObject()
                .element("error", t.getClass().getName() + ": " + t.getMessage())
                .element("stackTrace", sw.toString());
        this.status = status;
    }

    @Override
    public void generateResponse(StaplerRequest req, StaplerResponse rsp, Object node) throws IOException,
            ServletException {
        if (status > 0) {
            rsp.setStatus(status);
        }
        if (responseJson != null) {
            rsp.setContentType("application/json;charset=UTF-8");
            responseJson.write(rsp.getCompressedWriter(req));
        }
    }

}
