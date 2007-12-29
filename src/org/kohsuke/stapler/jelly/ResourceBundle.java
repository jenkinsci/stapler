package org.kohsuke.stapler.jelly;

import org.kohsuke.stapler.MetaClass;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Cache of localization strings.
 * 
 * @author Kohsuke Kawaguchi
 */
public class ResourceBundle {
    /**
     * URL to load message resources from, except the ".properties" suffix.
     * <p>
     * This is normally something like <tt>file://path/to/somewhere/org/acme/Foo</tt>.
     */
    private final String baseName;

    /**
     * Loaded messages.
     */
    private final Map<String,Properties> resources = new ConcurrentHashMap<String,Properties>();

    public ResourceBundle(String baseName) {
        this.baseName = baseName;
    }

    public String format(Locale locale, String key, Object... args) {
        StringBuilder buf = new StringBuilder();
        buf.append('_').append(locale.getLanguage());
        buf.append('_').append(locale.getCountry());
        buf.append('_').append(locale.getVariant());
        String suffix = buf.toString();

        while(true) {
            String msg = get(suffix).getProperty(key);
            if(msg!=null && msg.length()>0)
                // ignore a definition without value, because stapler:i18n generates
                // value-less definitions
                return MessageFormat.format(msg,args);

            int idx = suffix.lastIndexOf('_');
            if(idx<0)   // failed to find
                return MessageFormat.format(key,args);
            suffix = suffix.substring(0,idx);
        }
    }

    private Properties get(String key) {
        Properties props;
        if(!MetaClass.NO_CACHE) {
            props = resources.get(key);
            if(props!=null)     return props;
        }

        // attempt to load
        props = new Properties();
        String url = baseName + key + ".properties";
        InputStream in;
        try {
            in = new URL(url).openStream();
        } catch (IOException e) {
            // no such resources, so put an empty value
            resources.put(key,props);
            return props;
        }

        try {
            try {
                props.load(in);
            } finally {
                in.close();
            }
        } catch (IOException e) {
            throw new Error("Failed to load "+url,e);
        }

        resources.put(key,props);
        return props;
    }
}
