package org.kohsuke.stapler;

import junit.framework.TestCase;
import net.sf.json.JSONObject;

import java.net.Proxy;
import java.util.Collections;
import java.util.EnumSet;

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

    private <T> T bind(JSONObject json, T bean) {
        RequestImpl req = new RequestImpl(new Stapler(), new MockRequest(), Collections.<AncestorImpl>emptyList(), null);
        req.bindJSON(bean,json);
        return bean;
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
        RequestImpl req = new RequestImpl(new Stapler(), new MockRequest(), Collections.<AncestorImpl>emptyList(), null);
        ReferToObjectWithCustomConverter r = req.bindJSON(ReferToObjectWithCustomConverter.class,JSONObject.fromObject("{data:'1,2'}"));
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
        RequestImpl req = new RequestImpl(new Stapler(), new MockRequest(), Collections.<AncestorImpl>emptyList(), null);
        TwoBooleans r = req.bindJSON(TwoBooleans.class,JSONObject.fromObject("{a:false}"));
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
}
