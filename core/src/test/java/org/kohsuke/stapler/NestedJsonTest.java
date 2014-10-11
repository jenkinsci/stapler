package org.kohsuke.stapler;

import junit.framework.TestCase;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import javax.servlet.ServletException;
import java.util.Collections;
import java.util.List;

/**
 * Tests the instantiation of nested objects.
 *
 * @author Kohsuke Kawaguchi
 */
public class NestedJsonTest extends TestCase {
    public static final class Foo {
        public Bar bar;

        @DataBoundConstructor
        public Foo(Bar bar) {
            this.bar = bar;
        }
    }

    public static interface Bar {}

    public static final class BarImpl implements Bar {
        public final int i;

        @DataBoundConstructor
        public BarImpl(int i) {
            this.i = i;
        }
    }

    public void testCreateObject() throws Exception {
        Foo o = createRequest().bindJSON(Foo.class, createDataSet());

        assertNotNull(o);
        assertTrue(o.bar instanceof BarImpl);
        assertEquals(123, ((BarImpl)o.bar).i);
    }

    public void testInstanceFill() throws Exception {
        Foo o = new Foo(null);
        createRequest().bindJSON(o, createDataSet());
        
        assertTrue(o.bar instanceof BarImpl);
        assertEquals(123, ((BarImpl)o.bar).i);
    }

    public void testCreateList() throws Exception {
        // Just one
        List<Foo> list = createRequest().bindJSONToList(Foo.class, createDataSet());
        assertNotNull(list);
        assertEquals(1, list.size());
        assertTrue(list.get(0).bar instanceof BarImpl);
        assertEquals(123, ((BarImpl)list.get(0).bar).i);

        // Longer list
        JSONArray data = new JSONArray();
        data.add(createDataSet());
        data.add(createDataSet());
        data.add(createDataSet());

        list = createRequest().bindJSONToList(Foo.class, data);
        assertNotNull(list);
        assertEquals(3, list.size());
        assertEquals(123, ((BarImpl)list.get(2).bar).i);
    }

    private RequestImpl createRequest() throws Exception {
        return new RequestImpl(createStapler(), new MockRequest(), Collections.EMPTY_LIST,null);
    }

    private JSONObject createDataSet() {
        JSONObject bar = new JSONObject();
        bar.put("i",123);
        JSONObject foo = new JSONObject();
        foo.put("bar",bar);
        foo.getJSONObject("bar").put("$class", BarImpl.class.getName());

        return foo;
    }

    private Stapler createStapler() throws ServletException {
        Stapler stapler = new Stapler();
        stapler.init(new ServletConfigImpl());
        return stapler;
    }
}
