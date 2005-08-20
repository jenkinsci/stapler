package org.kohsuke.stapler;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Maps an HTTP request to a method call / JSP invocation against a model object
 * by evaluating the request URL in a EL-ish way.
 *
 * <p>
 * This servlet should be used as the default servlet.
 *
 * @author Kohsuke Kawaguchi
 */
public class Stapler extends HttpServlet {

    /**
     * Root of the model object.
     */
    private /*final*/ Object root;

    public void init(ServletConfig servletConfig) throws ServletException {
        super.init(servletConfig);
        root = servletConfig.getServletContext().getAttribute("app");
        if(root==null)
            throw new ServletException("there's no \"app\" attribute in the application context.");
    }

    protected void service(HttpServletRequest req, HttpServletResponse rsp) throws ServletException, IOException {
        // TODO: test
        URL url = getServletContext().getResource(req.getServletPath());
        if(url!=null && serveStaticResource(req,rsp,url))
            return; // done

        // consider reusing this ArrayList.
        invoke( req, rsp, root, req.getServletPath() );
    }

    /**
     * Serves the specified URL as a static resource.
     *
     * @return false
     *      if the resource doesn't exist.
     */
    private boolean serveStaticResource(HttpServletRequest req, HttpServletResponse rsp, URL url) throws IOException {
        URLConnection con = url.openConnection();
        InputStream in = null;
        try {
            in = con.getInputStream();
        } catch (IOException e) {
            // Tomcat only reports a missing resource error here
            return false;
        }

        con.connect();
        rsp.setContentType(getServletContext().getMimeType(req.getServletPath()));
        if(con.getContentLength()!=-1)
            rsp.setContentLength(con.getContentLength());

        byte[] buf = new byte[1024];
        int len;
        while((len=in.read(buf))>0)
            rsp.getOutputStream().write(buf,0,len);
        in.close();
        return true;
    }

    private static String[] tokenize(String url) {
        StringTokenizer tknzr = new StringTokenizer(url,"/");
        String[] tokens = new String[tknzr.countTokens()];
        int i=0;
        while(tknzr.hasMoreTokens())
            tokens[i++] = tknzr.nextToken();
        return tokens;
    }

    void invoke(HttpServletRequest req, HttpServletResponse rsp, Object root, String url) throws IOException, ServletException {
        invoke(req,rsp,root,new ArrayList(),tokenize(url),0);
    }

