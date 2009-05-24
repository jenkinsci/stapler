package org.kohsuke.stapler.jelly;

/**
 * Receives a notification of the {@link InternationalizedStringExpression} usage.
 *
 * @author Kohsuke Kawaguchi
 */
public interface InternationalizedStringExpressionListener {
    void onUsed(InternationalizedStringExpression exp, Object[] args);
}
