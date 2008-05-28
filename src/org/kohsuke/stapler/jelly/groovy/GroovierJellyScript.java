package org.kohsuke.stapler.jelly.groovy;

import org.apache.commons.jelly.JellyContext;
import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.Script;
import org.apache.commons.jelly.XMLOutput;
import org.codehaus.groovy.runtime.InvokerHelper;

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
        final JellyBuilder builder = new JellyBuilder(context, output);

        JellyBinding binding = new JellyBinding(context,output);
        binding.setProperty("builder",builder);
        GroovyClosureScript gcs = (GroovyClosureScript)InvokerHelper.createScript(clazz, binding);
        gcs.setDelegate(builder);
        gcs.run();
    }
}
