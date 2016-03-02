package org.kohsuke.stapler.interceptor;

import org.kohsuke.stapler.InjectedParameter;

/**
 * Determines when interception happens.
 *
 * @author Kohsuke Kawaguchi
 * @see InterceptorAnnotation#stage()
 * @since 1.239
 */
public enum Stage {
    /**
     * During the method selection, before all the {@link InjectedParameter}s are processed.
     */
    SELECTION,
    /**
     * Right before the dispatch of the method, after all the {@link InjectedParameter}s are processed.
     */
    PREINVOKE
}
