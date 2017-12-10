package org.kohsuke.stapler.interceptor;

import java.io.IOException;
import java.io.PrintWriter;

import org.kohsuke.stapler.ForwardToView;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationTargetException;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import org.kohsuke.stapler.HttpResponses;
import org.kohsuke.stapler.verb.POST;

/**
 * Requires the request to be a POST.
 *
 * <p>
 * When the current request has a non-matching HTTP method (such as 'GET'), this annotation
 * will send a failure response instead of searching for other matching web methods.
 *
 * @author Kohsuke Kawaguchi
 * @since 1.180
 * @see POST
 */
@Retention(RUNTIME)
@Target({METHOD,FIELD})
@InterceptorAnnotation(RequirePOST.Processor.class)
public @interface RequirePOST {

    /**
     * Allows customizing the error page shown when an annotated method is called with the wrong HTTP method.
     */
    interface ErrorHandler {
        ForwardToView getForwardView();
    }

    public static class Processor extends Interceptor {
        private static ErrorHandler handler;

        /**
         * Register the custom error handler to be used when an annotated method is called with the wrong HTTP method.
         * @param errorHandler the error handler providing the view to show instead
         */
        public static void registerErrorHandler(ErrorHandler errorHandler) {
            handler = errorHandler;
        }

        @Override
        public Object invoke(StaplerRequest request, StaplerResponse response, Object instance, Object[] arguments)
                throws IllegalAccessException, InvocationTargetException, ServletException {
            if (!request.getMethod().equals("POST")) {
                if (handler != null) {
                    ForwardToView forwardToView = handler.getForwardView();
                    if (forwardToView != null) {
                        throw new InvocationTargetException(forwardToView.with("requestURL", request.getRequestURLWithQueryString().toString()));
                    }
                }
                throw new InvocationTargetException(new HttpResponses.HttpResponseException() {
                    public void generateResponse(StaplerRequest req, StaplerResponse rsp, Object node) throws IOException, ServletException {
                        rsp.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
                        rsp.addHeader("Allow", "POST");
                        rsp.setContentType("text/html");
                        PrintWriter w = rsp.getWriter();
                        w.println("<html><head><title>POST required</title></head><body>");
                        w.println("POST is required for " + target.getQualifiedName() + "<br>");
                        w.println("<form method='POST'><input type='submit' value='Try POSTing'></form>");
                        w.println("</body></html>");
                    }
                });
            }
            return target.invoke(request, response, instance, arguments);
        }
    }
}
