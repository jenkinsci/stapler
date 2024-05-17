package org.kohsuke.stapler.jelly.groovy;

import groovy.lang.GroovyObject;

/**
 * Typed interface to provide auto-completion for IDEs when invoking taglibs.
 *
 * Subtype must have the {@link TagLibraryUri} annotation
 *
 * @author Kohsuke Kawaguchi
 */
public interface TypedTagLibrary extends GroovyObject {}
