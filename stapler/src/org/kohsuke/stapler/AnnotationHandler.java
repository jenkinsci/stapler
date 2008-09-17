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
    abstract String parse(HttpServletRequest request, T a) throws ServletException;

    static Object handle(HttpServletRequest request, Annotation[] annotations, Class targetType) throws ServletException {
        for (Annotation a : annotations) {
            AnnotationHandler h = HANDLERS.get(a.annotationType());
            if(h==null)     continue;

            Converter converter = Stapler.lookupConverter(targetType);
            if (converter==null)
                throw new IllegalArgumentException("Unable to convert to "+targetType);

            return converter.convert(targetType,h.parse(request,a));
        }

        return null; // probably we should report an error
    }


    static final Map<Class<? extends Annotation>,AnnotationHandler> HANDLERS = new HashMap<Class<? extends Annotation>, AnnotationHandler>();

    static {
        HANDLERS.put(Header.class,new AnnotationHandler<Header>() {
            String parse(HttpServletRequest request, Header a) throws ServletException {
                String value = request.getHeader(a.value());
                if(a.required() && value!=null)
                    throw new ServletException("Required HTTP header "+a.value()+" is missing");

                return value;
            }
        });

        HANDLERS.put(QueryParameter.class,new AnnotationHandler<QueryParameter>() {
            String parse(HttpServletRequest request, QueryParameter a) throws ServletException {
                String value = request.getParameter(a.value());
                if(a.required() && value!=null)
                    throw new ServletException("Required Query parameter "+a.value()+" is missing");

                return value;
            }
        });
    }
}
