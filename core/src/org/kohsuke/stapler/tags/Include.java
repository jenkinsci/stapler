package org.kohsuke.stapler.tags;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.SimpleTagSupport;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Includes a side JSP file for the "it" object.
 *
 * <p>
 * This tag looks for a side JSP file of the given name
 * from the inheritance hierarchy of the it object,
 * and includes the contents of it, just like &lt;jsp:include>.
 *
 * <p>
 * For example, if the "it" object is an instance of the <tt>Foo</tt> class,
 * which looks like the following:
 *
 * <pre>
 * class Foo extends Bar { ... }
 * class Bar extends Zot { ... }
 * </pre>
 *
 * <p>
 * And if you write:
 * <pre><xmp>
 * <st:include page="abc.jsp"/>
 * </xmp></pre>
 * then, it looks for the following files in this order,
 * and includes the first one found.
 * <ol>
 *  <li>a side-file of the Foo class named abc.jsp (/WEB-INF/side-files/Foo/abc.jsp)
 *  <li>a side-file of the Bar class named abc.jsp (/WEB-INF/side-files/Bar/abc.jsp)
 *  <li>a side-file of the Zot class named abc.jsp (/WEB-INF/side-files/Zot/abc.jsp)
 * </ol>
 *
 * @author Kohsuke Kawaguchi
 */
public class Include extends SimpleTagSupport {

    private Object it;

    private String page;

    /**
     * Specifies the name of the JSP to be included.
     */
    public void setPage(String page) {
        this.page = page;
    }

    /**
     * Specifies the object for which JSP will be included.
     */
    public void setIt(Object it) {
        this.it = it;
    }

    private Object getPageObject( String name ) {
        return getJspContext().getAttribute(name,PageContext.PAGE_SCOPE);
    }

    public void doTag() throws JspException, IOException {
        Object it = getJspContext().getAttribute("it",PageContext.REQUEST_SCOPE);
        final Object oldIt = it;
        if(this.it!=null)
            it = this.it;

        ServletConfig cfg = (ServletConfig) getPageObject(PageContext.CONFIG);
        ServletContext sc = cfg.getServletContext();

        for( Class c = it.getClass(); c!=Object.class; c=c.getSuperclass() ) {
            String name = "/WEB-INF/side-files/"+c.getName().replace('.','/')+'/'+page;
            if(sc.getResource(name)!=null) {
                // Tomcat returns a RequestDispatcher even if the JSP file doesn't exist.
                // so check if the resource exists first.
                RequestDispatcher disp = sc.getRequestDispatcher(name);
                if(disp!=null) {
                    getJspContext().setAttribute("it",it,PageContext.REQUEST_SCOPE);
                    try {
                        HttpServletRequest request = (HttpServletRequest) getPageObject(PageContext.REQUEST);
                        disp.include(
                            request,
                            new Wrapper(
                                (HttpServletResponse)getPageObject(PageContext.RESPONSE),
                                new PrintWriter(getJspContext().getOut()) )
                        );
                    } catch (ServletException e) {
                        throw new JspException(e);
                    } finally {
                        getJspContext().setAttribute("it",oldIt,PageContext.REQUEST_SCOPE);
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