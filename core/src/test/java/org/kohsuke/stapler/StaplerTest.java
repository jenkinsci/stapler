package org.kohsuke.stapler;

import junit.framework.TestCase;

/**
 * @author Kohsuke Kawaguchi
 */
public class StaplerTest extends TestCase {
    public void testNormalization() {
        assertIdemPotent("/");
        assertIdemPotent("");
        assertIdemPotent("/foo");
        assertIdemPotent("/bar/");
        assertIdemPotent("zot/");
        testC12n("",".");
        testC12n("","foo/..");
        testC12n("foo", "foo/bar/./..");

        testC12n("/abc","/abc");
        testC12n("/abc/","/abc/");
        testC12n("/","/abc/../");
        testC12n("/","/abc/def/../../");
        testC12n("/def","/abc/../def");
        testC12n("/xxx","/../../../xxx");
    }

    private void testC12n(String expected, String input) {
        assertEquals(expected, Stapler.canonicalPath(input));
    }

    private void assertIdemPotent(String str) {
        assertEquals(str,Stapler.canonicalPath(str));
    }
}
