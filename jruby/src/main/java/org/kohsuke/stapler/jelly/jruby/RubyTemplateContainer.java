package org.kohsuke.stapler.jelly.jruby;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.io.IOUtils;
import org.apache.commons.jelly.Script;
import org.jruby.RubyClass;
import org.jruby.embed.ScriptingContainer;

import java.io.IOException;
import java.net.URL;

/**
 * Per-{@link ScriptingContainer} portion of {@link RubyTemplateLanguage}.
 *
 * <p>
 * One often needs to do some preparation work in every {@link ScriptingContainer} that it uses,
 * such as loading gem. Instance of this captures that context.
 *
 * <p>
 * Right now, we only use one {@link ScriptingContainer}, so this isn't serving any useful purpose,
 * but this is in anticipation of the future expansion to handle multiple {@link ScriptingContainer}s.
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
    public final ScriptingContainer container;

    public RubyTemplateContainer(RubyClass scriptClass, RubyTemplateLanguage language, ScriptingContainer container) {
        this.scriptClass = scriptClass;
        this.language = language;
        this.container = container;
    }

    @SuppressFBWarnings(value = "URLCONNECTION_SSRF_FD", justification = "Not relevant in this situation.")
    public Script parseScript(URL path) throws IOException {
        try {
            String template = IOUtils.toString(path.openStream(), "UTF-8");
            return (Script) container.callMethod(scriptClass, "new", template);
        } catch (Exception e) {
            throw (IOException) new IOException("Failed to parse "+path).initCause(e);
        }
    }
}
