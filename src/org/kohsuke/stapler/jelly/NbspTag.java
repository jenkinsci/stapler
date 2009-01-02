package org.kohsuke.stapler.jelly;

import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.MissingAttributeException;
import org.apache.commons.jelly.TagSupport;
import org.apache.commons.jelly.XMLOutput;
import org.xml.sax.SAXException;
import org.jvnet.maven.jellydoc.annotation.NoContent;

/**
 * Writes out '&amp;nbsp;'.
 *
 * @author Kohsuke Kawaguchi
 */
@NoContent
public class NbspTag extends TagSupport {
    public void doTag(XMLOutput output) throws MissingAttributeException, JellyTagException {
        try {
            output.write("\u00A0"); // nbsp
        } catch (SAXException e) {
            throw new JellyTagException(e);
        }
    }
}
