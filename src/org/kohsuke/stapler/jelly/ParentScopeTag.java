package org.kohsuke.stapler.jelly;

import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.TagSupport;
import org.apache.commons.jelly.XMLOutput;

/**
 * Executes the body in the parent scope.
 * This is useful for creating a 'local' scope.
 *
 * @author Kohsuke Kawaguchi
 */
public class ParentScopeTag extends TagSupport {
    public void doTag(XMLOutput output) throws JellyTagException {
        getBody().run(context.getParent(), output);
    }
}
