package org.kohsuke.stapler.jelly;

import org.apache.commons.jelly.XMLOutput;
import org.apache.commons.jelly.JellyTagException;
import org.jvnet.maven.jellydoc.annotation.NoContent;
import org.jvnet.maven.jellydoc.annotation.Required;

import java.io.IOException;

/**
 * Writes out DOCTYPE declaration.
 * 
 * @author Kohsuke Kawaguchi
 */
@NoContent
public class DoctypeTag extends AbstractStaplerTag {
    private String publicId;
    private String systemId;

    @Required
    public void setPublicId(String publicId) {
        this.publicId = publicId;
    }

    @Required
    public void setSystemId(String systemId) {
        this.systemId = systemId;
    }

    public void doTag(XMLOutput output) throws JellyTagException {
        try {
            getResponse().getOutputStream().println("<!DOCTYPE html PUBLIC \""+publicId+"\" \""+systemId+"\">");
        } catch (IOException e) {
            throw new JellyTagException(e);
        }
    }
}
