package org.kohsuke.stapler.jelly.groovy;

import groovy.lang.Binding;
import org.apache.commons.jelly.JellyContext;
import org.apache.commons.jelly.XMLOutput;

/**
 * {@link Binding} that delegates to {@link JellyContext}.
 *
 * @author Kohsuke Kawaguchi
 */
public class JellyBinding extends Binding {
    private final JellyContext context;

    private final XMLOutput out;

    public JellyBinding(JellyContext context, XMLOutput out) {
        this.context = context;
        this.out = out;
    }

    public Object getVariable(String name) {
        return context.getVariable(name);
    }

    public void setVariable(String name, Object value) {
        context.setVariable(name,value);
    }
}
