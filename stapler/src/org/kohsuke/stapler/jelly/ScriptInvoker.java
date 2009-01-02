package org.kohsuke.stapler.jelly;

import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.Script;

import java.io.IOException;

/**
 * Pluggability point for controlling how scripts get executed.
 *
 * <p>
 * TODO: how does the user specify it?
 * @author Kohsuke Kawaguchi
 */
public interface ScriptInvoker {
    void invokeScript(StaplerRequest req, StaplerResponse rsp, Script script, Object it) throws IOException, JellyTagException;
}
