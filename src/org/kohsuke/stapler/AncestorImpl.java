package org.kohsuke.stapler;

import java.util.List;

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
        return buf.toString();
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
