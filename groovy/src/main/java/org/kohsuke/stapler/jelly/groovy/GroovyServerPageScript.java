package org.kohsuke.stapler.jelly.groovy;

import groovy.lang.Binding;
import groovy.lang.Script;
import org.kohsuke.stapler.framework.adjunct.AdjunctsInPage;
import org.kohsuke.stapler.framework.adjunct.NoSuchAdjunctException;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.Writer;

/**
 * Base class for compiled GSP files.
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class GroovyServerPageScript extends Script {
    protected GroovyServerPageScript() {
    }

    protected GroovyServerPageScript(Binding binding) {
        super(binding);
    }

    public Writer getOut() {
        return (Writer)getProperty("out");
    }

    public void adjunct(String name) throws IOException, SAXException {
        try {
            AdjunctsInPage aip = AdjunctsInPage.get();
            aip.generate(null,name);
        } catch (NoSuchAdjunctException e) {
            // that's OK.
        }
    }
}
