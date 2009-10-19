package org.kohsuke.stapler.jelly;

import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.XMLOutput;
import org.jvnet.localizer.LocaleProvider;
import org.jvnet.maven.jellydoc.annotation.Required;
import org.xml.sax.SAXException;

import java.util.ArrayList;
import java.util.List;

/**
 * Format message from a resource, but by using a nested children as arguments, instead of just using expressions.
 * 
 * @author Kohsuke Kawaguchi
 */
public class StructuredMessageFormatTag extends AbstractStaplerTag {
    private final List<Object> arguments = new ArrayList<Object>();

    private String resourceKey;

    @Required
    public void setKey(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    public void addArgument(Object o) {
        this.arguments.add(o);
    }

    public void doTag(XMLOutput output) throws JellyTagException {
        try {
            arguments.clear();
            invokeBody(output);

            ResourceBundle rb = ResourceBundle.load(context.getCurrentURL());
            output.write(rb.format(LocaleProvider.getLocale(),resourceKey,arguments.toArray()));
        } catch (SAXException e) {
            throw new JellyTagException("could not write the XMLOutput",e);
        } finally {
            arguments.clear(); // don't keep heavy objects in memory for too long
        }
    }
}
