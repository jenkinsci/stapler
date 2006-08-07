package org.kohsuke.stapler.subloader;

import javax.servlet.ServletContext;

/**
 * @author Kohsuke Kawaguchi
 */
public class SubWebAppServletContext extends FilterServletContext {
    public SubWebAppServletContext(ServletContext parent) {
        super(parent);
    }

    
}
