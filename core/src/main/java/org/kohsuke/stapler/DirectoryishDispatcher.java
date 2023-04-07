package org.kohsuke.stapler;

import javax.servlet.ServletException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * {@link Dispatcher} that tells browsers to append '/' to the request path and try again.
 *
 * If we are serving the index page, we demand that the URL be '/some/dir/' not '/some/dir'
 * so that relative links in the page will resolve correctly. Apache does the same thing.
 *
 * @author Kohsuke Kawaguchi
 */
class DirectoryishDispatcher extends Dispatcher {

    @Override
    public boolean dispatch(RequestImpl req, ResponseImpl rsp, Object node) throws IOException, ServletException, IllegalAccessException, InvocationTargetException {
        if(!req.tokens.hasMore()) {
            return handleNoTokens(req, rsp);
        }

        return false;
    }

    private boolean handleNoTokens(RequestImpl req, ResponseImpl rsp) throws IOException {
        String servletPath = req.stapler.getServletPath(req);
        if(!servletPath.endsWith("/")) {
            return handleRedirect(req, rsp, servletPath);
        }

        return false;
    }

    private boolean handleRedirect(RequestImpl req, ResponseImpl rsp, String servletPath) throws IOException {
        String target = req.getContextPath() + servletPath + '/';
        if(req.getQueryString()!=null)
            target += '?' + req.getQueryString();
        if(LOGGER.isLoggable(Level.FINER))
            LOGGER.finer("Redirecting to "+target);
        rsp.sendRedirect2(target);
        return true;
    }


    @Override
    public String toString() {
        return "If path ends without '/' insert it";
    }

    private static final Logger LOGGER = Logger.getLogger(DirectoryishDispatcher.class.getName());
}
