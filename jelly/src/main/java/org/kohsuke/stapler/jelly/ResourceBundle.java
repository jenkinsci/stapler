/*
 * Copyright (c) 2004-2010, Kohsuke Kawaguchi
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided
 * that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice, this list of
 *       conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright notice, this list of
 *       conditions and the following disclaimer in the documentation and/or other materials
 *       provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS
 * OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
 * AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER
 * IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF
 * THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.kohsuke.stapler.jelly;

import org.kohsuke.stapler.MetaClass;
import org.kohsuke.stapler.WebApp;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
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
        String[] suffixes = toStrings(locale);

        while(true) {
            for (int i=0; i<suffixes.length; i++) {
                String suffix = suffixes[i];
                String msg = get(suffix).getProperty(key);
                if(msg!=null && msg.length()>0)
                    // ignore a definition without value, because stapler:i18n generates
                    // value-less definitions
                    return msg;

                int idx = suffix.lastIndexOf('_');
                if(idx<0)   // failed to find
                    return null;
                suffixes[i] = suffix.substring(0,idx);
            }
        }
    }

    /**
     * Works like {@link #getFormatString(Locale, String)} except there's no
     * searching up the delegation chain.
     */
    public String getFormatStringWithoutDefaulting(Locale locale, String key) {
        for (String s : toStrings(locale)) {
            String msg = get(s).getProperty(key);
            if(msg!=null && msg.length()>0)
                return msg;
        }
        return null;
    }

    /**
     * Some language codes have changed over time, such as Hebrew from iw to he.
     * This method returns all such variations in an array.
     *
     * @see Locale#getLanguage()
     */
    private String[] toStrings(Locale l) {
        String v = ISO639_MAP.get(l.getLanguage());
        if (v==null)
            return new String[]{'_'+l.toString()};
        else
            return new String[]{'_'+l.toString(),
                                '_'+v+l.toString().substring(2)};
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

    public static ResourceBundle load(URL jellyUrl) {
        return load(jellyUrl.toExternalForm());
    }

    /**
     * Loads the resource bundle associated with the Jelly script.
     */
    public static ResourceBundle load(String jellyUrl) {
        if(jellyUrl.endsWith(".jelly"))    // cut the trailing .jelly
            jellyUrl = jellyUrl.substring(0,jellyUrl.length()-".jelly".length());

        JellyFacet facet = WebApp.getCurrent().getFacet(JellyFacet.class);
        return facet.resourceBundleFactory.create(jellyUrl);
    }

    /**
     * JDK internally converts new ISO-639 code back to old code. This table provides reverse mapping.
     */
    private static final Map<String,String> ISO639_MAP = new HashMap<String, String>();

    static {
        ISO639_MAP.put("iw","he");
        ISO639_MAP.put("ji","yi");
        ISO639_MAP.put("in","id");
    }
}
