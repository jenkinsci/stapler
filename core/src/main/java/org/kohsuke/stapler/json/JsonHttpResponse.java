package org.kohsuke.stapler.json;

import edu.umd.cs.findbugs.annotations.Nullable;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import javax.servlet.ServletException;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.HttpResponses.HttpResponseException;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

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

    @SuppressFBWarnings(
            value = "INFORMATION_EXPOSURE_THROUGH_AN_ERROR_MESSAGE",
            justification = "Jenkins handles this issue differently or doesn't care about it")
    public JsonHttpResponse(Throwable t, int status) {
        StringWriter sw = new StringWriter();
        t.printStackTrace(new PrintWriter(sw));
        this.responseJson = new JSONObject()
                .element("error", t.getClass().getName() + ": " + t.getMessage())
                .element("stackTrace", sw.toString());
        this.status = status;
    }

    @Override
    public void generateResponse(StaplerRequest req, StaplerResponse rsp, Object node)
            throws IOException, ServletException {
        if (status > 0) {
            rsp.setStatus(status);
        }
        if (responseJson != null) {
            rsp.setContentType("application/json;charset=UTF-8");
            try (Writer w = rsp.getCompressedWriter(req)) {
                responseJson.write(w);
            }
        }
    }
}
