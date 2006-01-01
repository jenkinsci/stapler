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
    private final TokenList tokens;

    private final Stapler stapler;
    final List<AncestorImpl> ancestors;
    // lazily computed
    private String rest;

    public RequestImpl(Stapler stapler, HttpServletRequest request, List<AncestorImpl> ancestors, TokenList tokens) {
        super(request);
        this.stapler = stapler;
        this.ancestors = ancestors;
        this.tokens = tokens;
    }

    public String getRestOfPath() {
        if(rest==null)
            rest = assembleRestOfPath(tokens);
        return rest;
    }

    public ServletContext getServletContext() {
        return stapler.getServletContext();
    }

    private static String assembleRestOfPath(TokenList tokens) {
        StringBuffer buf = new StringBuffer();
        for( int i=tokens.idx; i<tokens.length(); i++ ) {
            buf.append('/');
            buf.append(tokens.get(i));
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
            idx = buf.substring(idx).indexOf("/")+1;
        buf.setLength(idx-1);
        buf.append(super.getContextPath());
        return buf.toString();
    }

    public List getAncestors() {
        return ancestors;
    }
}
