package org.kohsuke.stapler.jelly;

import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.MissingAttributeException;
import org.apache.commons.jelly.TagSupport;
import org.apache.commons.jelly.XMLOutput;
import org.xml.sax.SAXException;

/**
 * Writes out '&amp;nbsp;'.
 *
 * @author Kohsuke Kawaguchi
 */
public class NbspTag extends TagSupport {
    public void doTag(XMLOutput output) throws MissingAttributeException, JellyTagException {
        try {
            output.write("\u00A0"); // nbsp
        } catch (SAXException e) {
            throw new JellyTagException(e);
        }
    }
}
