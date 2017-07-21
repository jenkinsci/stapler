package org.kohsuke.stapler.lang;

import java.lang.annotation.Annotation;

/**
 * @author Kohsuke Kawaguchi
 */
public abstract class AnnotatedRef {
    private static final Annotation[] EMPTY_ANNOTATIONS = new Annotation[0];

    // no subtyping outside the package
    /*package*/ AnnotatedRef() {}

    public abstract <T extends Annotation> T getAnnotation(Class<T> type);

    public Annotation[] getAnnotations() {
        return EMPTY_ANNOTATIONS;
    }

    public boolean hasAnnotation(Class<? extends Annotation> type) {
        return getAnnotation(type)!=null;
    }
}
