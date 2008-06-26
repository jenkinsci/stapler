package org.kohsuke.stapler.jelly;

import org.apache.commons.jelly.XMLOutput;
import org.apache.commons.jelly.JellyTagException;

import java.io.IOException;

/**
 * Writes out DOCTYPE declaration.
 * 
 * @author Kohsuke Kawaguchi
 */
public class DoctypeTag extends AbstractStaplerTag {
    private String publicId;
    private String systemId;

    public void setPublicId(String publicId) {
        this.publicId = publicId;
    }

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
