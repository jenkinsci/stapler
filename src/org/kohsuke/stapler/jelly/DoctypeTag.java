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

    public DoctypeTag(String publicId, String systemId) {
        this.publicId = publicId;
        this.systemId = systemId;
    }

    public void doTag(XMLOutput output) throws JellyTagException {
        try {
            getResponse().getWriter().println("<!DOCTYPE html PUBLIC \""+publicId+"\" \""+systemId+"\">");
        } catch (IOException e) {
            throw new JellyTagException(e);
        }
    }
}
