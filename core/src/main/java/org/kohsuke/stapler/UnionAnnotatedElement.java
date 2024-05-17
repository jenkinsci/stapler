package org.kohsuke.stapler;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.List;

/**
 * Presents combined view of all the annotations.
 *
 * The item in the source list with smaller index is preferred (think of it as 'override')
 * over the item with larger index.
 */
class UnionAnnotatedElement implements AnnotatedElement {
    private final List<? extends AnnotatedElement> sources;

    UnionAnnotatedElement(List<? extends AnnotatedElement> sources) {
        this.sources = sources;
    }

    @Override
    public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
        for (AnnotatedElement s : sources) {
            if (s.isAnnotationPresent(annotationClass)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        for (AnnotatedElement s : sources) {
            T a = s.getAnnotation(annotationClass);
            if (a != null) {
                return a;
            }
        }
        return null;
    }

    @Override
    public Annotation[] getAnnotations() {
        Annotation[] a = null;
        for (AnnotatedElement s : sources) {
            Annotation[] next = s.getAnnotations();
            if (a == null) {
                a = next;
            } else {
                a = ReflectionUtils.union(a, next);
            }
        }
        return a;
    }

    @Override
    public Annotation[] getDeclaredAnnotations() {
        return sources.get(sources.size() - 1).getAnnotations();
    }
}
