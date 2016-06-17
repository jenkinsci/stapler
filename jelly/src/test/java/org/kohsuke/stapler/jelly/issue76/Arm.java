package org.kohsuke.stapler.jelly.issue76;

import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.HttpResponses;
import org.kohsuke.stapler.StaplerRequest;

/**
 * @author Kohsuke Kawaguchi
 */
public class Arm {
    public HttpResponse doDynamic(StaplerRequest req) {
        return HttpResponses.plainText(req.getRestOfPath());
    }

    @Override
    public String toString() {
        return "arm";
    }
}
