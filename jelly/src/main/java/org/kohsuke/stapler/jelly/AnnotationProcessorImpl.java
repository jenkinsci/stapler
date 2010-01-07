package org.kohsuke.stapler.jelly;

import org.kohsuke.MetaInfServices;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic.Kind;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Kohsuke Kawaguchi
 */
@SuppressWarnings({"Since15"})
//@MetaInfServices(Processor.class)
public class AnnotationProcessorImpl extends AbstractProcessor {
    private final Map<TypeElement,MissingViews> missingViews = new HashMap<TypeElement, MissingViews>();

    private static class MissingViews extends HashSet<String> {}

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton("*");
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (roundEnv.processingOver())      return false;

        missingViews.clear();
        for (TypeElement t : ElementFilter.typesIn(roundEnv.getRootElements())) {
            check(t);
        }
        missingViews.clear();
        return false;
    }

    private MissingViews check(TypeElement t) {
        MissingViews r = missingViews.get(t);
        if (r==null) {
            r = new MissingViews();
            missingViews.put(t,r);

            r.addAll(check(t.getSuperclass()));
            for (TypeMirror i : t.getInterfaces())
                r.addAll(check(i));

            RequiresView a = t.getAnnotation(RequiresView.class);
            if (a!=null)
                r.addAll(Arrays.asList(a.value()));

            if (!r.isEmpty() && !t.getModifiers().contains(Modifier.ABSTRACT)) {
                processingEnv.getMessager().printMessage(Kind.ERROR, t.getQualifiedName()+" is missing views: "+r,t);
            }
        }
        return r;
    }

    private MissingViews check(TypeMirror t) {
        if (t.getKind()== TypeKind.DECLARED)
            return check((TypeElement)((DeclaredType)t).asElement());
        return EMPTY;
    }

    private static final MissingViews EMPTY = new MissingViews();
}
