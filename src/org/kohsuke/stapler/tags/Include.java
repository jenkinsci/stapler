package org.kohsuke.stapler.tags;

import javax.servlet.jsp.tagext.SimpleTagSupport;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author Kohsuke Kawaguchi
 */
public class Include extends SimpleTagSupport {

    private String page;

    public void setPage(String page) {
        this.page = page;
    }

    private Object getPageObject( String name ) {
        return getJspContext().getAttribute(name,PageContext.PAGE_SCOPE);
    }

    public void doTag() throws JspException, IOException {
        Object it = getJspContext().getAttribute("it",PageContext.REQUEST_SCOPE);

        ServletConfig cfg = (ServletConfig) getPageObject(PageContext.CONFIG);
        ServletContext sc = cfg.getServletContext();

        for( Class c = it.getClass(); c!=Object.class; c=c.getSuperclass() ) {
            String name = "/WEB-INF/side-files/"+c.getName().replace('.','/')+'/'+page;
            if(sc.getResource(name)!=null) {
                // Tomcat returns a RequestDispatcher even if the JSP file doesn't exist.
                // so check if the resource exists first.
                RequestDispatcher disp = sc.getRequestDispatcher(name);
                if(disp!=null) {
                    try {
                        disp.include(
                            (HttpServletRequest)getPageObject(PageContext.REQUEST),
                            new Wrapper(
                                (HttpServletResponse)getPageObject(PageContext.RESPONSE),
                                new PrintWriter(getJspContext().getOut()) )
                        );
                    } catch (ServletException e) {
                        throw new JspException(e);
                    }
                    return;
                }
            }
        }

        throw new JspException("Unable to find '"+page+"' for "+it.getClass());
    }
}

class Wrapper extends HttpServletResponseWrapper {
    private final PrintWriter pw;

    public Wrapper(HttpServletResponse httpServletResponse, PrintWriter w) {
        super(httpServletResponse);
        this.pw = w;
    }

    public PrintWriter getWriter() throws IOException {
        return pw;
    }
}