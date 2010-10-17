/*
 * Copyright (c) 2004-2010, Kohsuke Kawaguchi
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided
 * that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice, this list of
 *       conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright notice, this list of
 *       conditions and the following disclaimer in the documentation and/or other materials
 *       provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS
 * OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
 * AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER
 * IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF
 * THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

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
