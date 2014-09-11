package org.kohsuke.stapler.jsr269;

import org.kohsuke.MetaInfServices;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementScanner6;
import java.io.IOException;
import java.util.Properties;
import java.util.Set;
import javax.lang.model.element.Modifier;
import javax.tools.Diagnostic;

/**
 * @author Kohsuke Kawaguchi
 */
@SuppressWarnings({"Since15"})
@SupportedSourceVersion(SourceVersion.RELEASE_6)
@SupportedAnnotationTypes("*")
@MetaInfServices(Processor.class)
public class ConstructorProcessor extends AbstractProcessorImpl {
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        try {
            ElementScanner6<Void, Void> scanner = new ElementScanner6<Void, Void>() {
                @Override
                public Void visitExecutable(ExecutableElement e, Void aVoid) {
                    if(e.getAnnotation(DataBoundConstructor.class)!=null) {
                        write(e);
                    } else {
                        String javadoc = getJavadoc(e);
                        if(javadoc!=null && javadoc.contains("@stapler-constructor")) {
                            write(e);
                        }
                    }

                    return super.visitExecutable(e, aVoid);
                }
            };

            for (Element e : roundEnv.getRootElements()) {
                if (e.getKind() == ElementKind.PACKAGE) { // JENKINS-11739
                    continue;
                }
                scanner.scan(e, null);
            }

            return false;
        } catch (RuntimeException e) {
            // javac sucks at reporting errors in annotation processors
            e.printStackTrace();
            throw e;
        } catch (Error e) {
            e.printStackTrace();
            throw e;
        }
    }

    private void write(ExecutableElement c) {
        if (!c.getModifiers().contains(Modifier.PUBLIC)) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "@DataBoundConstructor must be applied to a public constructor", c);
            return;
        }
        if (c.getEnclosingElement().getModifiers().contains(Modifier.ABSTRACT)) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "@DataBoundConstructor may not be used on an abstract class (only on concrete subclasses)", c);
            return;
        }
        try {
            StringBuilder buf = new StringBuilder();
            for( VariableElement p : c.getParameters() ) {
                if(buf.length()>0)  buf.append(',');
                buf.append(p.getSimpleName());
            }

            TypeElement t = (TypeElement) c.getEnclosingElement();
            String name = t.getQualifiedName().toString().replace('.', '/') + ".stapler";
            notice("Generating " + name, c);

            Properties p = new Properties();
            p.put("constructor",buf.toString());
            writePropertyFile(p, name);
        } catch (IOException x) {
            error(x);
        }
    }
}
