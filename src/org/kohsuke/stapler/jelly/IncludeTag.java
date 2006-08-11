package org.kohsuke.stapler.jelly;

import org.apache.commons.jelly.JellyContext;
import org.apache.commons.jelly.JellyException;
import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.MissingAttributeException;
import org.apache.commons.jelly.Script;
import org.apache.commons.jelly.TagSupport;
import org.apache.commons.jelly.XMLOutput;
import org.kohsuke.stapler.MetaClass;

import java.net.URL;

/**
 * Tag that includes views of the object.
 *
 * @author Kohsuke Kawaguchi
 */
public class IncludeTag extends TagSupport {
    private Object it;

    private String page;

    private Object from;

    /**
     * Specifies the name of the JSP to be included.
     */
    public void setPage(String page) {
        this.page = page;
    }

    /**
     * Specifies the object for which JSP will be included.
     */
    public void setIt(Object it) {
        this.it = it;
    }

    /**
     * When loading the script, use the classloader from this object
     * to locate the script.
     */
    public void setFrom(Object from) {
        this.from = from;
    }

    public void doTag(XMLOutput output) throws MissingAttributeException, JellyTagException {
        Object it = this.it;
        if(it==null)
            it = getContext().getVariable("it");

        MetaClass c = MetaClass.get((from!=null?from:it).getClass());
        Script script;
        try {
            script = c.findScript(page);
        } catch (JellyException e) {
            throw new JellyTagException("Error loading '"+page+"' for "+it.getClass(),e);
        }

        if(script==null) {
            throw new JellyTagException("No page found '"+page+"' for "+it.getClass());
        }

        JellyContext context = getContext();
        if(this.it!=null) {
            context = new JellyContext(context);
            context.setVariable("it",this.it);
        }
        script.run(context,output);
    }
}
