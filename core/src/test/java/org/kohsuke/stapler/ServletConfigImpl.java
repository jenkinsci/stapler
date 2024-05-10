package org.kohsuke.stapler;

import java.util.Enumeration;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

/**
 * @author Kohsuke Kawaguchi
 */
public class ServletConfigImpl implements ServletConfig {
    ServletContext context = new MockServletContext();

    @Override
    public String getServletName() {
        return "";
    }

    @Override
    public ServletContext getServletContext() {
        return context;
    }

    @Override
    public String getInitParameter(String name) {
        return null;
    }

    @Override
    public Enumeration getInitParameterNames() {
        return null;
    }
}
