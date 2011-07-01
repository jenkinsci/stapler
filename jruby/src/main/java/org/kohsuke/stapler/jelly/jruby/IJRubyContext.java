package org.kohsuke.stapler.jelly.jruby;

import org.apache.commons.jelly.JellyContext;
import org.apache.commons.jelly.XMLOutput;

/**
 * @author Kohsuke Kawaguchi
 */
public interface IJRubyContext {
    JellyContext getJellyContext();
    void setJellyContext(JellyContext context);
    XMLOutput getOutput();
    void setOutput(XMLOutput output);
}
