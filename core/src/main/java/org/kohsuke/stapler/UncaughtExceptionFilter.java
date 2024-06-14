package org.kohsuke.stapler;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class UncaughtExceptionFilter implements Filter {
    private ServletContext context;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        context = filterConfig.getServletContext();
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse rsp, FilterChain filterChain)
            throws IOException, ServletException {
        try {
            filterChain.doFilter(req, rsp);
        } catch (IOException | Error | RuntimeException | ServletException e) {
            if (DISABLED) {
                throw e;
            }
            reportException(e, (HttpServletRequest) req, (HttpServletResponse) rsp);
        }
    }

    private void reportException(Throwable e, HttpServletRequest req, HttpServletResponse rsp)
            throws IOException, ServletException {
        getUncaughtExceptionHandler(context).reportException(e, context, req, rsp);
    }

    @Override
    public void destroy() {}

    public static void setUncaughtExceptionHandler(ServletContext context, UncaughtExceptionHandler handler) {
        context.setAttribute(UncaughtExceptionHandler.class.getName(), handler);
    }

    public static UncaughtExceptionHandler getUncaughtExceptionHandler(ServletContext context) {
        UncaughtExceptionHandler h =
                (UncaughtExceptionHandler) context.getAttribute(UncaughtExceptionHandler.class.getName());
        if (h == null) {
            h = UncaughtExceptionHandler.DEFAULT;
        }
        return h;
    }

    /**
     * Disables the effect of {@link #setUncaughtExceptionHandler}, letting all errors be rethrown.
     */
    @SuppressFBWarnings(value = "MS_SHOULD_BE_FINAL", justification = "for script console")
    public static boolean DISABLED = Boolean.getBoolean(UncaughtExceptionFilter.class.getName() + ".disabled");
}
