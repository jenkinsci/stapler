package org.kohsuke.stapler.export;

import junit.framework.TestCase;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

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
        SAXParser p = SAXParserFactory.newInstance().newSAXParser();
        p.parse(new InputSource(new StringReader(s)),new DefaultHandler());
    }

    /**
     * Can we write out anonymous classes as the root object?
     */
    public void testAnonymousClass() throws Exception {
        assertValidXML(serialize(new X() {},X.class));
    }

    @ExportedBean
    public static class PA {
        @Exported public int[] v = new int[]{1,2,3};
    }

    public void testPrimitiveArrays() throws Exception {
        assertEquals("<PA><v>1</v><v>2</v><v>3</v></PA>",serialize(new PA(),PA.class));
    }

    public void testMakeXmlName() {
        assertEquals("_",   XMLDataWriter.makeXmlName(""));
        assertEquals("abc", XMLDataWriter.makeXmlName("abc"));
        assertEquals("abc", XMLDataWriter.makeXmlName("/abc"));
        assertEquals("abc", XMLDataWriter.makeXmlName("/a/b/c/"));
    }

    @ExportedBean public static class Arrays {
        @Exported public String[] categories = {"general", "specific"};
        @Exported public String[] styles = {"ornate", "plain"};
    }

    public void testToSingular() throws Exception {
        assertEquals("<arrays><category>general</category><category>specific</category><style>ornate</style><style>plain</style></arrays>",
                serialize(new Arrays(), Arrays.class));
    }

}
