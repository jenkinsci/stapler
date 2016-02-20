package org.kohsuke.stapler.jsr269;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
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

    public static final List<String> IGNORE = Arrays.asList(
            "source value 1.6 is obsolete and will be removed in a future release", // Filter out warnings about source 1.6 is obsolete in java 9
            "To suppress warnings about obsolete options" // This usually appears with other warnings
    );

    public static List<Diagnostic<? extends JavaFileObject>> filterObsoleteSourceVersionWarnings(List<Diagnostic<? extends JavaFileObject>> diagnostics) {
        List<Diagnostic<? extends JavaFileObject>> r = new ArrayList<Diagnostic<? extends JavaFileObject>>();
        for (Diagnostic<? extends JavaFileObject> d : diagnostics) {
            if (!isIgnored(d.getMessage(Locale.ENGLISH))) {
                r.add(d);
            }
        }
        return r;
    }

    private static boolean isIgnored(String message) {
        for (String i : IGNORE) {
            if (message.contains(i)) return true;
        }
        return false;
    }

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
