package org.kohsuke.stapler.jsr269;

import java.io.IOException;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.TreeMap;

class Utils {
    static final String CLASS_OUTPUT = "target/elementary-classes";

    public static String getGeneratedResource(String filename) {
        Path resource = Path.of(CLASS_OUTPUT, filename);
        if (!Files.exists(resource)) {
            return null;
        }
        try {
            return Files.readString(resource);
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
