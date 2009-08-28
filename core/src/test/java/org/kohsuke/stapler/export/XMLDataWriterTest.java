package org.kohsuke.stapler.export;

import java.io.IOException;
import java.io.StringWriter;
import java.io.StringReader;

import junit.framework.TestCase;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

public class XMLDataWriterTest extends TestCase {

    public XMLDataWriterTest(String n) {
        super(n);
    }

    private static <T> String serialize(T bean, Class<T> clazz) throws IOException {
        StringWriter w = new StringWriter();
        Model<T> model = new ModelBuilder().get(clazz);
        model.writeTo(bean, Flavor.XML.createDataWriter(bean, w));
        return w.toString();
    }

    @ExportedBean public static class X {
        @Exported public String a = "aval";
        public String b = "bval";
        @Exported public String getC() {return "cval";}
        public String getD() {return "dval";}
    }
    public void testSimpleUsage() throws Exception {
        assertEquals("<x><a>aval</a><c>cval</c></x>",
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
        assertEquals("<container><polymorph><basic>super</basic><generic>sub</generic>" +
                "<specific>sub</specific></polymorph></container>",
                serialize(new Container(), Container.class));
    }

    private void assertValidXML(String s) throws Exception {
        XMLStreamReader r = XMLInputFactory.newInstance().createXMLStreamReader(new StringReader(s));
        while (r.hasNext())
            r.next();
        r.close();
    }

    /**
     * Can we write out anonymous classes as the root object?
     */
    public void testAnonymousClass() throws Exception {
        assertValidXML(serialize(new X() {},X.class));
    }

}
