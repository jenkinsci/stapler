package org.kohsuke.stapler.jelly;

import org.apache.commons.jelly.expression.Expression;
import org.apache.commons.jelly.impl.TagScript;

/**
 * This class implements a {@link TagScript} that allows rewriting attribute names.
 *
 */
/* package */ class AttributeNameRewritingTagScript extends TagScript {
    private final String original;
    private final String replacement;

    public AttributeNameRewritingTagScript(String original, String replacement) {
        this.original = original;
        this.replacement = replacement;
    }

    @Override
    public void addAttribute(String name, Expression expression) {
        if (replacement.equals(name)) {
            // cf. TagScript#run
            throw new IllegalArgumentException("This tag does not understand the '" + replacement + "' attribute");
        }
        if (original.equals(name)) {
            super.addAttribute(replacement, expression);
        } else {
            super.addAttribute(name, expression);
        }
    }
}
