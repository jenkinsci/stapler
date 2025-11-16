package org.kohsuke.stapler;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.annotation.PostConstruct;
import java.lang.reflect.Type;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.junit.jupiter.api.Test;

/**
 * @author Kohsuke Kawaguchi
 */
class DataBindingTest {

    public class Data {
        public int a;
        String myB;

        public void setB(String s) {
            myB = s;
        }
    }

    @Test
    void test1() {
        JSONObject json = new JSONObject();
        json.put("a", 123);
        json.put("b", "string");

        Data data = bind(json, new Data());
        assertEquals(123, data.a);
        assertEquals("string", data.myB);
    }

    @Test
    void testMismatchingTypes() {
        JSONObject json = new JSONObject();
        json.put("b", new String[] {"v1", "v2"});

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> bind(json, new Data()));
        assertEquals(
                "Error binding field b: Got type array but no lister class found for type class java.lang.String",
                e.getMessage());
    }

    public class DataEnumSet {
        public EnumSet<Proxy.Type> set;
    }

    @Test
    void testEnumSet() {
        JSONObject json = new JSONObject();
        json.put("DIRECT", false);
        json.put("HTTP", true);
        json.put("SOCKS", null);

        JSONObject container = new JSONObject();
        container.put("set", json);

        DataEnumSet data = bind(container, new DataEnumSet());
        assertTrue(data.set.contains(Proxy.Type.HTTP));
        assertFalse(data.set.contains(Proxy.Type.DIRECT));
        assertFalse(data.set.contains(Proxy.Type.SOCKS));
    }

    @Test
    void testFromStaplerMethod() throws Exception {
        MockRequest mr = new MockRequest() {
            @Override
            public String getContentType() {
                return "text/html";
            }
        };
        mr.parameters.put("a", "123");
        mr.parameters.put("b", "string");
        RequestImpl req = new RequestImpl(new Stapler(), mr, Collections.emptyList(), null);
        new Function.InstanceFunction(
                        getClass().getMethod("doFromStaplerMethod", StaplerRequest2.class, int.class, Binder.class))
                .bindAndInvoke(this, req, null);
        assertEquals(
                42,
                new Function.InstanceFunction(getClass().getMethod("doStaticMethod")).bindAndInvoke(this, req, null));
    }

    public void doFromStaplerMethod(StaplerRequest2 req, @QueryParameter int a, Binder b) {
        assertEquals(123, a);
        assertSame(req, b.req);
        assertEquals("string", b.b);
    }

    public static int doStaticMethod() {
        return 42;
    }

    public static class Binder {
        StaplerRequest2 req;
        String b;

        @CapturedParameterNames({"req", "b"})
        public static Binder fromStapler(StaplerRequest2 req, @QueryParameter String b) {
            Binder r = new Binder();
            r.req = req;
            r.b = b;
            return r;
        }
    }

    @Test
    void testCustomConverter() {
        ReferToObjectWithCustomConverter r = bind("{data:'1,2'}", ReferToObjectWithCustomConverter.class);
        assertEquals(1, r.data.x);
        assertEquals(2, r.data.y);
    }

    public static class ReferToObjectWithCustomConverter {
        final ObjectWithCustomConverter data;

        @SuppressWarnings("checkstyle:redundantmodifier")
        @DataBoundConstructor
        public ReferToObjectWithCustomConverter(ObjectWithCustomConverter data) {
            this.data = data;
        }
    }

    @Test
    void testNullToFalse() {
        TwoBooleans r = bind("{a:false}", TwoBooleans.class);
        assertFalse(r.a);
        assertFalse(r.b);
    }

    public static class TwoBooleans {
        private boolean a, b;

        @SuppressWarnings("checkstyle:redundantmodifier")
        @DataBoundConstructor
        public TwoBooleans(boolean a, boolean b) {
            this.a = a;
            this.b = b;
        }
    }

    @Test
    void testScalarToArray() {
        ScalarToArray r = bind("{a:'x',b:'y',c:5,d:6}", ScalarToArray.class);
        assertEquals("x", r.a[0]);
        assertEquals("y", r.b.get(0));
        assertEquals(Integer.valueOf(5), r.c[0]);
        assertEquals(Integer.valueOf(6), r.d.get(0));
    }

    public static class ScalarToArray {
        private String[] a;
        private List<String> b;
        private Integer[] c;
        private List<Integer> d;

        @DataBoundConstructor
        @SuppressWarnings("checkstyle:redundantmodifier")
        public ScalarToArray(String[] a, List<String> b, Integer[] c, List<Integer> d) {
            this.a = a;
            this.b = b;
            this.c = c;
            this.d = d;
        }
    }

    private <T> T bind(String json, Class<T> type) {
        return bind(json, type, BindInterceptor.NOOP);
    }

    private <T> T bind(String json, Class<T> type, BindInterceptor bi) {
        RequestImpl req = createFakeRequest();
        req.setBindInterceptor(bi);
        return req.bindJSON(type, JSONObject.fromObject(json));
    }

    private RequestImpl createFakeRequest() {
        Stapler s = new Stapler();
        s.setWebApp(new WebApp(new MockServletContext()));
        return new RequestImpl(s, new MockRequest(), Collections.emptyList(), null);
    }

    private <T> T bind(JSONObject json, T bean) {
        RequestImpl req = createFakeRequest();
        req.bindJSON(bean, json);
        return bean;
    }

    public static class RawBinding {
        JSONObject x;

        JSONArray y;

        @DataBoundConstructor
        @SuppressWarnings("checkstyle:redundantmodifier")
        public RawBinding(JSONObject x, JSONArray y) {
            this.x = x;
            this.y = y;
        }
    }

    @Test
    void testRaw() {
        RawBinding r = bind("{x:{a:true,b:1},y:[1,2,3]}", RawBinding.class);

        // array coercion on y
        RawBinding r2 = bind("{x:{a:true,b:1},y:{p:true}}", RawBinding.class);
        JSONObject o = (JSONObject) r2.y.get(0);
        assertTrue(o.getBoolean("p"));

        // array coercion on y
        RawBinding r3 = bind("{x:{a:true,b:1},y:true}", RawBinding.class);
        assertTrue((Boolean) r3.y.get(0));
    }

    public static class SetterBinding {
        private int w, x, y, z;
        private Object o;
        private List<SetterBinding> children;

        @DataBoundConstructor
        @SuppressWarnings("checkstyle:redundantmodifier")
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
        int x, y;

        @DataBoundConstructor
        @SuppressWarnings("checkstyle:redundantmodifier")
        public Point(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            Point rhs = (Point) o;
            return x == rhs.x && y == rhs.y;
        }

        @Override
        public int hashCode() {
            int result = x;
            result = 31 * result + y;
            return result;
        }
    }

    @Test
    void testSetterInvocation() {
        SetterBinding r = bind(
                "{x:1,y:2,z:3,w:1, children:[{x:5,y:5,z:5},{x:6,y:6,z:6}], anotherObject:{$class:'org.kohsuke.stapler.DataBindingTest$Point', x:1,y:1} }",
                SetterBinding.class);
        assertEquals(1, r.x);
        assertEquals(2, r.y);
        assertEquals(3, r.z);
        assertEquals(0, r.w);

        assertEquals(2, r.children.size());
        SetterBinding c1 = r.children.get(0);
        assertEquals(5, c1.x);
        assertEquals(5, c1.y);
        assertEquals(5, c1.z);

        SetterBinding c2 = r.children.get(1);
        assertEquals(6, c2.x);
        assertEquals(6, c2.y);
        assertEquals(6, c2.z);

        Point o = (Point) r.o;
        assertEquals(1, o.x);
        assertEquals(1, o.y);
    }

    public abstract static class Point3 {
        @DataBoundSetter
        private int x, y, z;

        int post = 1;

        public void assertValues() {
            assertEquals(1, x);
            assertEquals(2, y);
            assertEquals(3, z);
        }

        @PostConstruct
        private void post1() {
            post += 4;
        }
    }

    public static class Point3Derived extends Point3 {
        @DataBoundConstructor
        @SuppressWarnings("checkstyle:redundantmodifier")
        public Point3Derived() {}

        @PostConstruct
        private void post1() {
            post *= 2;
        }
    }

    @Test
    void testFieldInjection() {
        Point3Derived r = bind("{x:1,y:2,z:3} }", Point3Derived.class);
        r.assertValues();
        assertEquals(10, r.post);
    }

    @Test
    void testInterceptor1() {
        String r = bind("{x:1}", String.class, new BindInterceptor() {
            @Override
            public Object onConvert(Type targetType, Class targetTypeErasure, Object jsonSource) {
                assertEquals(String.class, targetType);
                assertEquals(String.class, targetTypeErasure);
                return String.valueOf(((JSONObject) jsonSource).getInt("x"));
            }
        });
        assertEquals("1", r);
    }

    @Test
    void testInterceptor2() {
        RequestImpl req = createFakeRequest();
        req.setBindInterceptor(new BindInterceptor() {
            @Override
            public Object onConvert(Type targetType, Class targetTypeErasure, Object jsonSource) {
                if (targetType == String.class) {
                    return String.valueOf(((JSONObject) jsonSource).getInt("x"));
                }
                return DEFAULT;
            }
        });

        String[] r = (String[]) req.bindJSON(String[].class, String[].class, JSONArray.fromObject("[{x:1},{x:2}]"));
        assertArrayEquals(new String[] {"1", "2"}, r);
    }

    @Test
    void testInterceptor3() {
        RequestImpl req = createFakeRequest();
        req.setBindInterceptor(new BindInterceptor() {
            @Override
            public Object instantiate(Class actualType, JSONObject json) {
                if (actualType.equals(Point.class)) {
                    return new Point(1, 2);
                }
                return DEFAULT;
            }
        });

        Object[] r = (Object[]) req.bindJSON(
                Object[].class, Object[].class, JSONArray.fromObject("[{$class:'" + Point.class.getName() + "'}]"));
        assertArrayEquals(new Object[] {new Point(1, 2)}, r);
    }

    public static class AsymmetricProperty {
        private final List<Integer> items = new ArrayList<>();

        @DataBoundConstructor
        @SuppressWarnings("checkstyle:redundantmodifier")
        public AsymmetricProperty() {}

        public List<Integer> getItems() {
            return items;
        }

        @DataBoundSetter
        public void setItems(Collection<Integer> v) {
            items.clear();
            items.addAll(v);
        }
    }

    /**
     * Sometimes a setter has more relaxing parameter definition than the corresponding getter.
     */
    @Test
    void testAsymmetricProperty() {
        AsymmetricProperty r = bind("{items:[1,3,5]}", AsymmetricProperty.class);
        assertEquals(Arrays.asList(1, 3, 5), r.getItems());
    }

    public static class DerivedProperty extends AsymmetricProperty {
        @DataBoundConstructor
        @SuppressWarnings("checkstyle:redundantmodifier")
        public DerivedProperty() {}

        @Override
        public void setItems(Collection<Integer> v) {
            super.setItems(v);
        }
    }

    /**
     * Subtyping and overriding a setter shouldn't hide it.
     */
    @Test
    void testDerivedProperty() {
        DerivedProperty r = bind("{items:[1,3,5]}", DerivedProperty.class);
        assertEquals(Arrays.asList(1, 3, 5), r.getItems());
    }

    public static class Thing {
        @DataBoundConstructor
        @SuppressWarnings("checkstyle:redundantmodifier")
        public Thing() {}
    }

    public static class ThingHolder {
        private final List<Thing> items;

        @DataBoundConstructor
        @SuppressWarnings("checkstyle:redundantmodifier")
        public ThingHolder(List<Thing> items) {
            this.items = items;
        }
    }

    static class ThingInterceptor extends BindInterceptor {
        public int seen;

        @Override
        public Object instantiate(Class actualType, JSONObject json) {
            if (actualType == Thing.class) {
                // all 'things' are tracked, but only 'valid' ones created
                seen++;
                if (!json.getBoolean("valid")) {
                    return null;
                }
            }
            return BindInterceptor.DEFAULT;
        }
    }

    @Test
    void testNullItemsOmittedFromLists() {
        ThingInterceptor thingInterceptor = new ThingInterceptor();
        ThingHolder thingHolder = bind(
                "{items:[{$class:'" + Thing.class.getName() + "',valid:true},{$class:'" + Thing.class.getName()
                        + "',valid:false}]}",
                ThingHolder.class,
                thingInterceptor);
        // ensure we encountered two things, but only kept the non-null things
        assertEquals(2, thingInterceptor.seen);
        assertEquals(1, thingHolder.items.size());
        assertNotNull(thingHolder.items.get(0));
    }
}
