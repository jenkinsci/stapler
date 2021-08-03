package org.kohsuke.stapler.export;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import junit.framework.TestCase;
import org.junit.Test;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;

public class XMLDataWriterTest extends TestCase {
    private ExportConfig config = new ExportConfig().withFlavor(Flavor.XML).withClassAttribute(ClassAttributeBehaviour.IF_NEEDED.simple());

    public XMLDataWriterTest(String n) {
        super(n);
    }

    private <T> String serialize(T bean, Class<T> clazz) throws IOException {
        StringWriter w = new StringWriter();
        Model<T> model = new ModelBuilder().get(clazz);
        model.writeTo(bean, Flavor.XML.createDataWriter(bean, w, config));
        return w.toString();
    }

    //Nested test
    @ExportedBean(defaultVisibility=2) public static abstract class Build {

        public String getName(){
            return "build1";
        }

        @Exported
        public Collection<Job> getJobs(){
            return Collections.singleton(new Job());
        }
    }

    @ExportedBean
    public static class Job {
        @Exported
        public String getName() {return "job1";}

        @Exported(visibility = 2)
        public Collection<Action> getActions() {
            return java.util.Arrays.asList(new ParameterAction(), new CauseAction());
        }

    }

    public interface Action {
        String getName();
    }

    @ExportedBean public static class ParameterAction implements Action, Iterable<ParameterValue>{

        public String getName() {
            return "foo";
        }

        //        @Exported(visibility = 2)
        public Iterator<ParameterValue> iterator() {
            return Collections.singleton(new ParameterValue()).iterator();
        }

        @Exported(visibility = 2)
        public List<ParameterValue> getParameters(){
            return Collections.singletonList(new ParameterValue());
        }
    }

    @ExportedBean public static class CauseAction implements Action{

        public String getName() {
            return "cause1";
        }

        @Exported(visibility = 2)
        public String getCause() { return "xyz";}
    }

    @ExportedBean(defaultVisibility = 3) public static class ParameterValue{

        @Exported
        public String getNames() {
            return "foo";
        }

        @Exported
        public String getValues() {
            return "bar";
        }
    }


    @Test
    public void testNestedBeans() throws Exception {
        System.out.println(serialize(new Job(), Job.class));
        assertEquals("<job _class='Job'><action _class='ParameterAction'><parameter><names>foo</names><values>bar</values></parameter></action><action _class='CauseAction'><cause>xyz</cause></action><name>job1</name></job>",
                serialize(new Job(), Job.class));
    }

    @ExportedBean public static class X {
        @Exported public String a = "aval";
        public String b = "bval";
        @Exported public String getC() {return "cval";}
        public String getD() {return "dval";}
    }
    public void testSimpleUsage() throws Exception {
        assertEquals("<x _class='X'><a>aval</a><c>cval</c></x>",
                serialize(new X(), X.class));
    }

    @ExportedBean
    public static class Escape {
        @Exported
        public String foo =
                "\u0001\u0008\t\n\u000B\u000C\r\u000E\u001F &<>A-Za-z0-9~\u007F\u0084\u0085\u0086\u009F\u00A0";
    }

    public void testEscape() throws Exception {
        String s = serialize(new Escape(), Escape.class);
        assertValidXML("<?xml version=\"1.1\"?>" + s);
        assertEquals(
                "<escape _class='Escape'><foo>&#1;&#8;\t\n&#11;&#12;\r&#14;&#31; &amp;&lt;&gt;A-Za-z0-9~&#127;&#132;\u0085&#134;&#159;\u00A0</foo></escape>",
                s);
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
        assertEquals("<container _class='Container'><polymorph _class='Sub'><basic>super</basic><generic>sub</generic>" +
                "<specific>sub</specific></polymorph></container>",
                serialize(new Container(), Container.class));
    }

    public static class Sub2 extends Super {
        @Exported @Override public String generic() {return "sub2";}
    }
    public void testInheritance2() throws Exception { // JENKINS-13336
        assertEquals("<sub2 _class='Sub2'><basic>super</basic><generic>sub2</generic></sub2>",
                serialize(new Sub2(), Sub2.class));
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
        assertEquals("<PA _class='PA'><v>1</v><v>2</v><v>3</v></PA>",serialize(new PA(),PA.class));
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
        assertEquals("<arrays _class='Arrays'><category>general</category><category>specific</category><style>ornate</style><style>plain</style></arrays>",
                serialize(new Arrays(), Arrays.class));
    }


    @ExportedBean public static class ArraysWithPluralProperties {
        @Exported public String[] categories = {"general", "specific"};
        @Exported public String[] styles = {"ornate", "plain"};
        @Exported public String foos = "foo";
        @Exported public String bars = "foo";
    }

    public void testToSingularWithPluralProperties() throws Exception {
        assertEquals("<arraysWithPluralProperties _class='ArraysWithPluralProperties'><bars>foo</bars><category>general</category><category>specific</category><foos>foo</foos><style>ornate</style><style>plain</style></arraysWithPluralProperties>",
                serialize(new ArraysWithPluralProperties(), ArraysWithPluralProperties.class));
    }
}
