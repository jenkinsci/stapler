package org.kohsuke.stapler.jelly;

import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.MissingAttributeException;
import org.apache.commons.jelly.XMLOutput;
import org.jvnet.maven.jellydoc.annotation.NoContent;
import org.jvnet.maven.jellydoc.annotation.Required;

/**
 * Adds an HTTP header to the response.
 *
 * @author Kohsuke Kawaguchi
 */
@NoContent
public class HeaderTag extends AbstractStaplerTag {
    private String name;
    private String value;

    /**
     * Header name.
     */
    @Required
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Header value.
     */
    @Required
    public void setValue(String value) {
        this.value = value;
    }

    public void doTag(XMLOutput output) throws JellyTagException {
        getResponse().addHeader(name,value);
    }
}
