package org.kohsuke.stapler.jelly;

import org.apache.commons.jelly.TagSupport;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletContext;

/**
 * @author Kohsuke Kawaguchi
 */
abstract class AbstractStaplerTag extends TagSupport {

    protected HttpServletRequest getRequest() {
        return (HttpServletRequest)getContext().getVariable("request");
    }

    protected HttpServletResponse getResponse() {
        return (HttpServletResponse)getContext().getVariable("response");
    }

    protected ServletContext getServletContext() {
        return (ServletContext)getContext().getVariable("servletContext");
    }
}
