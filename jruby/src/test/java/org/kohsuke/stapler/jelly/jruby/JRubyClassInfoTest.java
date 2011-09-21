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

        assertEquals("nested_nested/http_connection",JRubyClassInfo.decamelize("NestedNested/HTTPConnection"));
        assertEquals("a/http_connection",JRubyClassInfo.decamelize("A/HTTPConnection"));
        assertEquals("a/http_connection",JRubyClassInfo.decamelize("a/HTTPConnection"));
        assertEquals("aa/http_connection",JRubyClassInfo.decamelize("AA/HTTPConnection"));
        assertEquals("aa/http_connection",JRubyClassInfo.decamelize("Aa/HTTPConnection"));
        assertEquals("aa_a/http_connection",JRubyClassInfo.decamelize("AaA/HTTPConnection"));
        assertEquals("/http_connection",JRubyClassInfo.decamelize("/HTTPConnection"));
        assertEquals("nested/a",JRubyClassInfo.decamelize("Nested/A"));
        assertEquals("nested/a",JRubyClassInfo.decamelize("Nested/a"));
        assertEquals("nested/",JRubyClassInfo.decamelize("Nested/"));
        assertEquals("/",JRubyClassInfo.decamelize("/"));
        assertEquals("",JRubyClassInfo.decamelize(""));
        try {
            JRubyClassInfo.decamelize(null);
            fail();
        } catch (NullPointerException npe) {
        }
    }

    public void testGetResource() throws Exception {

    }
}
