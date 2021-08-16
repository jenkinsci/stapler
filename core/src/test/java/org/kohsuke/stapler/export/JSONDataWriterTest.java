package org.kohsuke.stapler.export;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import net.sf.json.JSONObject;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.kohsuke.stapler.export.Flavor.JSON;

public class JSONDataWriterTest {
    private ExportConfig config = new ExportConfig().withFlavor(Flavor.JSON).withClassAttribute(ClassAttributeBehaviour.IF_NEEDED.simple());

    private <T> String serialize(T bean, Class<T> clazz) throws IOException {
        StringWriter w = new StringWriter();
        Model<T> model = new ModelBuilder().get(clazz);
        model.writeTo(bean, Flavor.JSON.createDataWriter(bean, w, config));
        return w.toString();
    }

    @ExportedBean public static class X {
        @Exported public String a = "aval";
        public String b = "bval";
        @Exported public String getC() {return "cval";}
        public String getD() {return "dval";}
    }

    @Test
    public void testSimpleUsage() throws Exception {
        assertEquals("{\"_class\":\"X\",\"a\":\"aval\",\"c\":\"cval\"}",
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

    @Test
    public void testInheritance() throws Exception {
        assertEquals("{\"_class\":\"Container\",\"polymorph\":{\"_class\":\"Sub\",\"basic\":\"super\",\"generic\":\"sub\",\"specific\":\"sub\"}}",
                serialize(new Container(), Container.class));
    }

    public static class Sub2 extends Super {
        @Exported @Override public String generic() {return "sub2";}
    }

    @Test
    public void testEncodedChars() throws Exception {
        assertEquals("{\"_class\":\"Encoded\",\"bar\":\"\\ud834\\udd1e\",\"foo\":\"\\u0000\"}",
                serialize(new Encoded(), Encoded.class));
    }

    @Test
    public void testInheritance2() throws Exception { // JENKINS-13336
        assertEquals("{\"_class\":\"Sub2\",\"basic\":\"super\",\"generic\":\"sub2\"}",
                serialize(new Sub2(), Sub2.class));
    }

    @Test
    public void exceptionHandling() throws IOException {
        assertEquals("{\"_class\":\"Supers\",\"elements\":[{\"_class\":\"Sub\",\"basic\":\"super\",\"generic\":\"sub\",\"specific\":\"sub\"},{\"_class\":\"Sub2\",\"basic\":\"super\",\"generic\":\"sub2\"}]}",
                     serialize(new Supers(new Sub(), new Broken(), new Sub2()), Supers.class));
    }
    public static class Broken extends Super {
        @Exported @Override public String generic() {throw new RuntimeException("oops");}
    }
    @ExportedBean public static class Supers {
        @Exported public final List<? extends Super> elements;
        public Supers(Super... elements) {
            this.elements = Arrays.asList(elements);
        }
    }

    @Test
    public void JSONObjectWithoutNullValue() throws IOException {
        String json = "{'key':'value', 'optional':'present'}";

        StringWriter w = new StringWriter();
        ModelWithJsonField jsonModel = new ModelWithJsonField(json);

        DataWriter writer = JSON.createDataWriter(jsonModel,w);
        Model<ModelWithJsonField> model = new ModelBuilder().get(ModelWithJsonField.class);
        model.writeTo(jsonModel, writer);

        assertThat("not a correct representation of " + json, w.toString(), containsString("present"));
    }

    @Test
    @Ignore
    public void JSONObjectWithNullValue() throws IOException {
        String json = "{'key':'value', 'optional':null}";

        StringWriter w = new StringWriter();
        ModelWithJsonField jsonModel = new ModelWithJsonField(json);

        DataWriter writer = JSON.createDataWriter(jsonModel,w);
        Model<ModelWithJsonField> model = new ModelBuilder().get(ModelWithJsonField.class);
        model.writeTo(jsonModel, writer);

        assertThat("not a correct representation of " + json, w.toString(), containsString("null"));
    }

    @ExportedBean
    public static class ModelWithJsonField {
        private final JSONObject json;

        public ModelWithJsonField(String jsonAsString) {
            json = JSONObject.fromObject(jsonAsString.replace('\'', '"'));
        }

        @Exported(visibility = 2)
        public JSONObject getJson() {
            return json;
        }
    }

    @ExportedBean
    public static class Encoded {
        @Exported
        public String getFoo() {
            return "\u0000";
        }
        @Exported
        public String getBar() {
            return "\uD834\uDD1E";
        }
    }

}