    private void invoke(HttpServletRequest req, HttpServletResponse rsp, Object node, List ancestors, String[] tokens, int idx ) throws IOException, ServletException {
        AncestorImpl a = new AncestorImpl(ancestors);
        a.set(node,tokens,idx,req);

        if(node==null) {
            rsp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        if(idx==tokens.length) {
            if(!req.getServletPath().endsWith("/")) {
                rsp.sendRedirect(req.getContextPath()+req.getServletPath()+'/');
                return;
            }
            // TODO: find the list of welcome pages for this class by reading web.xml
            RequestDispatcher indexJsp = getResourceDispatcher(node,"index.jsp");
            if(indexJsp==null) {
                URL indexHtml = getSideFileURL(node,"index.html");
                if(indexHtml!=null && serveStaticResource(req,rsp,indexHtml))
                    return; // done
                rsp.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
            forward(indexJsp,new RequestImpl(this,req,ancestors,tokens,idx),rsp);
            return;
        }
        final String next = tokens[idx++];
        final String arg = (tokens.length==idx)?null:tokens[idx];


        // if the token includes '.', try static resources
        if(next.indexOf('.')!=-1 && arg==null) {
            URL res = getSideFileURL(node,next);
            if(res!=null && serveStaticResource(req,rsp,res))
                return; // done
            // otherwise continue
        }


        // check a public property first <obj>.<token>
        try {
            Field field = node.getClass().getField(next);
            invoke(req,rsp,field.get(node),ancestors,tokens,idx);
            return;
        } catch (NoSuchFieldException e) {
            // fall through
        } catch (IllegalAccessException e) {
            // since we're only looking for public fields, this shall never happen
            getServletContext().log(e.getMessage(),e);
            // fall through
        }

        String methodName = getMethodName(next);


        // check a public getter <obj>.get<Token>()
        try {
            Method method = node.getClass().getMethod("get"+methodName,emptyArgs);
            invoke(req,rsp,method.invoke(node,emptyArgs),ancestors,tokens,idx);
            return;
        } catch (NoSuchMethodException e) {
            // fall through
        } catch (IllegalAccessException e) {
            // since we're only looking for public methods, this shall never happen
            getServletContext().log("Error while serving "+req.getRequestURL(),e);
            // fall through
        } catch (InvocationTargetException e) {
            getServletContext().log("Error while serving "+req.getRequestURL(),e);
            rsp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }


        // check a public getter <obj>.get<Token>(request)
        try {
            Method method = node.getClass().getMethod("get"+methodName,requestArgs);
            invoke(req,rsp,method.invoke(node,new Object[]{new RequestImpl(this,req,ancestors,tokens,idx)}),ancestors,tokens,idx);
            return;
        } catch (NoSuchMethodException e) {
            // fall through
        } catch (IllegalAccessException e) {
            // since we're only looking for public methods, this shall never happen
            getServletContext().log("Error while serving "+req.getRequestURL(),e);
            // fall through
        } catch (InvocationTargetException e) {
            getServletContext().log("Error while serving "+req.getRequestURL(),e);
            rsp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }


        // check a public selector <obj>.get<Token>(<arg>)
        if(arg!=null) {
            try {
                Method method = node.getClass().getMethod("get"+methodName,selectorArgs);
                invoke(req,rsp,method.invoke(node,new Object[]{arg}),ancestors,tokens,idx+1);
                return;
            } catch (NoSuchMethodException e) {
                // fall through
            } catch (IllegalAccessException e) {
                // since we're only looking for public methods, this shall never happen
                getServletContext().log("Error while serving "+req.getRequestURL(),e);
                // fall through
            } catch (InvocationTargetException e) {
                getServletContext().log("Error while serving "+req.getRequestURL(),e);
                rsp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                return;
            }

            try {
                Method method = node.getClass().getMethod("get"+methodName,int_selectorArgs);
                invoke(req,rsp,method.invoke(node,new Object[]{Integer.valueOf(arg)}),ancestors,tokens,idx+1);
                return;
            } catch (NoSuchMethodException e) {
                // fall through
            } catch (IllegalAccessException e) {
                // since we're only looking for public methods, this shall never happen
                getServletContext().log("Error while serving "+req.getRequestURL(),e);
                // fall through
            } catch (InvocationTargetException e) {
                getServletContext().log("Error while serving "+req.getRequestURL(),e);
                rsp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                return;
            }
        }

        // check action <obj>.do<token>()
        try {
            Method method = node.getClass().getMethod("do"+methodName,actionArgs);
            method.invoke(node,new Object[]{
                new RequestImpl(this,req,ancestors,tokens,idx),
                new ResponseImpl(this,rsp)
            });
            return;
        } catch (NoSuchMethodException e) {
            // fall through
        } catch (IllegalAccessException e) {
            // since we're only looking for public methods, this shall never happen
            getServletContext().log("Error while serving "+req.getRequestURL(),e);
            // fall through
        } catch (InvocationTargetException e) {
            getServletContext().log("Error while serving "+req.getRequestURL(),e);
            rsp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        if( node.getClass().isArray() ) {
            try {
                int i = Integer.parseInt(next);
                invoke(req,rsp,((Object[])node)[i],ancestors,tokens,idx);
                return;
            } catch (NumberFormatException e) {
                // fall through
            }
        }
        if( node instanceof List ) {
            try {
                int i = Integer.parseInt(next);
                invoke(req,rsp,((List)node).get(i),ancestors,tokens,idx);
                return;
            } catch (NumberFormatException e) {
                // fall through
            }
        }
        if( node instanceof Map ) {
            Object item = ((Map)node).get(next);
            if(item!=null) {
                invoke(req,rsp,item,ancestors,tokens,idx);
                return;
            }
            // otherwise just fall through
        }

        // check JSP views
        // I thought about generalizing this to invoke other resources (such as static images, etc)
        // but I realized that those would require a very different handling.
        // so for now we just assume it's a JSP
        RequestDispatcher disp = getResourceDispatcher(node,next+".jsp");
        if(disp!=null) {
            forward(disp,new RequestImpl(this,req,ancestors,tokens,idx),rsp);
            return;
        }

        // TODO: check if we can route to static resources
        // which directory shall we look up a resource from?

        // we really run out of options.
        rsp.sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    private void forward(RequestDispatcher dispatcher, StaplerRequest req, HttpServletResponse rsp) throws ServletException, IOException {
        dispatcher.forward(req,new ResponseImpl(this,rsp));
    }

    RequestDispatcher getResourceDispatcher(Object node, String fileName) throws MalformedURLException {
        for( Class c = node.getClass(); c!=Object.class; c=c.getSuperclass() ) {
            String name = "/WEB-INF/side-files/"+c.getName().replace('.','/')+'/'+fileName;
            if(getServletContext().getResource(name)!=null) {
                // Tomcat returns a RequestDispatcher even if the JSP file doesn't exist.
                // so check if the resource exists first.
                RequestDispatcher disp = getServletContext().getRequestDispatcher(name);
                if(disp!=null) {
                    return new RequestDispatcherWrapper(disp,node);
                }
            }
        }
        return null;
    }

    private URL getSideFileURL(Object node,String fileName) throws MalformedURLException {
        for( Class c = node.getClass(); c!=Object.class; c=c.getSuperclass() ) {
            String name = "/WEB-INF/side-files/"+c.getName().replace('.','/')+'/'+fileName;
            URL url = getServletContext().getResource(name);
            if(url!=null) return url;
        }
        return null;
    }

    private static String getMethodName(String name) {
        return Character.toUpperCase(name.charAt(0))+name.substring(1);
    }


    /**
     * Gets the URL (e.g., "/WEB-INF/side-files/fully/qualified/class/name/jspName")
     * from a class and the JSP name.
     */
    public static String getViewURL(Class clazz,String jspName) {
        return "/WEB-INF/side-files/"+clazz.getName().replace('.','/')+'/'+jspName;
    }

    private static final Class[] emptyArgs = new Class[0];
    private static final Class[] requestArgs = new Class[] {
        StaplerRequest.class
    };
    private static final Class[] selectorArgs = new Class[] {
        String.class
    };
    private static final Class[] int_selectorArgs = new Class[] {
        int.class
    };

    private static final Class[] actionArgs = new Class[]{
        StaplerRequest.class,
        StaplerResponse.class
    };


    /**
     * Sets the specified object as the root of the web application.
     *
     * <p>
     * This method should be invoked from your implementation of
     * {@link ServletContextListener#contextInitialized(ServletContextEvent)}.
     *
     * <p>
     * This is just a convenience method to invoke
     * <code>servletContext.setAttribute("app",rootApp)</code>.
     *
     * <p>
     * The root object is bound to the URL '/' and used to resolve
     * all the requests to this web application.
     */
    public static void setRoot( ServletContextEvent event, Object rootApp ) {
        event.getServletContext().setAttribute("app",rootApp);
    }
}
