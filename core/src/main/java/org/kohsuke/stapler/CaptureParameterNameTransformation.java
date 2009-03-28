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
import java.util.HashSet;
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
        handleClasses((List<ClassNode>) source.getAST().getClasses());
    }

    private void handleClasses(List<ClassNode> classNodes) {
        for (ClassNode c : classNodes)
            handleMethods(c.getMethods());
    }

    // set of annotation class names to capture
    private static final Set<String> CONSTRUCTOR_ANN = Collections.singleton(DataBoundConstructor.class.getName());
    private static final Set<String> HANDLER_ANN = new HashSet<String>();

    static {
        for (Class c : AnnotationHandler.HANDLERS.keySet())
            HANDLER_ANN.add(c.getName());
    }

    private void handleMethods(List<MethodNode> methods) {
        for (MethodNode m : methods)
            // copy the array as we'll modify them
            if(hasAnnotation(m,CONSTRUCTOR_ANN) || hasInjectionAnnotation(m))
                write(m);
    }

    private boolean hasInjectionAnnotation(MethodNode m) {
        for (Parameter p : m.getParameters())
            if(hasAnnotation(p,HANDLER_ANN))
                return true;
        return false;
    }


    private boolean hasAnnotation(AnnotatedNode target, Set<String> annotationTypeNames) {
        for (AnnotationNode a : (List<AnnotationNode>)target.getAnnotations())
            if(annotationTypeNames.contains(a.getClassNode().getName()))
                return true;
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
