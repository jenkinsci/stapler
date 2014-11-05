package org.kohsuke.stapler;

import net.sf.json.JSONObject;

/**
 * For data-bound class (that has a constructor marked with {@link DataBoundConstructor}, the
 * {@link #bindResolve(StaplerRequest, JSONObject)} allows an instance to replace the object
 * bound from submitted JSON object.
 *
 * <p>
 * This method is automatically invoked by Stapler during databinding method like
 * {@link StaplerRequest#bindJSON(Class, JSONObject)}.
 *
 * <p>
 * This method definition is inspired by Java serialization's {@code readResolve()} method.
 *
 * @author Kohsuke Kawaguchi
 * @see <a href="https://issues.jenkins-ci.org/browse/JENKINS-20262">JENKINS-201262</a>
 */
public interface DataBoundResolvable {
    /**
     * Called after the object is instantiated to allow the object to nominate its replacement.
     *
     * @param request
     *      Request object that's currently performing databinding. Passed in as a contextual
     *      parameter.
     * @param src
     *      JSON object that originally constructed the 'this' instance on which this method
     *      is being invoked.
     * @return
     *      Can be any value, including null. Typically, this method would have to return an
     *      instance of a type compatible to the caller's expectation.
     */
    Object bindResolve(StaplerRequest request, JSONObject src);
}
