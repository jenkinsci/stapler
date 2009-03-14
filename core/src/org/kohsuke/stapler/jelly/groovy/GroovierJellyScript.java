package org.kohsuke.stapler.jelly.groovy;

import org.apache.commons.jelly.JellyContext;
import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.Script;
import org.apache.commons.jelly.XMLOutput;
import org.codehaus.groovy.runtime.InvokerHelper;
import groovy.lang.Binding;

/**
 * Wraps a Groovy-driven Jelly script into {@link Script}
 * (so that it can be called from other Jelly scripts.) 
 *
 * @author Kohsuke Kawaguchi
 */
public class GroovierJellyScript implements Script {
    /**
     * Compiled Groovy class.
     */
    private final Class clazz;

    public GroovierJellyScript(Class clazz) {
        this.clazz = clazz;
    }
    
    public Script compile() {
        return this;
    }

    public void run(JellyContext context, XMLOutput output) throws JellyTagException {
        run(new JellyBuilder(context, output));
    }

    public void run(JellyBuilder builder) {
        GroovyClosureScript gcs = (GroovyClosureScript) InvokerHelper.createScript(clazz, new Binding());
        gcs.setDelegate(builder);
        gcs.run();
    }
}
