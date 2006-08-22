package org.kohsuke.stapler.jelly.groovy;

import org.apache.commons.jelly.Script;
import org.apache.commons.jelly.JellyException;
import org.apache.commons.jelly.JellyContext;
import org.apache.commons.jelly.XMLOutput;
import org.apache.commons.jelly.JellyTagException;
import groovy.lang.Closure;

/**
 * {@link Script} that invokes a {@link Closure}.
 *
 * @author Kohsuke Kawaguchi
 */
public class ClosureScript implements Script {

    private final JellyBuilder builder;
    private final Closure closure;

    public ClosureScript(JellyBuilder builder, Closure closure) {
        this.builder = builder;
        this.closure = closure;
    }

    public Script compile() throws JellyException {
        return this;
    }

    public void run(JellyContext context, XMLOutput output) throws JellyTagException {
        JellyContext oldc = builder.setContext(context);
        XMLOutput oldo = builder.setOutput(output);
        try {
            closure.setDelegate(builder);
            closure.call();
        } finally {
            builder.setContext(oldc);
            builder.setOutput(oldo);
        }
    }
}
