package org.kohsuke.stapler.jelly.groovy;

import groovy.lang.GroovyObjectSupport;
import groovy.xml.QName;
import org.apache.commons.jelly.XMLOutput;
import org.xml.sax.SAXException;

/**
 * @author Kohsuke Kawaguchi
 */
public class Namespace extends GroovyObjectSupport {
    private final JellyBuilder builder;
    private final String nsUri;
    private final String prefix;

    // note that the mapping from nsUri to TagLibrary
    // may change depending on the scope, so we can't cache TagLibrary

    Namespace(JellyBuilder builder, String nsUri, String prefix) {
        this.builder = builder;
        this.nsUri = nsUri;
        this.prefix = prefix==null ? "" : prefix;
    }

    public Object invokeMethod(String localName, Object args) {
        builder.doInvokeMethod(new QName(nsUri,localName,prefix),args);
        return null;
    }

    public void startPrefixMapping(XMLOutput output) throws SAXException {
        output.startPrefixMapping(prefix,nsUri);
    }

    public void endPrefixMapping(XMLOutput output) throws SAXException {
        output.endPrefixMapping(prefix);

    }
}
