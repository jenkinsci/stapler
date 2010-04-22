package org.kohsuke.stapler.jelly;

import org.apache.commons.jelly.TagSupport;
import org.apache.commons.jelly.XMLOutput;
import org.jvnet.maven.jellydoc.annotation.Required;

/**
 * DTD-like expression that specifies the consraints on attribute appearances.
 *
 * <p>
 * This tag should be placed right inside {@link DocumentationTag}
 * to describe attributes of a tag.
 *
 * @author Kohsuke Kawaguchi
 */
public class AttributeConstraintsTag extends TagSupport {
    public void doTag(XMLOutput output) {
        // noop
    }

    /**
     * Constraint expression.
     */
    @Required
    public void setExpr(String v) {}
}

