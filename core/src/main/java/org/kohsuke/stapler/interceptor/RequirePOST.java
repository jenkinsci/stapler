package org.kohsuke.stapler.interceptor;

import java.io.IOException;
import java.io.PrintWriter;

import org.kohsuke.stapler.ForwardToView;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationTargetException;
import java.util.ServiceLoader;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;
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
    interface ErrorCustomizer {
        /**
         * Return the {@link ForwardToView} showing a custom error page for {@link RequirePOST} annotated methods. This is
         * typically used to show a form with a "Try again using POST" button.
         *
         * <p>Implementations are looked up using {@link java.util.ServiceLoader}, the first implementation to return a non-null value will be used.</p>
         */
        @CheckForNull ForwardToView getForwardView();
    }

    class Processor extends Interceptor {
        @Override
        public Object invoke(StaplerRequest request, StaplerResponse response, Object instance, Object[] arguments)
                throws IllegalAccessException, InvocationTargetException, ServletException {
            if (!request.getMethod().equals("POST")) {
                for (ErrorCustomizer handler : ServiceLoader.load(ErrorCustomizer.class, request.getWebApp().getClassLoader())) {
                    ForwardToView forwardToView = handler.getForwardView();
                    if (forwardToView != null) {
                        throw new InvocationTargetException(forwardToView.with("requestURL", request.getRequestURLWithQueryString().toString()));
                    }
                }
                throw new InvocationTargetException(new HttpResponses.HttpResponseException() {
                    @Override
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
