package org.kohsuke.stapler;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author Kohsuke Kawaguchi
 */
class AncestorImpl implements Ancestor {
    private final List owner;
    private final int listIndex;

    private Object object;
    private String[] tokens;
    private int index;
    private String contextPath;

    public AncestorImpl(List owner) {
        this.owner = owner;
        listIndex = owner.size();
        owner.add(this);
    }

    public void set(Object object, String[] tokens, int index, HttpServletRequest req ) {
        this.object = object;
        this.tokens = tokens;
        this.index = index;
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
            return (Ancestor)owner.get(listIndex-1);
    }

    public Ancestor getNext() {
        if(listIndex==owner.size()-1)
            return null;
        else
            return (Ancestor)owner.get(listIndex+1);
    }

    public String toString() {
        return object.toString();
    }
}
