/*
 * Copyright (c) 2004-2010, Kohsuke Kawaguchi
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided
 * that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice, this list of
 *       conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright notice, this list of
 *       conditions and the following disclaimer in the documentation and/or other materials
 *       provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS
 * OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
 * AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER
 * IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF
 * THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

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
 * and includes the contents of it, just like {@code <jsp:include>}.
 *
 * <p>
 * For example, if the "it" object is an instance of the <tt>Foo</tt> class,
 * which looks like the following:
 *
 * <pre>{@code
 * class Foo extends Bar { ... }
 * class Bar extends Zot { ... }
 * }</pre>
 *
 * <p>
 * And if you write:
 * <pre>{@code
 * <st:include page="abc.jsp"/>
 * }</pre>
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