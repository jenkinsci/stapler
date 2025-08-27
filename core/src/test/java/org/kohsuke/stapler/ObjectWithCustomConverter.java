package org.kohsuke.stapler;

import org.springframework.core.convert.converter.Converter;

/**
 * @author Kohsuke Kawaguchi
 */
public class ObjectWithCustomConverter {
    public final int x, y;

    public ObjectWithCustomConverter(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public static class StaplerConverterImpl implements Converter<String, Object> {
        @Override
        public Object convert(String source) {
            String[] tokens = source.split(",");
            return new ObjectWithCustomConverter(Integer.parseInt(tokens[0]), Integer.parseInt(tokens[1]));
        }
    }
}
