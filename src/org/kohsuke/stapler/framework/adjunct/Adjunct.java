package org.kohsuke.stapler.framework.adjunct;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * In-memory cache of fully inlined "adjunct" which is a pair of CSS and JavaScript.
 *
 * @author Kohsuke Kawaguchi
 */
public class Adjunct {
    /**
     * Fully qualified name of this adjunct.
     */
    public final String name;

    /**
     * List of fully qualified adjunct names that are required before this adjunct.
     */
    public final List<String> required = new ArrayList<String>();

    public final boolean hasCss;
    public final boolean hasJavaScript;

    /**
     * Builds an adjunct.
     *
     * @param name
     *      Fully qualified name of the adjunct.
     * @param classLoader
     *      This is where adjucts are loaded.
     */
    public Adjunct(String name, final ClassLoader classLoader) throws IOException {
        this.name = name;
        this.hasCss = parseOne(classLoader, name + ".css");
        this.hasJavaScript = parseOne(classLoader,name +".js");

        if(!hasCss && !hasJavaScript)
            throw new IOException("Neither "+ name +".css nor "+ name +".js were found");
    }

    private boolean parseOne(ClassLoader classLoader, String resName) throws IOException {
        InputStream is = classLoader.getResourceAsStream(resName);
        if (is == null)     return false;

        BufferedReader in = new BufferedReader(new InputStreamReader(is,UTF8));
        String line;
        while((line=in.readLine())!=null) {
            Matcher m = INCLUDE.matcher(line);
            if(m.lookingAt())
                required.add(m.group(1));
        }
        in.close();
        return true;
    }

    public boolean has(Kind k) {
        switch (k) {
        case CSS:   return hasCss;
        case JS:    return hasJavaScript;
        }
        throw new AssertionError(k);
    }

    public enum Kind { CSS, JS }

    private static final Pattern INCLUDE = Pattern.compile("/[/*]\\s*@include (\\S+)");
    private static final Charset UTF8 = Charset.forName("UTF-8");
}
