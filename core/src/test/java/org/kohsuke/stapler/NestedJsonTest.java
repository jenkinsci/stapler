package org.kohsuke.stapler;

import junit.framework.TestCase;
import net.sf.json.JSONObject;

import javax.servlet.ServletException;
import java.util.Collections;

/**
 * Tests the instantiation of nested objects.
 *
 * @author Kohsuke Kawaguchi
 */
public class NestedJsonTest extends TestCase {
    public static final class Foo {
        public Bar bar;

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

    public void testCreateObject() throws Exception {
        Foo o = createRequest().bindJSON(Foo.class, createDataSet());

        assertTrue(o!=null);
        assertTrue(o.bar instanceof BarImpl);
        assertEquals(123, ((BarImpl)o.bar).i);
    }

    public void testInstanceFill() throws Exception {
        Foo o = new Foo(null);
        createRequest().bindJSON(o, createDataSet());
        
        assertTrue(o.bar instanceof BarImpl);
        assertEquals(123, ((BarImpl)o.bar).i);
    }

    private RequestImpl createRequest() throws Exception {
        return new RequestImpl(createStapler(), new MockRequest(), Collections.EMPTY_LIST,null);
    }

    private JSONObject createDataSet() {
        JSONObject bar = new JSONObject();
        bar.put("i",123);
        JSONObject foo = new JSONObject();
        foo.put("bar",bar);
        foo.getJSONObject("bar").put("stapler-class", BarImpl.class.getName());
        return foo;
    }

    private Stapler createStapler() throws ServletException {
        Stapler stapler = new Stapler();
        stapler.init(new ServletConfigImpl());
        return stapler;
    }
}
