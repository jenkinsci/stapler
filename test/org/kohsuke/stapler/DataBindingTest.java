package org.kohsuke.stapler;

import junit.framework.TestCase;
import net.sf.json.JSONObject;

import java.util.Collections;

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

        RequestImpl req = new RequestImpl(new Stapler(), new MockRequest(), Collections.EMPTY_LIST,null);
        Data data = new Data();
        req.bindJSON(data,json);
        assertEquals(123,data.a);
        assertEquals("string",data.myB);
    }
}
