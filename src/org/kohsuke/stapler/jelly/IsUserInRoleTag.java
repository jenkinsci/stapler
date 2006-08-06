package org.kohsuke.stapler.jelly;

import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.MissingAttributeException;
import org.apache.commons.jelly.XMLOutput;

/**
 * @author Kohsuke Kawaguchi
 */
public class IsUserInRoleTag extends AbstractStaplerTag {
    private String role;

    /**
     * The name of the role against which the user is checked.
     */
    public void setRole(String role) {
        this.role = role;
    }

    public void doTag(XMLOutput output) throws MissingAttributeException, JellyTagException {
        if(getRequest().isUserInRole(role))
            getBody().run(getContext(),output);
    }
}
