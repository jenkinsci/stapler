package org.kohsuke.stapler.assets;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * URL with an easy up-to-date check.
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class Asset {
    private Asset() {
    }

    public abstract URL getURL();
    public boolean isStale() {
        return false;
    }

    public static Asset fromFile(final File f) throws MalformedURLException {
        if (f==null)    return null;
        return new Asset() {
            final URL url = f.toURI().toURL();
            final long timestamp = f.lastModified();

            @Override
            public URL getURL() {
                return url;
            }

            @Override
            public boolean isStale() {
                return timestamp!=f.lastModified();
            }
        };
    }

    public static Asset fromURL(final URL url) {
        if (url==null)    return null;
        return new Asset() {
            @Override
            public URL getURL() {
                return url;
            }
        };
    }
}
