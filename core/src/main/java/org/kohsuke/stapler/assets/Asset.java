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
        final URL url = f.toURI().toURL();
        return new Asset() {
            @Override
            public URL getURL() {
                return url;
            }

            @Override
            public boolean isStale() {
                return !f.exists();
            }
        };
    }

    public static Asset fromURL(final URL url) {
        return new Asset() {
            @Override
            public URL getURL() {
                return url;
            }
        };
    }
}
