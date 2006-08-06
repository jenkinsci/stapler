package org.kohsuke.stapler;

import org.apache.commons.jelly.Script;
import org.apache.commons.jelly.JellyException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.WeakHashMap;

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

    /**
     * Duck-type wrappers for the given class.
     */
    private Map<Class,Class[]> wrappers;

    /**
     * All {@link Dispatcher}s.
     */
    private static final Map<Class,List<Dispatcher>> dispatchers
        = Collections.synchronizedMap(new WeakHashMap<Class, List<Dispatcher>>());

    public void init(ServletConfig servletConfig) throws ServletException {
        super.init(servletConfig);
        root = servletConfig.getServletContext().getAttribute("app");
        if(root==null)
            throw new ServletException("there's no \"app\" attribute in the application context.");
        wrappers = (Map<Class,Class[]>) servletConfig.getServletContext().getAttribute("wrappers");
        if(wrappers==null)
            wrappers = new HashMap<Class,Class[]>();
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
        // jetty reports directories as URLs, which isn't what this is intended for,
        // so check and reject.
        File f = toFile(url);
        if(f!=null && f.isDirectory())
            return false;

        URLConnection con = url.openConnection();
        InputStream in;
        try {
            in = con.getInputStream();
        } catch (IOException e) {
            // Tomcat only reports a missing resource error here
            return false;
        }

        con.connect();
        {// send out Last-Modified, or check If-Modified-Since
            long lastModified = con.getLastModified();
            if(lastModified!=0) {
                String since = req.getHeader("If-Modified-Since");
                if(since!=null) {
                    try {
                        long ims = HTTP_DATE_FORMAT.parse(since).getTime();
                        if(lastModified<ims+1000) {
                            // +1000 because date header is second-precision and Java has milli-second precision
                            rsp.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
                            return true;
                        }
                    } catch (ParseException e) {
                        // just ignore and serve the content
                    } catch (NumberFormatException e) {
                        // trying to locate a bug with Jetty
                        getServletContext().log("Error parsing ["+since+"]",e);
                        throw e;
                    }
                }
                rsp.setHeader("Last-Modified",HTTP_DATE_FORMAT.format(new Date(lastModified)));
            }
        }


        if(con.getContentLength()!=-1)
            rsp.setContentLength(con.getContentLength());
        rsp.setContentType(getServletContext().getMimeType(req.getServletPath()));

        byte[] buf = new byte[1024];
        int len;
        while((len=in.read(buf))>0)
            rsp.getOutputStream().write(buf,0,len);
        in.close();
        return true;
    }

    /**
     * If the URL is "file://", return its file representation.
     */
    private File toFile(URL url) {
        String urlstr = url.toExternalForm();
        if(!urlstr.startsWith("file:"))
            return null;
        try {
            return new File(new URI(urlstr).getPath());
        } catch (URISyntaxException e) {
            return null;
        }
    }


    void invoke(HttpServletRequest req, HttpServletResponse rsp, Object root, String url) throws IOException, ServletException {
        invoke(
            new RequestImpl(this,req,new ArrayList<AncestorImpl>(),new TokenList(url)),
            new ResponseImpl(this,rsp),
            root );
    }

    void invoke(RequestImpl req, ResponseImpl rsp, Object node ) throws IOException, ServletException {
        while(node instanceof StaplerProxy) {
            Object n = ((StaplerProxy)node).getTarget();
            if(n==node)
                break;  // if the proxy returns itself, assume that it doesn't want to proxy
            node = n;
        }

        // adds this node to ancestor list
        AncestorImpl a = new AncestorImpl(req.ancestors);
        a.set(node,req);

        if(node==null) {
            rsp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        MetaClass metaClass = MetaClass.get(node.getClass());

        if(!req.tokens.hasMore()) {
            if(!req.getServletPath().endsWith("/")) {
                rsp.sendRedirect2(req.getContextPath()+req.getServletPath()+'/');
                return;
            }
            // TODO: find the list of welcome pages for this class by reading web.xml
            RequestDispatcher indexJsp = getResourceDispatcher(node,"index.jsp");
            if(indexJsp!=null) {
                forward(indexJsp,req,rsp);
                return;
            }

            try {
                Script script = metaClass.findScript("index.jelly");
                if(script!=null) {
                    metaClass.invokeScript(req,rsp,script,node);
                    return;
                }
            } catch (JellyException e) {
                throw new ServletException(e);
            }

            URL indexHtml = getSideFileURL(node,"index.html");
            if(indexHtml!=null && serveStaticResource(req,rsp,indexHtml))
                return; // done
        }

        try {
            for( Dispatcher d : metaClass.dispatchers ) {
                if(d.dispatch(req,rsp,node))
                    return;
            }
        } catch (IllegalAccessException e) {
            // this should never really happen
            getServletContext().log("Error while serving "+req.getRequestURL(),e);
            throw new ServletException(e);
        } catch (InvocationTargetException e) {
            getServletContext().log("Error while serving "+req.getRequestURL(),e);
            throw new ServletException(e.getTargetException());
        }

        // we really run out of options.
        rsp.sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    void forward(RequestDispatcher dispatcher, StaplerRequest req, HttpServletResponse rsp) throws ServletException, IOException {
        dispatcher.forward(req,new ResponseImpl(this,rsp));
    }

    RequestDispatcher getResourceDispatcher(Object node, String fileName) throws MalformedURLException {
        for( Class c = node.getClass(); c!=Object.class; c=c.getSuperclass() ) {
            String name = "/WEB-INF/side-files/"+c.getName().replace('.','/').replace('$','/')+'/'+fileName;
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



    /**
     * Gets the URL (e.g., "/WEB-INF/side-files/fully/qualified/class/name/jspName")
     * from a class and the JSP name.
     */
    public static String getViewURL(Class clazz,String jspName) {
        return "/WEB-INF/side-files/"+clazz.getName().replace('.','/')+'/'+jspName;
    }

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



    /**
     * HTTP date format.
     */
    static final SimpleDateFormat HTTP_DATE_FORMAT =
        new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);
}
