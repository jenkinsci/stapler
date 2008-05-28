package org.kohsuke.stapler.framework.adjunct;

import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;

import javax.servlet.ServletOutputStream;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.io.IOException;
import java.io.Writer;

/**
 * This request-scope object keeps track of which {@link Adjunct}s are already included.
 *
 * @author Kohsuke Kawaguchi
 */
public class AdjunctsInPage {
    private final AdjunctManager manager;
    /**
     * All adjuncts that are already included in the page.
     */
    private final Set<String> included = new HashSet<String>();


    /**
     * Obtains the instance associated with the current request of the given {@link StaplerRequest}.
     */
    public static AdjunctsInPage get() {
        return get(Stapler.getCurrentRequest());
    }
    /**
     * Obtains the instance associated with the current request of the given {@link StaplerRequest}.
     *
     * <p>
     * This method is handy when the caller already have the request object around,
     * so that we can save {@link Stapler#getCurrentRequest()} call.
     */
    public static AdjunctsInPage get(StaplerRequest request) {
        AdjunctsInPage aip = (AdjunctsInPage) request.getAttribute(KEY);
        if(aip==null)
            request.setAttribute(KEY,aip=new AdjunctsInPage(AdjunctManager.get(request.getServletContext())));
        return aip;
    }

    private AdjunctsInPage(AdjunctManager manager) {
        this.manager = manager;
    }

    /**
     * Generates the script tag and CSS link tag to include necessary adjuncts,
     * and records the fact that those adjuncts are already included in the page,
     * so that it won't be loaded again.
     */
    public void generate(Writer out, String[] includes) throws IOException {
        List<Adjunct> needed = new ArrayList<Adjunct>();
        for (String include : includes)
            findNeeded(include,needed);

        StringBuilder cssList = buildList(needed,".css",Adjunct.Kind.CSS);
        if(cssList.length()>0)
            out.write("<link rel='stylesheet' href='"+manager.rootURL+'/'+cssList+"' type='text/css' />");

        StringBuilder jsList = buildList(needed,".js",Adjunct.Kind.JS);
        if(jsList.length()>0)
            out.write("<script src='"+manager.rootURL+'/'+jsList+"' type='text/javascript'></script>");
    }

    /**
     * Builds up the needed adjuncts into the 'needed' list.
     */
    private void findNeeded(String include, List<Adjunct> needed) throws IOException {
        if(!included.add(include))
            return; // alerady sent

        // list dependencies first
        Adjunct a = manager.get(include);
        for (String req : a.required)
            findNeeded(req,needed);
        needed.add(a);
    }

    /**
     * Combines adjunct names into ','-separated string.
     */
    private StringBuilder buildList(List<Adjunct> needed, String ext, Adjunct.Kind kind) {
        StringBuilder id = new StringBuilder();
        for (Adjunct adj : needed) {
            if(!adj.has(kind)) continue;
            if(id.length()>0)  id.append(',');
            id.append(adj.name).append(ext);
        }
        return id;
    }

    private static final String KEY = AdjunctsInPage.class.getName();

}
