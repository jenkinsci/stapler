package org.kohsuke.stapler.jelly;

import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.MissingAttributeException;
import org.apache.commons.jelly.TagSupport;
import org.apache.commons.jelly.XMLOutput;
import org.apache.commons.jelly.expression.Expression;
import org.xml.sax.SAXException;
import org.jvnet.maven.jellydoc.annotation.NoContent;
import org.jvnet.maven.jellydoc.annotation.Required;

/**
 * Tag that outputs the specified value but with escaping,
 * so that you can escape a portion even if the
 * {@link XMLOutput} is not escaping.
 *
 * @author Kohsuke Kawaguchi
 */
@NoContent
public class OutTag extends TagSupport {
    private Expression value;

    @Required
    public void setValue(Expression value) {
        this.value = value;
    }

    public void doTag(XMLOutput output) throws MissingAttributeException, JellyTagException {
        final String text = value.evaluateAsString(context);
        if (text != null) {
            StringBuilder buf = new StringBuilder(text.length());
            for (int i=0; i<text.length(); i++ ) {
                char ch = text.charAt(i);
                switch(ch) {
                case '<':       buf.append("&lt;");     break;
                case '&':       buf.append("&amp;");    break;
                default:        buf.append(ch);
                }
            }

            try {
                output.write(buf.toString());
            }
            catch (SAXException e) {
                throw new JellyTagException("could not write the XMLOutput",e);
            }
        }
    }
}
