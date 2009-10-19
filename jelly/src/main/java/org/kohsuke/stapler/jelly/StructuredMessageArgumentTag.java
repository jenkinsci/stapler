package org.kohsuke.stapler.jelly;

import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.XMLOutput;

import java.io.StringWriter;

/**
 * Body is evaluated and is used as an argument for the surrounding &lt;structuredMessageFormat> element.
 *
 * @author Kohsuke Kawaguchi
 */
public class StructuredMessageArgumentTag extends AbstractStaplerTag {
    public void doTag(XMLOutput output) throws JellyTagException {
        StructuredMessageFormatTag tag = (StructuredMessageFormatTag)findAncestorWithClass(StructuredMessageFormatTag.class);
        if(tag == null)
            throw new JellyTagException("This tag must be enclosed inside a <structuredMessageFormat> tag" );

        StringWriter sw = new StringWriter();
        XMLOutput o = XMLOutput.createXMLOutput(sw);
        invokeBody(o);

        tag.addArgument(sw);
    }
}
