package org.kohsuke.stapler;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.net.URL;
import java.net.URLConnection;
import java.net.MalformedURLException;

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
        invoke( req, rsp, root, new ArrayList(), tokenizeRequestURL(req), 0 );
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

    private static String[] tokenizeRequestURL(HttpServletRequest req) {
        StringTokenizer tknzr = new StringTokenizer(req.getServletPath(),"/");
        String[] tokens = new String[tknzr.countTokens()];
        int i=0;
        while(tknzr.hasMoreTokens())
            tokens[i++] = tknzr.nextToken();
        return tokens;
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
                rsp.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            forward(node, indexJsp, ancestors, req, rsp);
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
            invoke(req,rsp,method.invoke(node,new Object[]{new RequestProxy(req,tokens,idx)}),ancestors,tokens,idx);
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
        }

        // check action <obj>.do<token>()
        try {
            Method method = node.getClass().getMethod("do"+methodName,actionArgs);
            method.invoke(node,new Object[]{
                new RequestProxy(req,tokens,idx), rsp
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
            forward(node,disp,ancestors,new RequestProxy(req,tokens,idx),rsp);
            return;
        }

        // TODO: check if we can route to static resources
        // which directory shall we look up a resource from?

        // we really run out of options.
        rsp.sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    private void forward(Object node, RequestDispatcher dispatcher, List ancestors, HttpServletRequest req, HttpServletResponse rsp) throws ServletException, IOException {
        req.setAttribute("it",node);
        req.setAttribute("ancestors",ancestors);
        dispatcher.forward(req,rsp);
    }

    private RequestDispatcher getResourceDispatcher(Object node, String fileName) throws MalformedURLException {
        for( Class c = node.getClass(); c!=Object.class; c=c.getSuperclass() ) {
            String name = "/WEB-INF/side-files/"+c.getName().replace('.','/')+'/'+fileName;
            if(getServletContext().getResource(name)!=null) {
                // Tomcat returns a RequestDispatcher even if the JSP file doesn't exist.
                // so check if the resource exists first.
                RequestDispatcher disp = getServletContext().getRequestDispatcher(name);
                if(disp!=null)  return disp;
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



    private class AncestorImpl implements Ancestor {
        private final List owner;
        private final int listIndex;

        private Object object;
        private String[] tokens;
        private int index;
        private String contextPath;

        public AncestorImpl(List owner) {
            this.owner = owner;
            listIndex = owner.size();
            owner.add(this);
        }

        public void set(Object object, String[] tokens, int index, HttpServletRequest req ) {
            this.object = object;
            this.tokens = tokens;
            this.index = index;
            this.contextPath = req.getContextPath();
        }

        public Object getObject() {
            return object;
        }

        public String getUrl() {
            StringBuffer buf = new StringBuffer(contextPath);
            for( int i=0; i<index; i++ ) {
                buf.append('/');
                buf.append(tokens[i]);
            }
            return buf.toString();
        }

        public Ancestor getPrev() {
            if(listIndex==0)
                return null;
            else
                return (Ancestor)owner.get(listIndex-1);
        }

        public Ancestor getNext() {
            if(listIndex==owner.size()-1)
                return null;
            else
                return (Ancestor)owner.get(listIndex+1);
        }

        public String toString() {
            return object.toString();
        }
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

    private static final Class[] actionArgs = new Class[]{
        StaplerRequest.class,
        HttpServletResponse.class
    };


    class RequestProxy extends HttpServletRequestWrapper implements StaplerRequest {
        private final String[] tokens;
        private final int idx;

        private String rest;

        public RequestProxy(HttpServletRequest httpServletRequest, String[] tokens, int idx) {
            super(httpServletRequest);
            this.tokens = tokens;
            this.idx = idx;
        }

        public String getRestOfPath() {
            if(rest==null)
                rest = assembleRestOfPath(tokens,idx);
            return rest;
        }

        public ServletContext getServletContext() {
            return Stapler.this.getServletContext();
        }

        private String assembleRestOfPath(String[] tokens,int idx) {
            StringBuffer buf = new StringBuffer();
            for( ; idx<tokens.length; idx++ ) {
                buf.append('/');
                buf.append(tokens[idx]);
            }
            return buf.toString();
        }

        public RequestDispatcher getView(Object it,String jspName) throws IOException {
            return getResourceDispatcher(it,jspName);
        }

        public String getRootPath() {
            StringBuffer buf = super.getRequestURL();
            int idx = 0;
            for( int i=0; i<3; i++ )
                idx = buf.indexOf("/",idx)+1;
            buf.setLength(idx-1);
            buf.append(super.getContextPath());
            return buf.toString();
        }
    }

}
