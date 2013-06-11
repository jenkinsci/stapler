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

package org.kohsuke.stapler;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.ListExpression;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.transform.ASTTransformation;
import org.codehaus.groovy.transform.GroovyASTTransformation;
import org.kohsuke.MetaInfServices;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Groovy AST transformation that capture necessary parameter names.
 *
 * @author Kohsuke Kawaguchi
 */
@MetaInfServices
@GroovyASTTransformation
public class CaptureParameterNameTransformation implements ASTTransformation {
    public void visit(ASTNode[] nodes, SourceUnit source) {
        handleClasses(source.getAST().getClasses());
    }

    private void handleClasses(List<ClassNode> classNodes) {
        for (ClassNode c : classNodes)
            handleMethods(c.getMethods());
    }

    // set of annotation class names to capture
    private static final Set<String> CONSTRUCTOR_ANN = Collections.singleton(DataBoundConstructor.class.getName());
    private static final Set<String> INJECTED_PARAMETER_ANN = Collections.singleton(InjectedParameter.class.getName());

    private void handleMethods(List<MethodNode> methods) {
        for (MethodNode m : methods)
            // copy the array as we'll modify them
            if(hasAnnotation(m,CONSTRUCTOR_ANN) || hasInjectionAnnotation(m))
                write(m);
    }

    private boolean hasInjectionAnnotation(MethodNode m) {
        for (Parameter p : m.getParameters())
            if(hasInjectedParameterAnnotation(p))
                return true;
        return false;
    }


    private boolean hasAnnotation(AnnotatedNode target, Set<String> annotationTypeNames) {
        for (AnnotationNode a : target.getAnnotations())
            if(annotationTypeNames.contains(a.getClassNode().getName()))
                return true;
        return false;
    }

    private boolean hasInjectedParameterAnnotation(Parameter p) {
        for (AnnotationNode a : p.getAnnotations()) {
            if (hasAnnotation(a.getClassNode(), INJECTED_PARAMETER_ANN))
                return true;
        }
        return false;
    }


    /**
     * Captures the parameter names as annotations on the class.
     */
    private void write(MethodNode c) {
        ListExpression v = new ListExpression();
        for( Parameter p : c.getParameters() )
            v.addExpression(new ConstantExpression(p.getName()));

        AnnotationNode a = new AnnotationNode(new ClassNode(CapturedParameterNames.class));
        a.addMember("value",v);
        c.addAnnotation(a);
    }
}
