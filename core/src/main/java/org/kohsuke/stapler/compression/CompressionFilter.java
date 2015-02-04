package org.kohsuke.stapler.compression;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Pimps up {@link HttpServletResponse} so that it understands "Content-Encoding: gzip" and compress the response.
 * 
 * <p>
 * When exceptions are processed within web applications, different unrelated parts of the webapp can end up calling
 * {@link HttpServletResponse#getOutputStream()}. This fundamentally doesn't work with the notion that the application
 * needs to process "Content-Encoding:gzip" on its own. Such app has to maintain a GZIP output stream on its own,
 * since {@link HttpServletResponse} doesn't know that its output is written through a compressed stream.
 *
 * <p>
 * Another place this break-down can be seen is when a servlet throws an exception that the container handles.
 * It tries to render an error page, but of course it wouldn't know that "Content-Encoding:gzip" is set, so it
 * will fail to write in the correct format.
 * 
 * <p>
 * The only way to properly fix this is to make {@link HttpServletResponse} smart enough that it returns
 * the compression-transparent stream from {@link HttpServletResponse#getOutputStream()} (and it would also
 * have to process the exception thrown from the app.) This filter does exactly that.
 *
 * @author Kohsuke Kawaguchi
 */
public class CompressionFilter implements Filter {
    private ServletContext context;

    public void init(FilterConfig filterConfig) throws ServletException {
        context = filterConfig.getServletContext();
    }

    public void doFilter(ServletRequest _req, ServletResponse _rsp, FilterChain filterChain) throws IOException, ServletException {
        Object old1 = swapAttribute(_req, CompressionFilter.class, true);

        CompressionServletResponse rsp = new CompressionServletResponse(((HttpServletResponse) _rsp));
        Object old2 = swapAttribute(_req,CompressionServletResponse.class,rsp);

        try {
            filterChain.doFilter(_req, rsp);
        } catch (IOException e) {
            if (DISABLED)   throw e;
            reportException(e,(HttpServletRequest)_req,rsp);
        } catch (ServletException e) {
            if (DISABLED)   throw e;
            reportException(e,(HttpServletRequest)_req,rsp);
        } catch (RuntimeException e) {
            if (DISABLED)   throw e;
            reportException(e,(HttpServletRequest)_req,rsp);
        } catch (Error e) {
            if (DISABLED)   throw e;
            reportException(e,(HttpServletRequest)_req,rsp);
        } finally {
            rsp.close();

            _req.setAttribute(CompressionFilter.class.getName(),old1);
            _req.setAttribute(CompressionServletResponse.class.getName(),old2);
        }
    }

    private Object swapAttribute(ServletRequest req, Class<?> key, Object value) {
        Object old = req.getAttribute(key.getName());
        req.setAttribute(key.getName(), value);
        return old;
    }

    private void reportException(Throwable e, HttpServletRequest req, HttpServletResponse rsp) throws IOException, ServletException {
        getUncaughtExceptionHandler(context).reportException(e, context, req, rsp);
    }

    public void destroy() {
    }

    public static void setUncaughtExceptionHandler(ServletContext context, UncaughtExceptionHandler handler) {
        context.setAttribute(UncaughtExceptionHandler.class.getName(),handler);
    }

    public static UncaughtExceptionHandler getUncaughtExceptionHandler(ServletContext context) {
        UncaughtExceptionHandler h = (UncaughtExceptionHandler) context.getAttribute(UncaughtExceptionHandler.class.getName());
        if (h==null)    h=UncaughtExceptionHandler.DEFAULT;
        return h;
    }

    /**
     * Is this request already wrapped into {@link CompressionFilter}?
     */
    public static boolean has(ServletRequest req) {
        return req.getAttribute(CompressionServletResponse.class.getName())!=null;
    }

    /**
     * Is this request already wrapped into {@link CompressionFilter},
     * activate that, so that {@link ServletResponse#getOutputStream()} will return
     * a stream that automatically handles compression.
     */
    public static boolean activate(ServletRequest req) throws IOException {
        CompressionServletResponse rsp = (CompressionServletResponse) req.getAttribute(CompressionServletResponse.class.getName());
        if (rsp!=null) {
            rsp.activate();
            return true;
        } else {
            return false;
        }
    }

    /**
     * Disables the effect of {@link #setUncaughtExceptionHandler}, letting all errors be rethrown.
     * Despite its name, this flag does <strong>not</strong> disable {@link CompressionFilter} itself!
     * Rather use {@code DefaultScriptInvoker.COMPRESS_BY_DEFAULT}.
     */
    public static boolean DISABLED = Boolean.getBoolean(CompressionFilter.class.getName()+".disabled");
}
