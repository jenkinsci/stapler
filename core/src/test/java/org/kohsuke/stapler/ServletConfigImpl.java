package org.kohsuke.stapler;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import java.util.Enumeration;

/**
 * @author Kohsuke Kawaguchi
 */
public class ServletConfigImpl implements ServletConfig {
    ServletContext context = new MockServletContext();

    public String getServletName() {
        return "";
    }

    public ServletContext getServletContext() {
        return context;
    }

    public String getInitParameter(String name) {
        return null;
    }

    public Enumeration getInitParameterNames() {
        return null;
    }
}
