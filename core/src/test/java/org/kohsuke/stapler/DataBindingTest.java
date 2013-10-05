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
        Stapler s = new Stapler();
        s.setWebApp(new WebApp(null));
        RequestImpl req = new RequestImpl(s, new MockRequest(), Collections.<AncestorImpl>emptyList(), null);
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

    public static class SetterBinding {
        private int w,x,y,z;
        private Object o;
        private List<SetterBinding> children;

        @DataBoundConstructor
        public SetterBinding(int x, int y) {
            this.x = x;
            this.y = y;
        }

        /**
         * Setter not annotated with {@link DataBoundSetter}, so it should be ignored
         */
        public void setW(int w) {
            this.w = w;
        }

        @DataBoundSetter
        public void setZ(int z) {
            this.z = z;
        }

        /**
         * We expect y to be set through constructor
         */
        @DataBoundSetter
        public void setY(int y) {
            throw new IllegalArgumentException();
        }

        @DataBoundSetter
        public void setAnotherObject(Object o) {
            this.o = o;
        }

        @DataBoundSetter
        public void setChildren(List<SetterBinding> children) {
            this.children = children;
        }
    }

    public static class Point {
        int x,y;

        @DataBoundConstructor
        public Point(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    public void testSetterInvocation() {
        SetterBinding r = bind("{x:1,y:2,z:3,w:1, children:[{x:5,y:5,z:5},{x:6,y:6,z:6}], anotherObject:{stapler-class:'org.kohsuke.stapler.DataBindingTest$Point', x:1,y:1} }",SetterBinding.class);
        assertEquals(1,r.x);
        assertEquals(2,r.y);
        assertEquals(3,r.z);
        assertEquals(0,r.w);

        assertEquals(2,r.children.size());
        SetterBinding c1 = r.children.get(0);
        assertEquals(5, c1.x);
        assertEquals(5, c1.y);
        assertEquals(5, c1.z);

        SetterBinding c2 = r.children.get(1);
        assertEquals(6, c2.x);
        assertEquals(6, c2.y);
        assertEquals(6, c2.z);

        Point o = (Point)r.o;
        assertEquals(1,o.x);
        assertEquals(1,o.y);
    }

    public static abstract class Point3 {
        @DataBoundSetter
        private int x,y,z;

        public void assertValues() {
            assertEquals(1,x);
            assertEquals(2,y);
            assertEquals(3,z);
        }
    }

    public static class Point3Derived extends Point3 {
        @DataBoundConstructor
        public Point3Derived() {
        }
    }

    public void testFieldInjection() {
        Point3Derived r = bind("{x:1,y:2,z:3} }",Point3Derived.class);
        r.assertValues();
    }
}
