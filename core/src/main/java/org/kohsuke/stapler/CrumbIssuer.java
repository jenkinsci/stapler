package org.kohsuke.stapler;

import jakarta.servlet.http.HttpSession;
import java.util.UUID;

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
     * Default crumb issuer.
     */
    public static final CrumbIssuer DEFAULT = new CrumbIssuer() {
        @Override
        public String issueCrumb(StaplerRequest2 request) {
            HttpSession s = request.getSession();
            String v = (String) s.getAttribute(ATTRIBUTE_NAME);
            if (v != null) {
                return v;
            }
            v = UUID.randomUUID().toString();
            s.setAttribute(ATTRIBUTE_NAME, v);
            return v;
        }
    };

    private static final String ATTRIBUTE_NAME = CrumbIssuer.class.getName();
}
