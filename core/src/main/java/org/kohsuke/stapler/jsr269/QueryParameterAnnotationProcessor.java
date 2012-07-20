package org.kohsuke.stapler.jsr269;

import org.apache.commons.io.IOUtils;
import org.kohsuke.MetaInfServices;
import org.kohsuke.stapler.QueryParameter;

import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.tools.FileObject;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Kohsuke Kawaguchi
 */
@SuppressWarnings({"Since15"})
@SupportedSourceVersion(SourceVersion.RELEASE_6)
@SupportedAnnotationTypes("org.kohsuke.stapler.QueryParameter")
@MetaInfServices(Processor.class)
public class QueryParameterAnnotationProcessor extends AbstractProcessorImpl {
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        try {
            Set<? extends Element> params = roundEnv.getElementsAnnotatedWith(QueryParameter.class);
            Set<ExecutableElement> methods = new HashSet<ExecutableElement>();

            for (Element p : params) {
                // at least in JDK7u3, if some of the annotation types doesn't resolve, they end up showing up
                // in the result from the getElementsAnnotatedWith method. This check rejects those bogus matches
                if (p.getAnnotation(QueryParameter.class)!=null)
                    methods.add((ExecutableElement)p.getEnclosingElement());
            }

            for (ExecutableElement m : methods) {
                write(m);
            }
        } catch (IOException e) {
            error(e);
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

    /**
     * @param m
     *      Method whose parameter has {@link QueryParameter}
     */
    private void write(ExecutableElement m) throws IOException {
        StringBuffer buf = new StringBuffer();
        for( VariableElement p : m.getParameters() ) {
            if(buf.length()>0)  buf.append(',');
            buf.append(p.getSimpleName());
        }

        TypeElement t = (TypeElement)m.getEnclosingElement();
        FileObject f = createResource(t.getQualifiedName().toString().replace('.', '/') + "/" + m.getSimpleName() + ".stapler");
        notice("Generating " + f, m);

        OutputStream os = f.openOutputStream();
        try {
            IOUtils.write(buf, os, "UTF-8");
        } finally {
            os.close();
        }
    }
}
