package org.kohsuke.stapler.html;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * A renderer for {@link HtmlView}.
 */
public interface HtmlViewRenderer {

    /**
     * Dynamically injects text context into an element.
     * @param id an HTML {@code id} attribute found in the content
     * @return plain text to insert, or null to skip
     */
    @CheckForNull
    String supplyText(@NonNull String id) throws Exception;
}
