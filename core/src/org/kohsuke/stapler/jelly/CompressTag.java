package org.kohsuke.stapler.jelly;

import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.XMLOutput;
import org.apache.commons.jelly.Script;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

/**
 * Outer-most wrapper tag to indicate that the gzip compression is desirable
 * for this output.
 *
 * @author Kohsuke Kawaguchi
 */
public class CompressTag extends AbstractStaplerTag {
    /**
     * Doesn't particularly do anything as the actual processing
     * happens at {@link DefaultScriptInvoker#invokeScript(StaplerRequest, StaplerResponse, Script, Object)} 
     */
    public void doTag(XMLOutput output) throws JellyTagException {
        getBody().run(getContext(),output);
    }
}
