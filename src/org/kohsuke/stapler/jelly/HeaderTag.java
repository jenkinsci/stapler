package org.kohsuke.stapler.jelly;

import org.apache.commons.jelly.XMLOutput;
import org.apache.commons.jelly.MissingAttributeException;
import org.apache.commons.jelly.JellyTagException;

/**
 * Adds an HTTP header to the response.
 *
 * @author Kohsuke Kawaguchi
 */
public class HeaderTag extends AbstractStaplerTag {
    private String name;
    private String value;

    public void setName(String name) {
        this.name = name;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void doTag(XMLOutput output) throws MissingAttributeException, JellyTagException {
        getResponse().addHeader(name,value);
    }
}
