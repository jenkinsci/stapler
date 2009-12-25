package org.kohsuke.stapler.jelly;

import org.apache.commons.jelly.XMLOutput;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.Script;

import java.io.IOException;

/**
 * Pluggability point for controlling how scripts get executed.
 *
 * @author Kohsuke Kawaguchi
 * @see JellyFacet#scriptInvoker
 */
public interface ScriptInvoker {
    /**
     * Invokes the script and generates output to {@link StaplerResponse#getOutputStream()}.
     */
    void invokeScript(StaplerRequest req, StaplerResponse rsp, Script script, Object it) throws IOException, JellyTagException;

    /**
     * Invokes the script and generates output to the specified output
     */
    void invokeScript(StaplerRequest req, StaplerResponse rsp, Script script, Object it, XMLOutput out) throws IOException, JellyTagException;
}
