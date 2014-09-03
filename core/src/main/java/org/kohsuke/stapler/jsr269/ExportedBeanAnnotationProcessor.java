package org.kohsuke.stapler.jsr269;

import org.kohsuke.MetaInfServices;
import org.kohsuke.stapler.export.Exported;

import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.FileObject;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author Kohsuke Kawaguchi
 */
@SuppressWarnings({"Since15"})
@SupportedSourceVersion(SourceVersion.RELEASE_6)
@SupportedAnnotationTypes("org.kohsuke.stapler.export.Exported")
@MetaInfServices(Processor.class)
public class ExportedBeanAnnotationProcessor extends AbstractProcessorImpl {

    private Set<String> exposedBeanNames;

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        try {
            if (roundEnv.processingOver()) {
                FileObject beans = createResource(STAPLER_BEAN_FILE);
                PrintWriter w = new PrintWriter(new OutputStreamWriter(beans.openOutputStream(), "UTF-8"));
                for (String beanName : exposedBeanNames) {
                    w.println(beanName);
                }
                w.close();
                return false;
            }

            // collect all exposed properties
            PoormansMultimap<TypeElement, Element/*member decls*/> props = new PoormansMultimap<TypeElement,Element>();

            for (Element exported : roundEnv.getElementsAnnotatedWith(Exported.class)) {
                Element type = exported.getEnclosingElement();
                if (type.getKind().isClass() || type.getKind().isInterface()) {
                    props.put((TypeElement)type, exported);
                }
            }

            if (exposedBeanNames == null) {
                scanExisting();
            }

            for (Entry<TypeElement, Collection<Element>> e : props.asMap().entrySet()) {
                exposedBeanNames.add(e.getKey().getQualifiedName().toString());

                final Properties javadocs = new Properties();
                for (Element md : e.getValue()) {
                    switch (md.getKind()) {
                    case FIELD:
                    case METHOD:
                       String javadoc = getJavadoc(md);
                       if(javadoc!=null)
                           javadocs.put(md.getSimpleName().toString(), javadoc);
                        break;
                    default:
                        throw new AssertionError("Unexpected element type: "+md);
                    }
                        // TODO: possibly a proper method signature generation, but it's too tedious
                        // way too tedious.
                        //private String getSignature(MethodDeclaration m) {
                        //    final StringBuilder buf = new StringBuilder(m.getSimpleName());
                        //    buf.append('(');
                        //    boolean first=true;
                        //    for (ParameterDeclaration p : m.getParameters()) {
                        //        if(first)   first = false;
                        //        else        buf.append(',');
                        //        p.getType().accept(new SimpleTypeVisitor() {
                        //            public void visitPrimitiveType(PrimitiveType pt) {
                        //                buf.append(pt.getKind().toString().toLowerCase());
                        //            }
                        //            public void visitDeclaredType(DeclaredType dt) {
                        //                buf.append(dt.getDeclaration().getQualifiedName());
                        //            }
                        //
                        //            public void visitArrayType(ArrayType at) {
                        //                at.getComponentType().accept(this);
                        //                buf.append("[]");
                        //            }
                        //
                        //            public void visitTypeVariable(TypeVariable tv) {
                        //
                        //                // TODO
                        //                super.visitTypeVariable(typeVariable);
                        //            }
                        //
                        //            public void visitVoidType(VoidType voidType) {
                        //                // TODO
                        //                super.visitVoidType(voidType);
                        //            }
                        //        });
                        //    }
                        //    buf.append(')');
                        //    // TODO
                        //    return null;
                        //}
                }

                String javadocFile = e.getKey().getQualifiedName().toString().replace('.', '/') + ".javadoc";
                notice("Generating "+ javadocFile, e.getKey());
                writePropertyFile(javadocs, javadocFile);
            }

        } catch (IOException x) {
            error(x);
        } catch (RuntimeException e) {
            // javac sucks at reporting errors in annotation processors
            e.printStackTrace();
            throw e;
        } catch (Error e) {
            e.printStackTrace();
            throw e;
        }
        return false;
    }

    private void scanExisting() throws IOException {
        exposedBeanNames = new TreeSet<String>();

        try {
            FileObject beans = getResource(STAPLER_BEAN_FILE);
            BufferedReader in = new BufferedReader(new InputStreamReader(beans.openInputStream(),"UTF-8"));
            String line;
            while((line=in.readLine())!=null)
                exposedBeanNames.add(line.trim());
            in.close();
        } catch (FileNotFoundException e) {
            // no existing file, which is fine
        }
    }

    static final String STAPLER_BEAN_FILE = "META-INF/exposed.stapler-beans";
}
