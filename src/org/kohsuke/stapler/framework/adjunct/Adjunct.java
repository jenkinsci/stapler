package org.kohsuke.stapler.framework.adjunct;

import org.apache.commons.jelly.XMLOutput;
import org.apache.commons.io.IOUtils;
import org.xml.sax.SAXException;

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

    public final AdjunctManager manager;

    /**
     * Fully qualified name of this adjunct that follows the dot notation.
     */
    public final String name;

    /**
     * The same as {@link #name} but uses '/' separator.
     */
    public final String slashedName;

    /**
     * List of fully qualified adjunct names that are required before this adjunct.
     */
    public final List<String> required = new ArrayList<String>();

    private final boolean hasCss;
    private final boolean hasJavaScript;

    /**
     * If the HTML that includes CSS/JavaScripts are provided
     * by this adjunct, non-null. This allows external JavaScript/CSS
     * resources to be handled in the adjunct mechanism.
     */
    private final String inclusionFragment;

    /**
     * Builds an adjunct.
     *
     * @param name
     *      Fully qualified name of the adjunct.
     * @param classLoader
     *      This is where adjucts are loaded.
     */
    public Adjunct(AdjunctManager manager, String name, final ClassLoader classLoader) throws IOException {
        this.manager = manager;
        this.name = name;
        this.slashedName = name.replace('.','/');
        this.hasCss = parseOne(classLoader, slashedName+".css");
        this.hasJavaScript = parseOne(classLoader,slashedName+".js");
        if(parseOne(classLoader,slashedName+".html"))
            inclusionFragment = IOUtils.toString(new InputStreamReader(classLoader.getResourceAsStream(slashedName+".html"),"UTF-8"));
        else
            inclusionFragment = null;

        if(!hasCss && !hasJavaScript)
            throw new NoSuchAdjunctException("Neither "+ name +".css nor "+ name +".js were found");
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

    public void write(XMLOutput out) throws SAXException {
        if(inclusionFragment!=null) {
            out.write(inclusionFragment);
            return;
        }
        if(hasCss)
            out.write("<link rel='stylesheet' href='"+manager.rootURL+'/'+slashedName+".css' type='text/css' />");
        if(hasJavaScript)
            out.write("<script src='"+manager.rootURL+'/'+slashedName+".js' type='text/javascript'></script>");
    }

    public enum Kind { CSS, JS }

    private static final Pattern INCLUDE = Pattern.compile("/[/*]\\s*@include (\\S+)");
    private static final Charset UTF8 = Charset.forName("UTF-8");
}
