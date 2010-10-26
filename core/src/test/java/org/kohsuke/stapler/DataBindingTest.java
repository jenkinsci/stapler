package org.kohsuke.stapler;

import junit.framework.TestCase;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.net.Proxy;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

/**
 * @author Kohsuke Kawaguchi
 */
public class DataBindingTest extends TestCase {
    public class Data {
        public int a;
        String myB;

        public void setB(String s) {
            myB = s;
        }
    }
    public void test1() {
        JSONObject json = new JSONObject();
        json.put("a",123);
        json.put("b","string");

        Data data = bind(json, new Data());
        assertEquals(123,data.a);
        assertEquals("string",data.myB);
    }

    public class DataEnumSet {
        public EnumSet<Proxy.Type> set;
    }

    public void testEnumSet() {
        JSONObject json = new JSONObject();
        json.put("DIRECT",false);
        json.put("HTTP",true);
        json.put("SOCKS",null);

        JSONObject container = new JSONObject();
        container.put("set",json);

        DataEnumSet data = bind(container,new DataEnumSet());
        assertTrue(data.set.contains(Proxy.Type.HTTP));
        assertFalse(data.set.contains(Proxy.Type.DIRECT));
        assertFalse(data.set.contains(Proxy.Type.SOCKS));

    }

    public void testFromStaplerMethod() throws Exception {
        MockRequest mr = new MockRequest();
        mr.getParameterMap().put("a","123");
        mr.getParameterMap().put("b","string");
        RequestImpl req = new RequestImpl(new Stapler(), mr, Collections.<AncestorImpl>emptyList(), null);
        new Function.InstanceFunction(getClass().getMethod("doFromStaplerMethod",StaplerRequest.class,int.class,Binder.class))
                .bindAndInvoke(this,req,null);
    }

    public void doFromStaplerMethod(StaplerRequest req, @QueryParameter int a, Binder b) {
        assertEquals(123,a);
        assertSame(req,b.req);
        assertEquals("string",b.b);

    }

    public static class Binder {
        StaplerRequest req;
        String b;

        @CapturedParameterNames({"req","b"})
        public static Binder fromStapler(StaplerRequest req, @QueryParameter String b) {
            Binder r = new Binder();
            r.req = req;
            r.b = b;
            return r;
        }
    }

    public void testCustomConverter() throws Exception {
        ReferToObjectWithCustomConverter r = bind("{data:'1,2'}", ReferToObjectWithCustomConverter.class);
        assertEquals(r.data.x,1);
        assertEquals(r.data.y,2);
    }

    public static class ReferToObjectWithCustomConverter {
        final ObjectWithCustomConverter data;

        @DataBoundConstructor
        public ReferToObjectWithCustomConverter(ObjectWithCustomConverter data) {
            this.data = data;
        }
    }

    public void testNullToFalse() throws Exception {
        TwoBooleans r = bind("{a:false}", TwoBooleans.class);
        assertFalse(r.a);
        assertFalse(r.b);
    }

    public static class TwoBooleans {
        private boolean a,b;

        @DataBoundConstructor
        public TwoBooleans(boolean a, boolean b) {
            this.a = a;
            this.b = b;
        }
    }

    public void testScalarToArray() throws Exception {
        ScalarToArray r = bind("{a:'x',b:'y',c:5,d:6}", ScalarToArray.class);
        assertEquals("x",r.a[0]);
        assertEquals("y",r.b.get(0));
        assertEquals(5,(int)r.c[0]);
        assertEquals(6,(int)r.d.get(0));
    }

    public static class ScalarToArray {
        private String[] a;
        private List<String> b;
        private Integer[] c;
        private List<Integer> d;

        @DataBoundConstructor
        public ScalarToArray(String[] a, List<String> b, Integer[] c, List<Integer> d) {
            this.a = a;
            this.b = b;
            this.c = c;
            this.d = d;
        }
    }

    private <T> T bind(String json, Class<T> type) {
        RequestImpl req = new RequestImpl(new Stapler(), new MockRequest(), Collections.<AncestorImpl>emptyList(), null);
        return req.bindJSON(type, JSONObject.fromObject(json));
    }

    private <T> T bind(JSONObject json, T bean) {
        RequestImpl req = new RequestImpl(new Stapler(), new MockRequest(), Collections.<AncestorImpl>emptyList(), null);
        req.bindJSON(bean,json);
        return bean;
    }

    public static class RawBinding {
        JSONObject x;
        JSONArray y;
        @DataBoundConstructor
        public RawBinding(JSONObject x, JSONArray y) {
            this.x = x;
            this.y = y;
        }
    }

    public void testRaw() {
        RawBinding r = bind("{x:{a:true,b:1},y:[1,2,3]}", RawBinding.class);

        // array coersion on y
        RawBinding r2 = bind("{x:{a:true,b:1},y:{p:true}}", RawBinding.class);
        JSONObject o = (JSONObject)r2.y.get(0);
        assertTrue(o.getBoolean("p"));

        // array coersion on y
        RawBinding r3 = bind("{x:{a:true,b:1},y:true}", RawBinding.class);
        assertTrue((Boolean)r3.y.get(0));
    }
}
