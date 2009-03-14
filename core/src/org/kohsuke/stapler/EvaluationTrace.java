package org.kohsuke.stapler;

import java.util.List;
import java.util.ArrayList;
import java.io.PrintWriter;

/**
 * Remebers the {@link Stapler#invoke(RequestImpl, ResponseImpl, Object)}
 * evaluation traces.
 *
 * @author Kohsuke Kawaguchi
 */
public class EvaluationTrace {
    private final List<String> traces = new ArrayList<String>();

    public void trace(StaplerResponse rsp, String msg) {
        traces.add(msg);
        // Firefox Live HTTP header plugin cannot nicely render multiple headers
        // with the same name, so give each one unique name.
        rsp.addHeader(String.format("Stapler-Trace-%03d",traces.size()),msg);
    }
    
    public void printHtml(PrintWriter w) {
        for (String trace : traces)
            w.println(trace.replaceAll("&","&amp;").replaceAll("<","&lt;"));
    }

    public static EvaluationTrace get(StaplerRequest req) {
        EvaluationTrace et = (EvaluationTrace) req.getAttribute(KEY);
        if(et==null)
            req.setAttribute(KEY,et=new EvaluationTrace());
        return et;
    }

    /**
     * Used for counting trace header.
     */
    private static final String KEY = EvaluationTrace.class.getName();
}
