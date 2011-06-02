package org.kohsuke.stapler.jelly.jruby;

import org.kohsuke.stapler.*;

/**
 * @author Kohsuke Kawaguchi
 */
public abstract class StaplerJRubyTestCase extends StaplerTestCase {
    private static JRubyScriptProvider jruby = null;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    protected JRubyScriptProvider getScriptProvider() {
        initializeJRubyScriptProvider();
        return jruby;
    }

    private static synchronized void initializeJRubyScriptProvider() {
        if (jruby == null) {
            jruby = new JRubyScriptProvider();
        }
    }
}
