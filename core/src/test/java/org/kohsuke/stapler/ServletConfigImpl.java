package org.kohsuke.stapler;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import java.util.Enumeration;

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
