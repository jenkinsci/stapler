package org.kohsuke.stapler.jelly;

import org.apache.commons.jelly.JellyContext;
import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.Script;
import org.apache.commons.jelly.TagSupport;
import org.apache.commons.jelly.XMLOutput;
import org.kohsuke.stapler.MetaClass;

/**
 * Tag that includes views of the object.
 *
 * @author Kohsuke Kawaguchi
 */
public class IncludeTag extends TagSupport {
    private Object it;

    private String page;

    private Object from;

    private boolean optional;

    /**
     * Specifies the name of the JSP to be included.
     */
    public void setPage(String page) {
        this.page = page;
    }

    /**
     * Specifies the object for which JSP will be included.
     * Defaults to the "it" object in the current context.
     */
    public void setIt(Object it) {
        this.it = it;
    }

    /**
     * When loading the script, use the classloader from this object
     * to locate the script. Otherwise defaults to "it" object.
     */
    public void setFrom(Object from) {
        this.from = from;
    }

    /**
     * If true, not finding the page is not an error.
     */
    public void setOptional(boolean optional) {
        this.optional = optional;
    }

    public void doTag(XMLOutput output) throws JellyTagException {
        Object it = this.it;
        if(it==null)
            it = getContext().getVariable("it");

        MetaClass c = MetaClass.get((from!=null?from:it).getClass());
        Script script;
        try {
            script = c.loadTearOff(JellyClassTearOff.class).findScript(page);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new JellyTagException("Error loading '"+page+"' for "+it.getClass(),e);
        }

        if(script==null) {
            if(optional)    return;
            throw new JellyTagException("No page found '"+page+"' for "+it.getClass());
        }

        context = new JellyContext(getContext());
        if(this.it!=null)
            context.setVariable("it",this.it);
        context.setVariable("from", from!=null?from:it);

        ClassLoader old = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(c.classLoader.loader);
        try {
            script.run(context,output);
        } finally {
            Thread.currentThread().setContextClassLoader(old);
        }
    }
}
