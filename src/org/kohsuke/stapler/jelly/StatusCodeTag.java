package org.kohsuke.stapler.jelly;

import org.apache.commons.jelly.XMLOutput;
import org.apache.commons.jelly.JellyTagException;

/**
 * Sets HTTP status code.
 *
 * <p>
 * This is generally useful for programatically creating the error page.
 *
 * @author Kohsuke Kawaguchi
 */
public class StatusCodeTag extends AbstractStaplerTag {
    private int code;

    public void setValue(int code) {
        this.code = code;
    }

    public void doTag(XMLOutput output) throws JellyTagException {
        getResponse().setStatus(code);
    }
}
