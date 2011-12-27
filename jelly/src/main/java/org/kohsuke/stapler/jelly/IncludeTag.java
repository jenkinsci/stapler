/*
 * Copyright (c) 2004-2010, Kohsuke Kawaguchi
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided
 * that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice, this list of
 *       conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright notice, this list of
 *       conditions and the following disclaimer in the documentation and/or other materials
 *       provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS
 * OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
 * AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER
 * IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF
 * THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

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
import org.jvnet.maven.jellydoc.annotation.Required;

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
    @Required
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
     * (And in such a case, the body of the include tag is evaluated instead.)
     */
    public void setOptional(boolean optional) {
        this.optional = optional;
    }

    public void doTag(XMLOutput output) throws JellyTagException {
        if(page==null) {
            // this makes it convenient when the caller wants to gracefully the expression for @page
            // otherwise this results in http://pastie.org/601828
            if (optional) {
                invokeBody(output);
                return;
            }
            throw new JellyTagException("The page attribute is not specified");
        }
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
            throw new JellyTagException("Error loading '"+page+"' for "+c.klass,e);
        }

        if(script==null) {
            if(optional) {
                invokeBody(output);
                return;
            }
            throw new JellyTagException("No page found '"+page+"' for "+c.klass);
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
