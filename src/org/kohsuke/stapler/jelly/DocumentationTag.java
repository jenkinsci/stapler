package org.kohsuke.stapler.jelly;

import org.apache.commons.jelly.TagSupport;
import org.apache.commons.jelly.XMLOutput;

/**
 * Documentation for a Jelly tag file.
 *
 * <p>
 * This tag should be placed right inside the root element once,
 * to describe the tag and its attributes. Maven-stapler-plugin
 * picks up this tag and generate schemas and documentations.
 *
 * <p>
 * The description text inside this tag can also use
 * <a href="http://textile.thresholdstate.com/">Textile markup</a>
 *
 * @author Kohsuke Kawaguchi
 */
public class DocumentationTag extends TagSupport {
    public void doTag(XMLOutput output) {
        // noop
    }
}
