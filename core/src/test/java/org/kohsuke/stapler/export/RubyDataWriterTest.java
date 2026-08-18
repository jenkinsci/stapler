package org.kohsuke.stapler.export;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;

import java.io.IOException;
import java.io.StringWriter;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class RubyDataWriterTest {

    private ExportConfig config = new ExportConfig().withFlavor(Flavor.RUBY);

    private <T> String serialize(T bean, Class<T> clazz) throws IOException {
        StringWriter w = new StringWriter();
        Model<T> model = new ModelBuilder().get(clazz);
        model.writeTo(bean, Flavor.RUBY.createDataWriter(bean, w, config));
        return w.toString();
    }

    static final String CRAFTED_KEY = "escape\":\"pwned\",\"injected";

    @ExportedBean
    public static class MapHolder {
        @Exported
        public final Map<String, String> envVars;

        MapHolder(String key, String value) {
            envVars = new LinkedHashMap<>();
            envVars.put(key, value);
        }
    }

    @Test
    void mapKeyIsEscaped() throws Exception {
        String ruby = serialize(new MapHolder(CRAFTED_KEY, "value"), MapHolder.class);

        assertThat(ruby, containsString("\\\""));
        assertThat(ruby, not(containsString("\"pwned\",\"injected\" => ")));
    }

    @Test
    void craftedKeyRendersAsSingleEscapedEntry() throws Exception {
        String ruby = serialize(new MapHolder(CRAFTED_KEY, "value"), MapHolder.class);

        assertThat(ruby, containsString("\"escape\\\":\\\"pwned\\\",\\\"injected\" => \"value\""));
    }

    @Test
    void ordinaryMapEntryIsUnchanged() throws Exception {
        String ruby = serialize(new MapHolder("foo", "bar"), MapHolder.class);

        assertThat(ruby, containsString("\"foo\" => \"bar\""));
    }
}
