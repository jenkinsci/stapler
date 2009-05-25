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

    public String getBaseName() {
        return baseName;
    }

    public String format(Locale locale, String key, Object... args) {
        String str = getFormatString(locale, key);
        if(str==null)
            // see http://www.nabble.com/i18n-and-l10n-problems-td16004047.html for more discussion
            // return MessageFormat.format(key,args);
            return key;

        return MessageFormat.format(str,args);
    }

    /**
     * Gets the format string for the given key.
     * <p>
     * This method performs a search so that a look up for "pt_BR" would delegate
     * to "pt" then "" (the no-locale locale.)
     */
    public String getFormatString(Locale locale, String key) {
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
                return msg;

            int idx = suffix.lastIndexOf('_');
            if(idx<0)   // failed to find
                return null;
            suffix = suffix.substring(0,idx);
        }
    }

    /**
     * Works like {@link #getFormatString(Locale, String)} except there's no
     * searching up the delegation chain.
     */
    public String getFormatStringWithoutDefaulting(Locale locale, String key) {
        String msg = get('_'+locale.toString()).getProperty(key);
        if(msg!=null && msg.length()>0)
            return msg;
        return null;
    }

    protected void clearCache() {
        resources.clear();
    }

    protected Properties get(String key) {
        Properties props;

        if(!MetaClass.NO_CACHE) {
            props = resources.get(key);
            if(props!=null)     return props;
        }

        // attempt to load
        props = new Properties();
        String url = baseName + key + ".properties";
        InputStream in=null;
        try {
            in = new URL(url).openStream();
            // an user reported that on IBM JDK, URL.openStream
            // returns null instead of IOException.
            // see http://www.nabble.com/WAS---Hudson-tt16026561.html
        } catch (IOException e) {
            // failed.
        }

        if(in!=null) {
            try {
                try {
                    props.load(in);
                } finally {
                    in.close();
                }
            } catch (IOException e) {
                throw new Error("Failed to load "+url,e);
            }
        }

        resources.put(key,wrapUp(key.length()>0 ? key.substring(1) : "",props));
        return props;
    }

    /**
     * Interception point for property loading.
     */
    protected Properties wrapUp(String locale, Properties props) {
        return props;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ResourceBundle that = (ResourceBundle) o;
        return baseName.equals(that.baseName);
    }

    @Override
    public int hashCode() {
        return baseName.hashCode();
    }
}
