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

package org.kohsuke.stapler.jsp;

import org.kohsuke.stapler.Facet;
import org.kohsuke.stapler.MetaClass;
import org.kohsuke.stapler.Dispatcher;
import org.kohsuke.stapler.RequestImpl;
import org.kohsuke.stapler.ResponseImpl;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.MetaInfServices;
import org.kohsuke.stapler.lang.Klass;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletContext;
import java.util.List;
import java.util.logging.Level;
import java.io.IOException;

/**
 * {@link Facet} that adds JSP file support.
 *
 * @author Kohsuke Kawaguchi
 */
@MetaInfServices
public class JSPFacet extends Facet {
    public void buildViewDispatchers(MetaClass owner, List<Dispatcher> dispatchers) {
        dispatchers.add(new Dispatcher() {
            public boolean dispatch(RequestImpl req, ResponseImpl rsp, Object node) throws IOException, ServletException {
                String next = req.tokens.peek();
                if(next==null)  return false;

                Stapler stapler = req.getStapler();

                // check static resources
                RequestDispatcher disp = createRequestDispatcher(req,node.getClass(),node,next);
                if(disp==null) {
                    // check JSP views
                    disp = createRequestDispatcher(req,node.getClass(),node,next+".jsp");
                    if(disp==null)  return false;
                }

                req.tokens.next();

                if(traceable())
                    trace(req,rsp,"Invoking "+next+".jsp"+" on "+node+" for "+req.tokens);

                stapler.forward(disp,req,rsp);
                return true;
            }
            public String toString() {
                return "TOKEN.jsp for url=/TOKEN/...";
            }
        });
    }

    public RequestDispatcher createRequestDispatcher(RequestImpl request, Klass type, Object it, String viewName) throws IOException {
        ServletContext context = request.stapler.getServletContext();

        // JSP support is deprecated, and this doesn't work with non-Java objects
        for( Class c = type.toJavaClass(); c!=Object.class; c=c.getSuperclass() ) {
            String name = "/WEB-INF/side-files/"+c.getName().replace('.','/').replace('$','/')+'/'+viewName;
            if(context.getResource(name)!=null) {
                // Tomcat returns a RequestDispatcher even if the JSP file doesn't exist.
                // so check if the resource exists first.
                RequestDispatcher disp = context.getRequestDispatcher(name);
                if(disp!=null) {
                    return new RequestDispatcherWrapper(disp,it);
                }
            }
        }
        return null;
    }


    public boolean handleIndexRequest(RequestImpl req, ResponseImpl rsp, Object node, MetaClass nodeMetaClass) throws IOException, ServletException {
        Stapler stapler = req.stapler;
        
        // TODO: find the list of welcome pages for this class by reading web.xml
        RequestDispatcher indexJsp = createRequestDispatcher(req,node.getClass(),node,"index.jsp");
        if(indexJsp!=null) {
            if(LOGGER.isLoggable(Level.FINE))
                LOGGER.fine("Invoking index.jsp on "+node);
            stapler.forward(indexJsp,req,rsp);
            return true;
        }
        return false;
    }
}
