package org.kohsuke.stapler;

import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.ServletContext;
import javax.servlet.RequestDispatcher;
import java.io.IOException;
import java.util.List;

/**
 * @author Kohsuke Kawaguchi
 */
class RequestImpl extends HttpServletRequestWrapper implements StaplerRequest {
    private final String[] tokens;
    private final int idx;

    private String rest;
    private Stapler stapler;
    private List ancestors;

    public RequestImpl(Stapler stapler, HttpServletRequest request, List ancestors, String[] tokens, int idx) {
        super(request);
        this.stapler = stapler;
        this.ancestors = ancestors;
        this.tokens = tokens;
        this.idx = idx;
    }

    public String getRestOfPath() {
        if(rest==null)
            rest = assembleRestOfPath(tokens,idx);
        return rest;
    }

    public ServletContext getServletContext() {
        return stapler.getServletContext();
    }

    private static String assembleRestOfPath(String[] tokens,int idx) {
        StringBuffer buf = new StringBuffer();
        for( ; idx<tokens.length; idx++ ) {
            buf.append('/');
            buf.append(tokens[idx]);
        }
        return buf.toString();
    }

    public RequestDispatcher getView(Object it,String jspName) throws IOException {
        return stapler.getResourceDispatcher(it,jspName);
    }

    public String getRootPath() {
        StringBuffer buf = super.getRequestURL();
        int idx = 0;
        for( int i=0; i<3; i++ )
            idx = buf.indexOf("/",idx)+1;
        buf.setLength(idx-1);
        buf.append(super.getContextPath());
        return buf.toString();
    }

    public List getAncestors() {
        return ancestors;
    }
}
