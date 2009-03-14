package org.kohsuke.stapler.jelly;

import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.MissingAttributeException;
import org.apache.commons.jelly.XMLOutput;
import org.jvnet.maven.jellydoc.annotation.Required;
import org.jvnet.maven.jellydoc.annotation.NoContent;

/**
 * Set the HTTP Content-Type header of the page.
 *
 * @author Kohsuke Kawaguchi
 */
@NoContent
public class ContentTypeTag extends AbstractStaplerTag {
    private String contentType;

    /**
     * The content-type value, such as "text/html".
     */
    @Required
    public void setValue(String contentType) {
        this.contentType = contentType;
    }

    public void doTag(XMLOutput output) throws MissingAttributeException, JellyTagException {
        getResponse().setContentType(contentType);
    }
}
