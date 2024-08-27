package org.kohsuke.stapler.jsr269;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
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

            Map<String, String> output = new HashMap<>();
            for (ExecutableElement m : methods) {
                String paramNames = m.getParameters().stream()
                        .map(VariableElement::getSimpleName)
                        .collect(Collectors.joining(","));
                String existing = output.get(m.getSimpleName().toString());
                /*
                 * Allow multiple methods to have the same name but different argument types as long as the arguments
                 * have the same names. This allows deprecated StaplerRequest/StaplerResponse methods to coexist
                 * alongside non-deprecated StaplerRequest2/StaplerResponse2 methods.
                 */
                if (existing == null || !existing.equals(paramNames)) {
                    write(paramNames, m);
                    output.put(m.getSimpleName().toString(), paramNames);
                }
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
    private void write(String paramNames, ExecutableElement m) throws IOException {
        TypeElement t = (TypeElement) m.getEnclosingElement();
        String name = t.getQualifiedName().toString().replace('.', '/') + "/" + m.getSimpleName() + ".stapler";
        FileObject f = createResource(name);
        notice("Generating " + name, m);

        try (OutputStream os = f.openOutputStream()) {
            os.write(paramNames.getBytes(StandardCharsets.UTF_8));
        }
    }
}
