package org.kohsuke.stapler.jelly.jruby;

import org.jruby.embed.ScriptingContainer;

/**
 * Ruby template language binding.
 *
 * <p>
 * Implementations of this is discovered via service-loader mechanism.
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class RubyTemplateLanguage {
    /**
     * Defines the file extension, like ".erb", that designates this kind of view type.
     */
    protected abstract String getScriptExtension();

    protected abstract Class<? extends AbstractRubyTearOff> getTearOffClass();

    /**
     * Called to set up this template language binding on the specified scripting container.
     */
    protected abstract RubyTemplateContainer createContainer(ScriptingContainer container);
}
