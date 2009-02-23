package org.kohsuke.stapler;

import org.apache.commons.beanutils.Converter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Kohsuke Kawaguchi
 */
abstract class AnnotationHandler<T extends Annotation> {
    abstract String parse(HttpServletRequest request, T a, String parameterName) throws ServletException;

    static Object handle(HttpServletRequest request, Annotation[] annotations, String parameterName, Class targetType) throws ServletException {
        for (Annotation a : annotations) {
            AnnotationHandler h = HANDLERS.get(a.annotationType());
            if(h==null)     continue;

            Converter converter = Stapler.lookupConverter(targetType);
            if (converter==null)
                throw new IllegalArgumentException("Unable to convert to "+targetType);

            return converter.convert(targetType,h.parse(request,a,parameterName));
        }

        return null; // probably we should report an error
    }


    static final Map<Class<? extends Annotation>,AnnotationHandler> HANDLERS = new HashMap<Class<? extends Annotation>, AnnotationHandler>();

    static {
        HANDLERS.put(Header.class,new AnnotationHandler<Header>() {
            String parse(HttpServletRequest request, Header a, String parameterName) throws ServletException {
                String name = a.value();
                if(name.length()==0)    name=parameterName;
                if(name==null)
                    throw new IllegalArgumentException("Parameter name unavailable neither in the code nor in annotation");

                String value = request.getHeader(name);
                if(a.required() && value==null)
                    throw new ServletException("Required HTTP header "+name+" is missing");

                return value;
            }
        });

        HANDLERS.put(QueryParameter.class,new AnnotationHandler<QueryParameter>() {
            String parse(HttpServletRequest request, QueryParameter a, String parameterName) throws ServletException {
                String name = a.value();
                if(name.length()==0)    name=parameterName;
                if(name==null)
                    throw new IllegalArgumentException("Parameter name unavailable neither in the code nor in annotation");
                
                String value = request.getParameter(name);
                if(a.required() && value==null)
                    throw new ServletException("Required Query parameter "+name+" is missing");

                return value;
            }
        });
    }
}
