package org.kohsuke.stapler;

import org.apache.commons.jelly.Script;
import org.apache.commons.jelly.JellyTagException;
import org.kohsuke.stapler.MetaClass;
import org.kohsuke.stapler.RequestImpl;
import org.kohsuke.stapler.ResponseImpl;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.ServletException;
import java.io.IOException;

/**
 * @author Kohsuke Kawaguchi
 */
public class JellyRequestDispatcher implements RequestDispatcher {
    private final Object it;
    private final Script script;

    public JellyRequestDispatcher(Object it, Script script) {
        this.it = it;
        this.script = script;
    }

    public void forward(ServletRequest servletRequest, ServletResponse servletResponse) throws ServletException, IOException {
        try {
            MetaClass.get(it.getClass()).invokeScript(
                (RequestImpl)servletRequest,
                (ResponseImpl)servletResponse,
                script, it);
        } catch (JellyTagException e) {
            throw new ServletException(e);
        }
    }

    public void include(ServletRequest servletRequest, ServletResponse servletResponse) throws ServletException, IOException {
        forward(servletRequest,servletResponse);
    }
}
