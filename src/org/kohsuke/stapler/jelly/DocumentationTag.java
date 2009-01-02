package org.kohsuke.stapler.jelly;

import org.apache.commons.jelly.TagSupport;
import org.apache.commons.jelly.XMLOutput;

/**
 * Documentation tag.
 *
 * <p>
 * At the runtime, this is no-op.
 *
 * @author Kohsuke Kawaguchi
 */
public class DocumentationTag extends TagSupport {
    public void doTag(XMLOutput output) {
        // noop
    }
}
