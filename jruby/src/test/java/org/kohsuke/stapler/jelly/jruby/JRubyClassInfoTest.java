package org.kohsuke.stapler.jelly.jruby;

import junit.framework.TestCase;

/**
 * @author Kohsuke Kawaguchi
 */
public class JRubyClassInfoTest extends TestCase {
    public void testCamelize() throws Exception {
        assertEquals("foo_bar_zot",JRubyClassInfo.decamelize("FooBarZot"));
        assertEquals("http_connection",JRubyClassInfo.decamelize("HTTPConnection"));
        assertEquals("managed_jdk_listener",JRubyClassInfo.decamelize("ManagedJDKListener"));
        assertEquals("managed_jdk15_listener",JRubyClassInfo.decamelize("ManagedJDK15Listener"));
        assertEquals("abc000thunder",JRubyClassInfo.decamelize("Abc000thunder"));
    }

    public void testGetResource() throws Exception {

    }
}
