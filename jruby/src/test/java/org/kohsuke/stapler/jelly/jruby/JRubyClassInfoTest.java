package org.kohsuke.stapler.jelly.jruby;

import junit.framework.TestCase;

import static org.kohsuke.stapler.jelly.jruby.RubyKlassNavigator.decamelize;

/**
 * @author Kohsuke Kawaguchi
 */
public class JRubyClassInfoTest extends TestCase {
    public void testCamelize() throws Exception {
        assertEquals("foo_bar_zot", decamelize("FooBarZot"));
        assertEquals("http_connection",decamelize("HTTPConnection"));
        assertEquals("managed_jdk_listener",decamelize("ManagedJDKListener"));
        assertEquals("managed_jdk15_listener",decamelize("ManagedJDK15Listener"));
        assertEquals("abc000thunder",decamelize("Abc000thunder"));

        assertEquals("nested_nested/http_connection",decamelize("NestedNested/HTTPConnection"));
        assertEquals("a/http_connection",decamelize("A/HTTPConnection"));
        assertEquals("a/http_connection",decamelize("a/HTTPConnection"));
        assertEquals("aa/http_connection",decamelize("AA/HTTPConnection"));
        assertEquals("aa/http_connection",decamelize("Aa/HTTPConnection"));
        assertEquals("aa_a/http_connection",decamelize("AaA/HTTPConnection"));
        assertEquals("/http_connection",decamelize("/HTTPConnection"));
        assertEquals("nested/a",decamelize("Nested/A"));
        assertEquals("nested/a",decamelize("Nested/a"));
        assertEquals("nested/",decamelize("Nested/"));
        assertEquals("/",decamelize("/"));
        assertEquals("",decamelize(""));
        try {
            decamelize(null);
            fail();
        } catch (NullPointerException npe) {
        }
    }

    public void testGetResource() throws Exception {

    }
}
