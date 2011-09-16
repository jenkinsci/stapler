package org.kohsuke.stapler.jelly.jruby;

import org.apache.commons.io.IOUtils;
import org.apache.commons.jelly.Script;
import org.jruby.Ruby;
import org.jruby.RubyClass;
import org.jruby.embed.ScriptingContainer;
import org.jruby.javasupport.util.RuntimeHelpers;

import java.io.IOException;
import java.net.URL;

/**
 * Per-{@link ScriptingContainer} portion of {@link RubyTemplateLanguage}.
 *
 * <p>
 * One often needs to do some preparation work in every {@link ScriptingContainer} that it uses,
 * such as loading gem. Instance of this captures that context.
 *
 * @author Kohsuke Kawaguchi
 */
public class RubyTemplateContainer {
    /**
     * Subtype of {@link JRubyJellyScript} for this specific template language.
     */
    private final RubyClass scriptClass;

    /**
     * Where we came from.
     */
    public final RubyTemplateLanguage language;

    /**
     * This {@link RubyTemplateContainer} instance if scoped to this JRuby interpreter context.
     */
    public final Ruby runtime;

    public RubyTemplateContainer(RubyClass scriptClass, RubyTemplateLanguage language, Ruby runtime) {
        this.scriptClass = scriptClass;
        this.language = language;
        this.runtime = runtime;
    }

    public Script parseScript(URL path) throws IOException {
        try {
            String template = IOUtils.toString(path.openStream(), "UTF-8");
            return (Script) RuntimeHelpers.invoke(runtime.getCurrentContext(),scriptClass,"new",runtime.newString(template));
        } catch (Exception e) {
            throw (IOException) new IOException("Failed to parse "+path).initCause(e);
        }
    }
}
