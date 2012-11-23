package org.kohsuke.stapler.jelly.groovy;

import groovy.servlet.ServletBinding;
import groovy.text.Template;
import org.kohsuke.stapler.WebApp;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/**
 * @author Kohsuke Kawaguchi
 */
public class GroovyServerPage {
    private final WebApp webApp;
    private final Template template;

    public GroovyServerPage(WebApp webApp, Template template) {
        this.webApp = webApp;
        this.template = template;
    }

    public void invoke(Object it, HttpServletRequest request, HttpServletResponse response) throws IOException {
        template.make(bindingOf(it,request,response)).writeTo(response.getWriter());
    }

    /**
     * Determines variables visible in GSP.
     */
    protected Map bindingOf(Object it, HttpServletRequest request, HttpServletResponse response) {
        ServletBinding binding = new ServletBinding(request, response, webApp.context);
        binding.setVariable("my",it);
        return binding.getVariables();
    }

    public RequestDispatcher asRequestDispatcher(final Object it) {
        return new RequestDispatcher() {
            public void forward(ServletRequest request, ServletResponse response) throws ServletException, IOException {
                invoke(it, (HttpServletRequest)request, (HttpServletResponse)response);
            }

            public void include(ServletRequest request, ServletResponse response) throws ServletException, IOException {
                forward(request, response);
            }
        };
    }
}
