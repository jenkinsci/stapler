package org.kohsuke.stapler.jelly;

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
    Class<? extends AbstractTearOff<?,? extends Script,?>> getClassTearOffType();

    /**
     * Default file extension of this kind of scripts, such as ".jelly"
     */
    String getDefaultScriptExtension();
}
