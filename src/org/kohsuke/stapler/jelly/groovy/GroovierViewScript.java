package org.kohsuke.stapler.jelly.groovy;

import groovy.lang.Binding;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

/**
 * Base class for groovy script files used as stapler views and Groovy taglibs.
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class GroovierViewScript extends GroovyClosureScript {
    private StaplerRequest request;
    private StaplerResponse response;
    private String rootURL;

    protected GroovierViewScript() {
    }

    protected GroovierViewScript(Binding binding) {
        super(binding);
    }

    public JellyBuilder getDelegate() {
        return (JellyBuilder) super.getDelegate();
    }

//
//
// Methods that are exposed to the Groovy script
//
//

    /**
     * Loads a tag library instance (if not done so already)
     */
    public Object taglib(Class type) throws InstantiationException, IllegalAccessException {
        return getDelegate().taglib(type);
    }

    public StaplerRequest getRequest() {
        if(request==null)
            request = Stapler.getCurrentRequest();
        return request;
    }
    
    public StaplerResponse getResponse() {
        if(response!=null)
            response = Stapler.getCurrentResponse();
        return response;
    }

    /**
     * Gets the absolute URL to the top of the webapp.
     *
     * @see StaplerRequest#getContextPath()
     */
    public String getRootURL() {
        if(rootURL==null)
            rootURL = getRequest().getContextPath();
        return rootURL;
    }
}
