package org.kohsuke.stapler.jelly.jruby;

import org.kohsuke.stapler.test.AbstractStaplerTest;

/**
 * @author Kohsuke Kawaguchi
 * @author Hiroshi Nakamura
 */
public abstract class StaplerJRubyTestCase extends AbstractStaplerTest {
    protected JRubyFacet facet;

    @Override
    protected void setUp() throws Exception {
        facet = new JRubyFacet();
        super.setUp();
    }
}
