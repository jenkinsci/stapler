package org.kohsuke.stapler.jelly;

import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.Script;
import org.kohsuke.stapler.MetaClass;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.WebApp;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;

/**
 * @author Kohsuke Kawaguchi
 */
public final class JellyRequestDispatcher implements RequestDispatcher {
    private final Object it;
    private final Script script;
    private final JellyFacet facet;

    public JellyRequestDispatcher(Object it, Script script) {
        this.it = it;
        this.script = script;
        facet = WebApp.getCurrent().getFacet(JellyFacet.class);
    }

    public void forward(ServletRequest servletRequest, ServletResponse servletResponse) throws ServletException, IOException {
        try {
            facet.scriptInvoker.invokeScript(
                (StaplerRequest)servletRequest,
                (StaplerResponse)servletResponse,
                script, it);
        } catch (JellyTagException e) {
            throw new ServletException(e);
        }
    }

    public void include(ServletRequest servletRequest, ServletResponse servletResponse) throws ServletException, IOException {
        forward(servletRequest,servletResponse);
    }
}
