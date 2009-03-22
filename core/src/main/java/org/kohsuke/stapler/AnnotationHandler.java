package org.kohsuke.stapler;

import org.apache.commons.beanutils.Converter;

import javax.servlet.ServletException;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

/**
 * Handles stapler parameter annotations by determining what values to inject for a method call.
 *
 * @author Kohsuke Kawaguchi
 */
abstract class AnnotationHandler<T extends Annotation> {
    abstract Object parse(StaplerRequest request, T a, Class type, String parameterName) throws ServletException;

    /**
     * Helper method for {@link #parse(StaplerRequest, Annotation, Class, String)} to convert to the right type
     * from String.
     */
    protected final Object convert(Class targetType, String value) {
        Converter converter = Stapler.lookupConverter(targetType);
        if (converter==null)
            throw new IllegalArgumentException("Unable to convert to "+targetType);

        return converter.convert(targetType,value);
    }

    static Object handle(StaplerRequest request, Annotation[] annotations, String parameterName, Class targetType) throws ServletException {
        for (Annotation a : annotations) {
            AnnotationHandler h = HANDLERS.get(a.annotationType());
            if(h==null)     continue;

            Converter converter = Stapler.lookupConverter(targetType);
            if (converter==null)
                throw new IllegalArgumentException("Unable to convert to "+targetType);

            return h.parse(request,a,targetType,parameterName);
        }

        return null; // probably we should report an error
    }


    static final Map<Class<? extends Annotation>,AnnotationHandler> HANDLERS = new HashMap<Class<? extends Annotation>, AnnotationHandler>();

    static {
        HANDLERS.put(Header.class,new AnnotationHandler<Header>() {
            Object parse(StaplerRequest request, Header a, Class type, String parameterName) throws ServletException {
                String name = a.value();
                if(name.length()==0)    name=parameterName;
                if(name==null)
                    throw new IllegalArgumentException("Parameter name unavailable neither in the code nor in annotation");

                String value = request.getHeader(name);
                if(a.required() && value==null)
                    throw new ServletException("Required HTTP header "+name+" is missing");

                return convert(type,value);
            }
        });

        HANDLERS.put(QueryParameter.class,new AnnotationHandler<QueryParameter>() {
            Object parse(StaplerRequest request, QueryParameter a, Class type, String parameterName) throws ServletException {
                String name = a.value();
                if(name.length()==0)    name=parameterName;
                if(name==null)
                    throw new IllegalArgumentException("Parameter name unavailable neither in the code nor in annotation");
                
                String value = request.getParameter(name);
                if(a.required() && value==null)
                    throw new ServletException("Required Query parameter "+name+" is missing");

                return convert(type,value);
            }
        });

        HANDLERS.put(AncestorInPath.class,new AnnotationHandler<AncestorInPath>() {
            Object parse(StaplerRequest request, AncestorInPath a, Class type, String parameterName) throws ServletException {
                return request.findAncestorObject(type);
            }
        });
    }
}
