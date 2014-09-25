package org.kohsuke.stapler;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * {@link Filter} that sets the thread name to reflect the current request being processed.
 *
 * @author Kohsuke Kawaguchi
 */
public class DiagnosticThreadNameFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse rsp, FilterChain chain) throws IOException, ServletException {
        Thread t = Thread.currentThread();
        final String oldName = t.getName();
        try {
            HttpServletRequest hreq = (HttpServletRequest) req;
            t.setName("Handling " + hreq.getMethod() + ' ' + hreq.getRequestURI() + " from "+hreq.getRemoteAddr()+" : " + oldName);

            chain.doFilter(req, rsp);
        } finally {
            t.setName(oldName);
        }
    }

    @Override
    public void destroy() {
    }
}
