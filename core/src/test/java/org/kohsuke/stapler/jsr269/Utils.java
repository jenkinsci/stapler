package org.kohsuke.stapler.jsr269;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.TreeMap;
import javax.annotation.processing.SupportedSourceVersion;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import net.java.dev.hickory.testing.Compilation;

class Utils {

    private static JavaFileManager fileManager(Compilation compilation) {
        try {
            Field f = Compilation.class.getDeclaredField("jfm");
            f.setAccessible(true);
            return (JavaFileManager) f.get(compilation);
        } catch (Exception x) {
            throw new AssertionError(x);
        }
    }

    /**
     * Replacement for {@link Compilation#getGeneratedResource} that actually works.
     * https://code.google.com/p/jolira-tools/issues/detail?id=11
     */
    public static String getGeneratedResource(Compilation compilation, String filename) {
        try {
            FileObject fo = fileManager(compilation).getFileForOutput(StandardLocation.CLASS_OUTPUT, "", filename, null);
            if (fo == null) {
                return null;
            }
            return fo.getCharContent(true).toString();
        } catch (FileNotFoundException x) {
            return null;
        } catch (IOException x) {
            throw new RuntimeException(x);
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
        return new TreeMap<Object,Object>(p).toString();
    }

    private Utils() {}

}
