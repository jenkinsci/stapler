package org.kohsuke.stapler.jelly;

import org.apache.commons.jelly.TagSupport;
import org.apache.commons.jelly.XMLOutput;
import org.jvnet.maven.jellydoc.annotation.Required;

/**
 * Documentation for an attribute of a Jelly tag file.
 *
 * <p>
 * This tag should be placed right inside {@link DocumentationTag}
 * to describe attributes of a tag. The body would describe
 * the meaning of an attribute in a natural language.
 * The description text can also use
 * <a href="http://textile.thresholdstate.com/">Textile markup</a>
 *
 * @author Kohsuke Kawaguchi
 */
public class AttributeTag extends TagSupport {
    public void doTag(XMLOutput output) {
        // noop
    }

    /**
     * Name of the attribute.
     */
    @Required
    public void setName(String v) {}

    /**
     * If the attribute is required, specify use="required".
     * (This is modeled after XML Schema attribute declaration.)
     *
     * <p>
     * By default, use="optional" is assumed.
     */
    public void setUse(String v) {}

    /**
     * If it makes sense, describe the Java type that the attribute
     * expects as values.
     */
    public void setType(String v) {}

    /**
     * If the attribute is deprecated, set to true.
     * Use of the deprecated attribute will cause a warning.
     */
    public void setDeprecated(boolean v) {}
}

