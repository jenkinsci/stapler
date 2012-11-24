package org.kohsuke.stapler.jelly.groovy;

import groovy.text.SimpleTemplateEngine;

import java.io.Writer;

/**
 * Base class for compiled GSP files.
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class GroovyServerPageScript extends StaplerClosureScript {
    private Writer out;
    protected GroovyServerPageScript() {
    }

    /**
     * {@link SimpleTemplateEngine} expects 'out' variable
     */
    public Writer getOut() {
        if (out==null) {
            out = ((JellyBuilder)getDelegate()).getOutput().asWriter();
        }
        return out;
    }
}
