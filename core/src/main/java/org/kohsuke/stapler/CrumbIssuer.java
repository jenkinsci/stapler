package org.kohsuke.stapler;

/**
 * Generates a nonce value that allows us to protect against cross-site request forgery (CSRF) attacks.
 *
 * <p>
 * We send this with each JavaScript proxy and verify them when we receive a request.
 *
 * @author Kohsuke Kawaguchi
 * @see WebApp#getCrumbIssuer()
 * @see WebApp#setCrumbIssuer(CrumbIssuer)
 */
public abstract class CrumbIssuer {
    /**
     * Issues a crumb for the given request.
     */
    public /* abstract */ String issueCrumb(StaplerRequest2 request) {
        return ReflectionUtils.ifOverridden(
                () -> issueCrumb(StaplerRequest.fromStaplerRequest2(request)),
                CrumbIssuer.class,
                getClass(),
                "issueCrumb",
                StaplerRequest.class);
    }

    /**
     * @deprecated use {@link #issueCrumb(StaplerRequest2)}
     */
    @Deprecated
    public String issueCrumb(StaplerRequest request) {
        return ReflectionUtils.ifOverridden(
                () -> issueCrumb(StaplerRequest.toStaplerRequest2(request)),
                CrumbIssuer.class,
                getClass(),
                "issueCrumb",
                StaplerRequest2.class);
    }

    public final String issueCrumb() {
        return issueCrumb(Stapler.getCurrentRequest2());
    }

    /**
     * Returns the JavaScript expression that evaluates to the crumb value for the given request,
     * for example, {@code document.head.dataset.crumbValue}.
     *
     * @return the JavaScript expression that evaluates to the crumb value for the given request. Never the crumb itself.
     */
    public abstract String getCrumbExpression();

    /**
     * Sends the crumb value in plain text, enabling retrieval through XmlHttpRequest.
     */
    public HttpResponse doCrumb() {
        return HttpResponses.text(issueCrumb());
    }

    /**
     * Validates a crumb that was submitted along with the request.
     *
     * @param request
     *      The request that submitted the crumb
     * @param submittedCrumb
     *      The submitted crumb value to be validated.
     *
     * @throws SecurityException
     *      If the crumb doesn't match and the request processing should abort.
     */
    public void validateCrumb(StaplerRequest2 request, String submittedCrumb) {
        if (!issueCrumb(request).equals(submittedCrumb)) {
            throw new SecurityException("Request failed to pass the crumb test (try clearing your cookies)");
        }
    }

    /**
     * @deprecated use {@link #validateCrumb(StaplerRequest2, String)}
     */
    @Deprecated
    public void validateCrumb(StaplerRequest request, String submittedCrumb) {
        validateCrumb(StaplerRequest.toStaplerRequest2(request), submittedCrumb);
    }

    /**
     * No-op crumb issuer.
     */
    public static final CrumbIssuer NONE = new CrumbIssuer() {
        @Override
        public String issueCrumb(StaplerRequest2 request) {
            return "";
        }

        @Override
        public String getCrumbExpression() {
            return "''";
        }
    };
}
