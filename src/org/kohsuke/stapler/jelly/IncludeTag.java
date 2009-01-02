package org.kohsuke.stapler.jelly;

import org.apache.commons.jelly.JellyContext;
import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.Script;
import org.apache.commons.jelly.TagSupport;
import org.apache.commons.jelly.XMLOutput;
import org.apache.commons.jelly.impl.TagScript;
import org.kohsuke.stapler.MetaClass;
import org.kohsuke.stapler.WebApp;
import org.xml.sax.SAXException;

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

    private Class clazz;

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
     * When loading script, load from this class.
     *
     * By default this is "from.getClass()". This takes
     * precedence over the {@link #setFrom(Object)} method.
     */
    public void setClass(Class clazz) {
        this.clazz = clazz;
    }

    /**
     * If true, not finding the page is not an error.
     */
    public void setOptional(boolean optional) {
        this.optional = optional;
    }

    public void doTag(XMLOutput output) throws JellyTagException {
        if(page==null)
            throw new JellyTagException("The page attribute is not specified");
        Object it = this.it;
        if(it==null)
            it = getContext().getVariable("it");

        MetaClass c = WebApp.getCurrent().getMetaClass(getScriptClass(it));
        Script script;
        try {
            script = c.loadTearOff(JellyClassTearOff.class).findScript(page);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new JellyTagException("Error loading '"+page+"' for "+c.clazz,e);
        }

        if(script==null) {
            if(optional)    return;
            throw new JellyTagException("No page found '"+page+"' for "+c.clazz);
        }

        context = new JellyContext(getContext());
        if(this.it!=null)
            context.setVariable("it",this.it);
        context.setVariable("from", from!=null?from:it);

        ClassLoader old = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(c.classLoader.loader);
        try {
            String source = null;
            if(JellyFacet.TRACE) {
                if (script instanceof TagScript) {
                    TagScript ts = (TagScript) script;
                    source = ts.getFileName();
                } else
                    source = page+" (exact source location unknown)";

                String msg = "\nBegin " + source+'\n';
                output.comment(msg.toCharArray(),0,msg.length());
            }
            script.run(context,output);
            if(source!=null) {
                String msg = "\nEnd " + source+'\n';
                output.comment(msg.toCharArray(),0,msg.length());
            }
        } catch (SAXException e) {
            throw new JellyTagException(e);
        } finally {
            Thread.currentThread().setContextClassLoader(old);
        }
    }

    private Class getScriptClass(Object it) {
        if (clazz != null) return clazz;
        if (from != null) return from.getClass();
        else return it.getClass();
    }
}
