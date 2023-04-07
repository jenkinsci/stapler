package org.kohsuke.stapler.export;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.junit.Ignore;
import org.junit.Test;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;

import static org.junit.Assert.assertEquals;

public class XMLDataWriterTest {
    private ExportConfig config = new ExportConfig().withFlavor(Flavor.XML).withClassAttribute(ClassAttributeBehaviour.IF_NEEDED.simple());

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
            return Set.of(new Job());
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

        @Override
        public String getName() {
            return "foo";
        }

        //        @Exported(visibility = 2)
        @Override
        public Iterator<ParameterValue> iterator() {
            return Set.of(new ParameterValue()).iterator();
        }

        @Exported(visibility = 2)
        public List<ParameterValue> getParameters(){
            return List.of(new ParameterValue());
        }
    }

    @ExportedBean public static class CauseAction implements Action{

        @Override
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
    @Ignore
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
    @Test
    @Ignore
    public void testSimpleUsage() throws Exception {
        assertEquals("<x _class='X'><a>aval</a><c>cval</c></x>",
                serialize(new X(), X.class));
    }

    @ExportedBean(defaultVisibility=2) public static abstract class Super {
        @Exported public String basic = "super";
        @Exported public abstract String generic();
    }
    public static class Sub extends Super {
        @Override
        public String generic() {return "sub";}
        @Exported public String specific() {return "sub";}
    }
    @ExportedBean public static class Container {
        @Exported public Super polymorph = new Sub();
    }
    @Test
    @Ignore
    public void testInheritance() throws Exception {
        assertEquals("<container _class='Container'><polymorph _class='Sub'><basic>super</basic><generic>sub</generic>" +
                "<specific>sub</specific></polymorph></container>",
                serialize(new Container(), Container.class));
    }

    public static class Sub2 extends Super {
        @Exported @Override public String generic() {return "sub2";}
    }
    @Test
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
    @Test
    @Ignore
    public void testAnonymousClass() throws Exception {
        assertValidXML(serialize(new X() {},X.class));
    }

    @ExportedBean
    public static class PA {
        @Exported public int[] v = new int[]{1,2,3};
    }

    @Test
    @Ignore
    public void testPrimitiveArrays() throws Exception {
        assertEquals("<PA _class='PA'><v>1</v><v>2</v><v>3</v></PA>",serialize(new PA(),PA.class));
    }

    @Test
    @Ignore
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

    @Test
    @Ignore
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

    @Test
    @Ignore
    public void testToSingularWithPluralProperties() throws Exception {
        assertEquals("<arraysWithPluralProperties _class='ArraysWithPluralProperties'><bars>foo</bars><category>general</category><category>specific</category><foos>foo</foos><style>ornate</style><style>plain</style></arraysWithPluralProperties>",
                serialize(new ArraysWithPluralProperties(), ArraysWithPluralProperties.class));
    }
}
