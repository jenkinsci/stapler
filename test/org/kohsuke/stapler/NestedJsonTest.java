package org.kohsuke.stapler;

import junit.framework.TestCase;
import net.sf.json.JSONObject;

import java.util.Collections;

/**
 * Tests the instantiation of nested objects.
 *
 * @author Kohsuke Kawaguchi
 */
public class NestedJsonTest extends TestCase {
    public static final class Foo {
        public final Bar bar;

        // we test this with manual .stapler file
        // @DataBoundConstructor
        public Foo(Bar bar) {
            this.bar = bar;
        }
    }

    public static interface Bar {}

    public static final class BarImpl implements Bar {
        public final int i;

        // we test this with manual .stapler file
        // @DataBoundConstructor
        public BarImpl(int i) {
            this.i = i;
        }
    }

    public void test1() {
        JSONObject bar = new JSONObject();
        bar.put("i",123);
        JSONObject foo = new JSONObject();
        foo.put("bar",bar);
        foo.getJSONObject("bar").put("stapler-class",BarImpl.class.getName());

        RequestImpl req = new RequestImpl(null,new MockRequest(),Collections.EMPTY_LIST,null);
        Foo o = req.bindJSON(Foo.class, foo);
        assertTrue(o!=null);
        assertTrue(o.bar instanceof BarImpl);
        assertEquals(123, ((BarImpl)o.bar).i);
    }
}
