package org.kohsuke.stapler.jelly;

import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.XMLOutput;
import org.apache.commons.jelly.LocationAware;
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
public class StructuredMessageFormatTag extends AbstractStaplerTag implements LocationAware {
    private final List<Object> arguments = new ArrayList<Object>();

    private String key;
    private ResourceBundle rb;

    @Required
    public void setKey(String resourceKey) {
        this.key = resourceKey;
    }

    public void addArgument(Object o) {
        this.arguments.add(o);
    }

    public void doTag(XMLOutput output) throws JellyTagException {
        try {
            arguments.clear();
            invokeBody(output);

            output.write(rb.format(LocaleProvider.getLocale(), key,arguments.toArray()));
        } catch (SAXException e) {
            throw new JellyTagException("could not write the XMLOutput",e);
        } finally {
            arguments.clear(); // don't keep heavy objects in memory for too long
        }
    }

    public int getLineNumber() {
        return -1;
    }

    public void setLineNumber(int lineNumber) {
    }

    public int getColumnNumber() {
        return -1;
    }

    public void setColumnNumber(int columnNumber) {
    }

    public String getFileName() {
        return null;
    }

    public void setFileName(String fileName) {
        rb = ResourceBundle.load(fileName);
    }

    public String getElementName() {
        return null;
    }

    public void setElementName(String elementName) {
    }
}
