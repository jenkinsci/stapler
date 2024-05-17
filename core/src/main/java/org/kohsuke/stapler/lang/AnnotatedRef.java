package org.kohsuke.stapler.lang;

import java.lang.annotation.Annotation;

/**
 * @author Kohsuke Kawaguchi
 */
public abstract class AnnotatedRef {
    // no subtyping outside the package
    /*package*/ AnnotatedRef() {}

    public abstract <T extends Annotation> T getAnnotation(Class<T> type);

    public boolean hasAnnotation(Class<? extends Annotation> type) {
        return getAnnotation(type) != null;
    }
}
