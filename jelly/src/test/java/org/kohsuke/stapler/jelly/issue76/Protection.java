package org.kohsuke.stapler.jelly.issue76;

import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.HttpResponses;
import org.kohsuke.stapler.lang.KInstance;
import org.kohsuke.stapler.lang.Klass;

/**
 * Wraps various parts of robots and tweaks its routing behaviour.
 *
 * @author Kohsuke Kawaguchi
 */
public class Protection implements KInstance<ProtectedClass> {
    /**
     * Object being wrapped.
     */
    public final Object o;

    public Protection(Object o) {
        this.o = o;
    }

    /**
     * Fool the reflection so that Stapler sees routes exposed by {@code o.getClass()}.
     */
    @Override
    public Klass<ProtectedClass> getKlass() {
        return new Klass<>(new ProtectedClass(o.getClass()),ProtectedClass.NAVIGATOR);
    }

    /**
     * Override doIndex.
     */
    public HttpResponse doIndex() {
        return HttpResponses.plainText("protected "+o);
    }
}
