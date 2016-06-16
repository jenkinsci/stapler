package org.kohsuke.stapler.jelly.issue76;

import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.HttpResponses;

/**
 * @author Kohsuke Kawaguchi
 */
public class Head {
    public Eye getEye(int i) {
        return new Eye(i);
    }

    public HttpResponse doNose() {
        return HttpResponses.plainText("nose");
    }
}
