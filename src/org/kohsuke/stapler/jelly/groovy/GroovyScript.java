package org.kohsuke.stapler.jelly.groovy;

import groovy.lang.Closure;
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
        final JellyBuilder builder = new JellyBuilder(context, output);

        // there seems to be no way to run a script with the delegate set,
        // Groovy script needs to put the entire code into the "jelly {...}" block
        // to run the code inside JellyBuilder.
        
        JellyBinding binding = new JellyBinding(context,output);
        binding.setProperty("jelly",new Closure(builder) {
            public Object call(Object[] args) {
                Closure closure = (Closure) args[0];
                closure.setDelegate(builder);
                return closure.call();
            }
        });

        InvokerHelper.createScript(clazz,binding).run();
    }
}
