package org.kohsuke.stapler;

import org.apache.commons.beanutils.Converter;

/**
 * @author Kohsuke Kawaguchi
 */
public class ObjectWithCustomConverter {
    public final int x,y;

    public ObjectWithCustomConverter(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public static class StaplerConverterImpl implements Converter {
        public Object convert(Class type, Object value) {
            String[] tokens = value.toString().split(",");
            return new ObjectWithCustomConverter(
                    Integer.parseInt(tokens[0]),
                    Integer.parseInt(tokens[1]));
        }
    }
}
