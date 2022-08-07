package org.kohsuke.stapler;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletException;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 * @author Alan Harder
 */
public class ClassDescriptorTest {

    @Test public void loadConstructorParam() throws Exception {
        assertEquals(0,ClassDescriptor.loadParameterNames(C.class.getConstructor()).length);
        String[] names = ClassDescriptor.loadParameterNames(C.class.getConstructor(int.class, int.class, String.class));
        assertEquals("[a, b, x]",Arrays.asList(names).toString());
    }

    @Test public void loadParameterNamesFromReflection() {
        // collect test cases
        Map<String,Method> testCases = new HashMap<>();
        for (Method m : ClassDescriptorTest.class.getDeclaredMethods())
            if (m.getName().startsWith("methodWith"))
                testCases.put(m.getName().substring(10), m);
        // expected results
        Map<String,String[]> expected = new HashMap<>();
        expected.put("NoParams", new String[0]);
        expected.put("NoParams_static", new String[0]);
        expected.put("ManyParams", new String[] { "a", "b", "c", "d", "e", "f", "g", "h", "i" });
        expected.put("Params_static", new String[] { "abc", "def", "ghi" });
        // run tests
        for (Map.Entry<String,String[]> entry : expected.entrySet()) {
            Method testMethod = testCases.get(entry.getKey());
            assertNotNull("Method missing for " + entry.getKey(), testMethod);
            String[] result = ClassDescriptor.loadParameterNamesFromReflection(testMethod);
            assertNotNull("Null result for " + entry.getKey(), result);
            if (!Arrays.equals(entry.getValue(), result)) {
                StringBuilder buf = new StringBuilder("|");
                for (String s : result) buf.append(s).append('|');
                fail("Unexpected result for " + entry.getKey() + ": " + buf);
            }
        }
    }

    @Test public void loadParametersFromAsm() throws Exception {
        // collect test cases
        Map<String,Method> testCases = new HashMap<>();
        for (Method m : ClassDescriptorTest.class.getDeclaredMethods())
            if (m.getName().startsWith("methodWith"))
                testCases.put(m.getName().substring(10), m);
        // expected results
        Map<String,String[]> expected = new HashMap<>();
        expected.put("NoParams", new String[0]);
        expected.put("NoParams_static", new String[0]);
        expected.put("ManyParams", new String[] { "a", "b", "c", "d", "e", "f", "g", "h", "i" });
        expected.put("Params_static", new String[] { "abc", "def", "ghi" });
        // run tests
        for (Map.Entry<String,String[]> entry : expected.entrySet()) {
            Method testMethod = testCases.get(entry.getKey());
            assertNotNull("Method missing for " + entry.getKey(), testMethod);
            String[] result = BytecodeReadingParanamer.lookupParameterNames(testMethod);
            assertNotNull("Null result for " + entry.getKey(), result);
            if (!Arrays.equals(entry.getValue(), result)) {
                StringBuilder buf = new StringBuilder("|");
                for (String s : result) buf.append(s).append('|');
                fail("Unexpected result for " + entry.getKey() + ": " + buf);
            }
        }
    }

    @Test public void inheritedWebMethods() {
        // http://bugs.sun.com/view_bug.do?bug_id=6342411
        assertEquals(1, new ClassDescriptor(Sub.class).methods.name("doDynamic").signature(StaplerRequest.class, StaplerResponse.class).size());
    }

    public static class C {
        public C() {}
        public C(int a, int b, String x) {}
    }

    private void methodWithNoParams() { }
    private static void methodWithNoParams_static() { }
    private void methodWithManyParams(String a, boolean b, int c, long d,
            Boolean e, Integer f, Long g, Object h, ClassDescriptorTest i) { }
    private static void methodWithParams_static(String abc, long def, Object ghi) { }

    protected static abstract class Super {
        public void doDynamic(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {}
    }
    public static class Sub extends Super {}


    /**
     * D.x() overrides B.x()
     */
    @Test
    public void overridingMethod() throws Exception {
        FunctionList methods = new ClassDescriptor(D.class).methods.name("x");
        assertEquals(1, methods.size());

        // we should be able to see both annotations
        Function f = methods.get(0);
        assertEquals(3, f.getAnnotation(AnnA.class).value());
        assertNotNull(f.getAnnotation(AnnB.class));
        // similarly parameter annotations should be fused together
        assertSame(AnnC.class, f.getParameterAnnotations()[0][0].annotationType());

        // method should be dispatched to D.x() which overrides B.x()
        assertEquals(2, f.bindAndInvoke(new D(), null, null, "Hello"));
    }

    public static class B<T> {
        @AnnA @AnnB
        public int x(@AnnC T t) { return 1; }
    }

    public static class D extends B<String> {
        @Override
        @AnnA(3)
        public int x(String t) { return 2; }
    }

    @Retention(RetentionPolicy.RUNTIME)
    @interface AnnA {
        int value() default 0;
    }

    @Retention(RetentionPolicy.RUNTIME)
    @interface AnnB {
        int value() default 0;
    }

    @Retention(RetentionPolicy.RUNTIME)
    @interface AnnC {}

}
