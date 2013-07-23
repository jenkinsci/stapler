/*
 * Copyright (c) 2013, Jesse Glick
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package org.kohsuke.stapler.jsr269;

import java.util.Set;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import org.kohsuke.MetaInfServices;
import org.kohsuke.stapler.interceptor.RequirePOST;

@SupportedSourceVersion(SourceVersion.RELEASE_6)
@SupportedAnnotationTypes("org.kohsuke.stapler.interceptor.RequirePOST")
@MetaInfServices(Processor.class)
public class RequirePOSTAnnotationProcessor extends AbstractProcessorImpl {

    @Override public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element e : roundEnv.getElementsAnnotatedWith(RequirePOST.class)) {
            if (e.getKind() != ElementKind.METHOD) {
                continue; // defensive against broken code
            }
            ExecutableElement method = (ExecutableElement) e;
            if (method.getModifiers().contains(Modifier.ABSTRACT)) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "@RequirePOST is meaningless on an abstract method", e);
            }
        }
        for (Element e : roundEnv.getRootElements()) {
            if (e.getKind() != ElementKind.CLASS) {
                continue;
            }
            for (Element e2 : ((TypeElement) e).getEnclosedElements()) {
                if (e2.getKind() != ElementKind.METHOD) {
                    continue;
                }
                ExecutableElement method = (ExecutableElement) e2;
                if (method.getAnnotation(RequirePOST.class) != null) {
                    continue;
                }
                if (method.getModifiers().contains(Modifier.ABSTRACT)) {
                    continue;
                }
                checkForOverrides((ExecutableElement) e2);
            }
        }
        return true;
    }

    private void checkForOverrides(ExecutableElement concrete) {
        checkForOverrides(concrete, ((TypeElement) concrete.getEnclosingElement()).getSuperclass());
    }

    private void checkForOverrides(ExecutableElement concrete, TypeMirror superclass) {
        TypeElement superclassE = (TypeElement) processingEnv.getTypeUtils().asElement(superclass);
        if (superclassE == null) {
            return;
        }
        for (Element e : superclassE.getEnclosedElements()) {
            if (e.getKind() != ElementKind.METHOD) {
                continue;
            }
            ExecutableElement method = (ExecutableElement) e;
            if (processingEnv.getElementUtils().overrides(concrete, method, superclassE)) {
                if (method.getAnnotation(RequirePOST.class) != null) {
                    processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, "@RequirePOST ignored unless also specified on an overriding method", concrete);
                }
                return;
            }
        }
        checkForOverrides(concrete, superclassE.getSuperclass());
    }

}
