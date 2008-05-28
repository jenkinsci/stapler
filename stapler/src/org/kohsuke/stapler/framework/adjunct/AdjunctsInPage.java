package org.kohsuke.stapler.framework.adjunct;

import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

        for (Adjunct adj : needed) {
            if(adj.hasCss)
                out.write("<link rel='stylesheet' href='"+manager.rootURL+'/'+adj.slashedName+".css' type='text/css' />");
            if(adj.hasJavaScript)
                out.write("<script src='"+manager.rootURL+'/'+adj.slashedName+".js' type='text/javascript'></script>");
        }
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

    private static final String KEY = AdjunctsInPage.class.getName();

}
