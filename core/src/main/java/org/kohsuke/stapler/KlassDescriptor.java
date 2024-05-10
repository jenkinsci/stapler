package org.kohsuke.stapler;

import java.util.List;
import org.kohsuke.stapler.lang.FieldRef;
import org.kohsuke.stapler.lang.Klass;

/**
 * Reflection information of a {@link Klass} that drives the request routing.
 *
 * <p>
 * Born as a generalization of {@link ClassDescriptor} to {@link Klass}.
 * @author Kohsuke Kawaguchi
 */
class KlassDescriptor<C> {
    final Klass<C> clazz;
    final FunctionList methods;
    final List<FieldRef> fields;

    /**
     * @param klazz
     *      The class to build a descriptor around.
     */
    public KlassDescriptor(Klass<C> klazz) {
        this.clazz = klazz;
        this.fields = klazz.getFields();
        this.methods = new FunctionList(klazz.getFunctions());
    }
}
