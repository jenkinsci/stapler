package org.kohsuke.stapler.jelly;

import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.XMLOutput;
import org.jvnet.maven.jellydoc.annotation.NoContent;
import org.jvnet.maven.jellydoc.annotation.Required;
import org.kohsuke.stapler.WebApp;
import org.kohsuke.stapler.bind.Bound;
import org.xml.sax.SAXException;

/**
 * Binds a server-side object to client side so that JavaScript can call into server.
 * This tag evaluates to a &lt;script> tag.
 * 
 * @author Kohsuke Kawaguchi
 */
@NoContent
public class BindTag extends AbstractStaplerTag {
    private String varName;
    private Object javaObject;

    /**
     * JavaScript variable name to set the proxy to.
     *
     * This name can be arbitrary left hand side expression,
     * such as "a[0]" or "a.b.c".
     */
    @Required
    public void setVar(String varName) {
        this.varName = varName;
    }

    @Required
    public void setValue(Object o) {
        this.javaObject = o;
    }

    public void doTag(XMLOutput out) throws JellyTagException {
        // make sure we get the supporting script in place
        AdjunctTag a = new AdjunctTag();
        a.setContext(getContext());
        a.setIncludes("org.kohsuke.stapler.bind");
        a.doTag(out);

        try {
            out.startElement("script");
            if (javaObject==null) {
                out.write(varName+"=null;");
            } else {
                Bound h = WebApp.getCurrent().boundObjectTable.bind(javaObject);
                out.write(varName+'='+h.getProxyScript()+';');
            }
            out.endElement("script");
        } catch (SAXException e) {
            throw new JellyTagException(e);
        }
    }
}
