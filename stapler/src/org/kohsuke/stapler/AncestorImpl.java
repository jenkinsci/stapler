package org.kohsuke.stapler;

import java.util.List;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author Kohsuke Kawaguchi
 */
class AncestorImpl implements Ancestor {
    private final List<AncestorImpl> owner;
    private final int listIndex;

    private Object object;
    private String[] tokens;
    private int index;
    private String contextPath;

    public AncestorImpl(List<AncestorImpl> owner) {
        this.owner = owner;
        listIndex = owner.size();
        owner.add(this);
    }

    public void set(Object object, RequestImpl req ) {
        this.object = object;
        this.tokens = req.tokens.tokens;
        this.index = req.tokens.idx;
        this.contextPath = req.getContextPath();
    }

    public Object getObject() {
        return object;
    }

    public String getUrl() {
        StringBuffer buf = new StringBuffer(contextPath);
        for( int i=0; i<index; i++ ) {
            buf.append('/');
            buf.append(tokens[i]);
        }
        
        try {
            // 3 arg version accepts illegal character. 1-arg version doesn't
            return new URI(null,buf.toString(),null).toASCIIString();
        } catch (URISyntaxException e) {
            IllegalArgumentException y = new IllegalArgumentException();
            y.initCause(e);
            throw y;
        }
    }

    public Ancestor getPrev() {
        if(listIndex==0)
            return null;
        else
            return owner.get(listIndex-1);
    }

    public Ancestor getNext() {
        if(listIndex==owner.size()-1)
            return null;
        else
            return owner.get(listIndex+1);
    }

    public String toString() {
        return object.toString();
    }
}
