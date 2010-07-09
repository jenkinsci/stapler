package org.kohsuke.stapler.bind;

/**
 * Marker interface for objects that have known URLs.
 *
 * If objects that implement this interface are exported, the well-known URLs
 * are used instead of assigning temporary URLs to objects. In this way, the application
 * can reduce memory consumption. This also enables objects to have "identities"
 * that outlive their GC life (for example, instance of the JPA-bound "User" class can
 * come and go, but they represent the single identity that's pointed by its primary key.)
 *
 * @author Kohsuke Kawaguchi
 */
public interface WithWellKnownURL {
    String getWellKnownUrl();
}
