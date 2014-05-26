package org.kohsuke.stapler.interceptor;

import java.io.IOException;
import java.io.PrintWriter;
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

/**
 * Requires the request to be a POST.
 *
 * @author Kohsuke Kawaguchi
 * @since 1.180
 */
@Retention(RUNTIME)
@Target({METHOD,FIELD})
@InterceptorAnnotation(RequirePOST.Processor.class)
public @interface RequirePOST {
    public static class Processor extends Interceptor {
        @Override
        public Object invoke(StaplerRequest request, StaplerResponse response, Object instance, Object[] arguments)
                throws IllegalAccessException, InvocationTargetException {
            if (!request.getMethod().equals("POST")) {
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
