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
}
