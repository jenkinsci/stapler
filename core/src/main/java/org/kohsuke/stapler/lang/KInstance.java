package org.kohsuke.stapler.lang;

import org.kohsuke.stapler.Facet;

/**
 * Objects can implement this interface to designate its own {@link Klass}.
 *
 * This allows specific classes or instances to take over the routing rules without
 * going through the trouble of creating a new {@link Facet}
 *
 * @author Kohsuke Kawaguchi
 */
public interface KInstance<C> {
    /**
     * @return
     *      null if there's no designated {@link Klass} for this instance, in which
     *      case Stapler treats this instance normally as if it didn't implement
     *      {@link KInstance} to begin with (for example, by calling {@link Object#getClass()}
     */
    Klass<C> getKlass();
}
