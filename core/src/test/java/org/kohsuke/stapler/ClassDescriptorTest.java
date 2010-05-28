package org.kohsuke.stapler;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import junit.framework.TestCase;

/**
 * @author Alan Harder
 */
public class ClassDescriptorTest extends TestCase {
    public ClassDescriptorTest() {
    }

    public ClassDescriptorTest(int a, int b, String x) {
    }

    public void testLoadConstructorParam() throws Exception {
        assertEquals(0,ClassDescriptor.loadParameterNames(getClass().getConstructor()).length);
        String[] names = ClassDescriptor.loadParameterNames(getClass().getConstructor(int.class, int.class, String.class));
        assertEquals("[a, b, x]",Arrays.asList(names).toString());
    }

    public void testLoadParametersFromAsm() throws Exception {
        // get private method that is being tested
        Method lpfa = ClassDescriptor.class.getDeclaredClasses()[0].getDeclaredMethod(
                "loadParametersFromAsm", Method.class);
        lpfa.setAccessible(true);
        // collect test cases
        Map<String,Method> testCases = new HashMap<String,Method>();
        for (Method m : ClassDescriptorTest.class.getDeclaredMethods())
            if (m.getName().startsWith("methodWith"))
                testCases.put(m.getName().substring(10), m);
        // expected results
        Map<String,String[]> expected = new HashMap<String,String[]>();
        expected.put("NoParams", new String[0]);
        expected.put("NoParams_static", new String[0]);
        expected.put("ManyParams", new String[] { "a", "b", "c", "d", "e", "f", "g", "h", "i" });
        expected.put("Params_static", new String[] { "abc", "def", "ghi" });
        // run tests
        for (Map.Entry<String,String[]> entry : expected.entrySet()) {
            Method testMethod = testCases.get(entry.getKey());
            assertNotNull("Method missing for " + entry.getKey(), testMethod);
            String[] result = (String[])lpfa.invoke(null, testMethod);
            assertNotNull("Null result for " + entry.getKey());
            if (!Arrays.equals(entry.getValue(), result)) {
                StringBuilder buf = new StringBuilder('|');
                for (String s : result) buf.append(s).append('|');
                fail("Unexpected result for " + entry.getKey() + ": " + buf);
            }
        }
    }

    private void methodWithNoParams() { }
    private static void methodWithNoParams_static() { }
    private void methodWithManyParams(String a, boolean b, int c, long d,
            Boolean e, Integer f, Long g, Object h, ClassDescriptorTest i) { }
    private static void methodWithParams_static(String abc, long def, Object ghi) { }
}
