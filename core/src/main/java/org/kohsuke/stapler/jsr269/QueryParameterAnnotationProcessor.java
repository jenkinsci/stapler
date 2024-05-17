package org.kohsuke.stapler.jsr269;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.tools.FileObject;
import org.kohsuke.MetaInfServices;
import org.kohsuke.stapler.QueryParameter;

/**
 * @author Kohsuke Kawaguchi
 */
@SupportedAnnotationTypes("org.kohsuke.stapler.QueryParameter")
@MetaInfServices(Processor.class)
public class QueryParameterAnnotationProcessor extends AbstractProcessorImpl {
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        try {
            Set<? extends Element> params = roundEnv.getElementsAnnotatedWith(QueryParameter.class);
            Set<ExecutableElement> methods = new HashSet<>();

            for (Element p : params) {
                // at least in JDK7u3, if some of the annotation types doesn't resolve, they end up showing up
                // in the result from the getElementsAnnotatedWith method. This check rejects those bogus matches
                if (p.getAnnotation(QueryParameter.class) != null) {
                    methods.add((ExecutableElement) p.getEnclosingElement());
                }
            }

            for (ExecutableElement m : methods) {
                write(m);
            }
        } catch (IOException e) {
            error(e);
        } catch (RuntimeException | Error e) {
            // javac sucks at reporting errors in annotation processors
            e.printStackTrace();
            throw e;
        }
        return false;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }

    /**
     * @param m
     *      Method whose parameter has {@link QueryParameter}
     */
    private void write(ExecutableElement m) throws IOException {
        StringBuilder buf = new StringBuilder();
        for (VariableElement p : m.getParameters()) {
            if (buf.length() > 0) {
                buf.append(',');
            }
            buf.append(p.getSimpleName());
        }

        TypeElement t = (TypeElement) m.getEnclosingElement();
        FileObject f = createResource(
                t.getQualifiedName().toString().replace('.', '/') + "/" + m.getSimpleName() + ".stapler");
        notice("Generating " + f, m);

        try (OutputStream os = f.openOutputStream()) {
            os.write(buf.toString().getBytes(StandardCharsets.UTF_8));
        }
    }
}
