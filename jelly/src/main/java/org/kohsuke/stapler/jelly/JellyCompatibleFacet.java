package org.kohsuke.stapler.jelly;

import java.util.Collection;
import org.apache.commons.jelly.Script;
import org.kohsuke.stapler.AbstractTearOff;
import org.kohsuke.stapler.Facet;

/**
 * {@link Facet} subtype (although not captured in a type hierarchy) that loads Jelly-compatible scripts.
 *
 * @author Kohsuke Kawaguchi
 */
public interface JellyCompatibleFacet {
    /**
     *
     */
    Collection<? extends Class<? extends AbstractTearOff<?, ? extends Script, ?>>> getClassTearOffTypes();

    /**
     * Gets the list of view script extensions, such as ".jelly".
     */
    Collection<String> getScriptExtensions();
}
