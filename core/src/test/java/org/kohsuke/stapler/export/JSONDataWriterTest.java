package org.kohsuke.stapler.export;

import junit.framework.TestCase;

import java.io.IOException;
import java.io.StringWriter;

public class JSONDataWriterTest extends TestCase {

    public JSONDataWriterTest(String n) {
        super(n);
    }

    private static <T> String serialize(T bean, Class<T> clazz) throws IOException {
        StringWriter w = new StringWriter();
        Model<T> model = new ModelBuilder().get(clazz);
        model.writeTo(bean, Flavor.JSON.createDataWriter(bean, w));
        return w.toString();
    }

    @ExportedBean public static class X {
        @Exported public String a = "aval";
        public String b = "bval";
        @Exported public String getC() {return "cval";}
        public String getD() {return "dval";}
    }

    public void testSimpleUsage() throws Exception {
        assertEquals("{\"class\":\"X\",\"a\":\"aval\",\"c\":\"cval\"}",
                serialize(new X(), X.class));
    }

    @ExportedBean(defaultVisibility=2) public static abstract class Super {
        @Exported public String basic = "super";
        @Exported public abstract String generic();
    }
    public static class Sub extends Super {
        public String generic() {return "sub";}
        @Exported public String specific() {return "sub";}
    }
    @ExportedBean public static class Container {
        @Exported public Super polymorph = new Sub();
    }

    public void testInheritance() throws Exception {
        assertEquals("{\"class\":\"Container\",\"polymorph\":{\"class\":\"Sub\",\"basic\":\"super\",\"generic\":\"sub\",\"specific\":\"sub\"}}",
                serialize(new Container(), Container.class));
    }

    public static class Sub2 extends Super {
        @Exported @Override public String generic() {return "sub2";}
    }
    public void testInheritance2() throws Exception { // JENKINS-13336
        assertEquals("{\"class\":\"Sub2\",\"basic\":\"super\",\"generic\":\"sub2\"}",
                serialize(new Sub2(), Sub2.class));
    }
}
