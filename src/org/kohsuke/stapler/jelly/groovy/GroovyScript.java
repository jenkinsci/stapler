package org.kohsuke.stapler.jelly.groovy;

import org.apache.commons.jelly.JellyContext;
import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.Script;
import org.apache.commons.jelly.XMLOutput;
import org.codehaus.groovy.runtime.InvokerHelper;

/**
 * @author Kohsuke Kawaguchi
 */
public class GroovyScript implements Script {
    /**
     * Compiled Groovy class.
     */
    private final Class clazz;

    public GroovyScript(Class clazz) {
        this.clazz = clazz;
    }
    
    public Script compile() {
        return this;
    }

    public void run(JellyContext context, XMLOutput output) throws JellyTagException {
        InvokerHelper.createScript(clazz, new JellyBinding(context,output)).run();
    }
}
