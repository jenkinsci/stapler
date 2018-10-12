package org.kohsuke.stapler;

import junit.framework.TestCase;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import javax.annotation.PostConstruct;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;
import java.lang.reflect.Type;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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

    public void testMismatchingTypes() {
        JSONObject json = new JSONObject();
        json.put("b", new String[]{"v1", "v2"});
        try {
            Data data = bind(json, new Data());
            fail("Should have thrown an IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("Error binding field b: Got type array but no lister class found for type class java.lang.String", e.getMessage());
        }
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
        MockRequest mr = new MockRequest() {
            @Override
            public String getContentType() {
                return "text/html";
            }
        };
        mr.getParameterMap().put("a","123");
        mr.getParameterMap().put("b","string");
        RequestImpl req = new RequestImpl(new Stapler(), mr, Collections.<AncestorImpl>emptyList(), null);
        new Function.InstanceFunction(getClass().getMethod("doFromStaplerMethod",StaplerRequest.class,int.class,Binder.class))
                .bindAndInvoke(this,req,null);
        assertEquals(42, new Function.InstanceFunction(getClass().getMethod("doStaticMethod")).bindAndInvoke(this, req, null));
    }

    public void doFromStaplerMethod(StaplerRequest req, @QueryParameter int a, Binder b) {
        assertEquals(123,a);
        assertSame(req,b.req);
        assertEquals("string",b.b);

    }

    public static int doStaticMethod() {return 42;}

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
        assertEquals(Integer.valueOf(5), r.c[0]);
        assertEquals(Integer.valueOf(6), r.d.get(0));
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
        return bind(json,type,BindInterceptor.NOOP);
    }
    private <T> T bind(String json, Class<T> type, BindInterceptor bi) {
        RequestImpl req = createFakeRequest();
        req.setBindInterceptor(bi);
        return req.bindJSON(type, JSONObject.fromObject(json));
    }

    private RequestImpl createFakeRequest() {
        Stapler s = new Stapler();
        s.setWebApp(new WebApp(new MockServletContext()));
        return new RequestImpl(s, new MockRequest(), Collections.<AncestorImpl>emptyList(), null);
    }

    private <T> T bind(JSONObject json, T bean) {
        RequestImpl req = createFakeRequest();
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

        // array coercion on y
        RawBinding r2 = bind("{x:{a:true,b:1},y:{p:true}}", RawBinding.class);
        JSONObject o = (JSONObject)r2.y.get(0);
        assertTrue(o.getBoolean("p"));

        // array coercion on y
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

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

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

    public void testSetterInvocation() {
        SetterBinding r = bind("{x:1,y:2,z:3,w:1, children:[{x:5,y:5,z:5},{x:6,y:6,z:6}], anotherObject:{$class:'org.kohsuke.stapler.DataBindingTest$Point', x:1,y:1} }",SetterBinding.class);
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

        int post=1;

        public void assertValues() {
            assertEquals(1,x);
            assertEquals(2,y);
            assertEquals(3,z);
        }

        @PostConstruct
        private void post1() {
            post += 4;
        }
    }

    public static class Point3Derived extends Point3 {

        @PostConstruct
        private void post1() {
            post *= 2;
        }
    }

    public void testFieldInjection() {
        Point3Derived r = bind("{x:1,y:2,z:3} }",Point3Derived.class);
        r.assertValues();
        assertEquals(10,r.post);
    }

    public static class BeanValidation {

        @DataBoundSetter
        @Positive @DefaultValue("1")
        private int x;

        @DataBoundSetter
        @NotBlank
        private String y;

        @DataBoundSetter
        private String z;

        void assertValues() {
            assertEquals(1,x);
            assertEquals("2",y);
            assertEquals("3",z);
        }
    }

    public void testFieldInjectionWithValidation() {
        BeanValidation r = bind("{x:1,y:'2',z:'3'} }", BeanValidation.class);
        r.assertValues();
    }

    public void testFieldInjectionWithValidationFailure() {
        try {
            bind("{x:0,y:' ',z:'foo'} }", BeanValidation.class);
            fail("validation was expected to fail.");
        } catch (IllegalArgumentException e) {
            System.out.println(e.getCause());
        }
    }

    public void testFieldInjectionWithDefaultValue() {
        BeanValidation r = bind("{y:'2',z:'3'} }",BeanValidation.class);
        r.assertValues();
    }


    public void testInterceptor1() {
        String r = bind("{x:1}", String.class, new BindInterceptor() {
            @Override
            public Object onConvert(Type targetType, Class targetTypeErasure, Object jsonSource) {
                assertEquals(targetType, String.class);
                assertEquals(targetTypeErasure, String.class);
                return String.valueOf(((JSONObject) jsonSource).getInt("x"));
            }
        });
        assertEquals("1",r);
    }

    public void testInterceptor2() {
        RequestImpl req = createFakeRequest();
        req.setBindInterceptor(new BindInterceptor() {
            @Override
            public Object onConvert(Type targetType, Class targetTypeErasure, Object jsonSource) {
                if (targetType==String.class)
                    return String.valueOf(((JSONObject) jsonSource).getInt("x"));
                return DEFAULT;
            }
        });

        String[] r = (String[]) req.bindJSON(String[].class, String[].class, JSONArray.fromObject("[{x:1},{x:2}]"));
        assertTrue(Arrays.equals(r,new String[]{"1","2"}));
    }

    public void testInterceptor3() {
        RequestImpl req = createFakeRequest();
        req.setBindInterceptor(new BindInterceptor() {
            @Override
            public Object instantiate(Class actualType, JSONObject json) {
                if (actualType.equals(Point.class))
                    return new Point(1,2);
                return DEFAULT;
            }
        });

        Object[] r = (Object[]) req.bindJSON(Object[].class, Object[].class, JSONArray.fromObject("[{$class:'"+Point.class.getName()+"'}]"));
        assertTrue(Arrays.equals(r,new Object[]{new Point(1,2)}));
    }

    public static class AsymmetricProperty {
        private final List<Integer> items = new ArrayList<Integer>();

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
    public void testAsymmetricProperty() {
        AsymmetricProperty r = bind("{items:[1,3,5]}",AsymmetricProperty.class);
        assertEquals(Arrays.asList(1,3,5),r.getItems());
    }

    public static class DerivedProperty extends AsymmetricProperty {

        @Override
        public void setItems(Collection<Integer> v) {
            super.setItems(v);
        }
    }


    /**
     * Subtyping and overriding a setter shouldn't hide it.
     */
    public void testDerivedProperty() {
        DerivedProperty r = bind("{items:[1,3,5]}",DerivedProperty.class);
        assertEquals(Arrays.asList(1,3,5),r.getItems());
    }
}
