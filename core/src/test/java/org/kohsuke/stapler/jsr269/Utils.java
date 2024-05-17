package org.kohsuke.stapler.jsr269;

import java.io.IOException;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Properties;
import java.util.TreeMap;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;

class Utils {
    public static String getGeneratedResource(List<JavaFileObject> generated, String filename) {
        JavaFileObject fo = generated.stream()
                .filter(it -> it.getName().equals("/" + StandardLocation.CLASS_OUTPUT + "/" + filename))
                .findFirst()
                .orElse(null);
        if (fo == null) {
            return null;
        }
        try {
            return fo.getCharContent(true).toString();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Converts the text content of a properties file to a sorted map.
     * Otherwise you get junk like the header comment with a timestamp, the list is randomly sorted, etc.
     * @param props text content in *.properties format
     * @return string representation of a map (sorted ascending by key)
     */
    public static String normalizeProperties(String props) {
        if (props == null) {
            return null;
        }
        Properties p = new Properties();
        try {
            p.load(new StringReader(props));
        } catch (IOException x) {
            throw new AssertionError(x);
        }
        return new TreeMap<>(p).toString();
    }

    private Utils() {}
}
