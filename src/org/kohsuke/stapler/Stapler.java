package org.kohsuke.stapler;

import org.kohsuke.stapler.jelly.JellyClassTearOff;

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
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

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
        if(LOGGER.isLoggable(Level.FINE))
            LOGGER.fine("Processing request for "+req.getServletPath());

        URL url = getServletContext().getResource(req.getServletPath());
        if(url!=null && serveStaticResource(req,rsp,url, MetaClass.NO_CACHE ? 0 : 24*60*60*1000/*1 day*/))
            return; // done

        // consider reusing this ArrayList.
        invoke( req, rsp, root, req.getServletPath() );
    }

    /**
     * Serves the specified {@link URL} as a static resource.
     */
    boolean serveStaticResource(HttpServletRequest req, HttpServletResponse rsp, URL url, long expiration) throws IOException {
        // jetty reports directories as URLs, which isn't what this is intended for,
        // so check and reject.
        File f = toFile(url);
        if(f!=null && f.isDirectory())
            return false;

        if(LOGGER.isLoggable(Level.FINE))
            LOGGER.fine("Serving static resource "+f);

        URLConnection con = url.openConnection();

        InputStream in;
        try {
            in = con.getInputStream();
        } catch (IOException e) {
            // Tomcat only reports a missing resource error here
            return false;
        }

        con.connect();

        return serveStaticResource(req,rsp, in, con.getLastModified(), expiration, con.getContentLength(), url.toString());
    }

    /**
     * Serves the specified {@link InputStream} as a static resource.
     *
     * @param contentLength
     *      if the length of the input stream is known in advance, specify that value
     *      so that HTTP keep-alive works. Otherwise specify -1 to indicate that the length is unknown.
     * @param expiration
     *      The number of milliseconds until the resource will "expire".
     *      Until it expires the browser will be allowed to cache it
     *      and serve it without checking back with the server.
     *      After it expires, the client will send conditional GET to
     *      check if the resource is actually modified or not.
     *      If 0, it will immediately expire.
     * @param fileName
     *      file name of this resource. Used to determine the MIME type.
     *      Since the only important portion is the file extension, this could be just a file name,
     *      or a full path name, or even a pseudo file name that doesn't actually exist.
     *      It supports both '/' and '\\' as the path separator.
     * @return false
     *      if the resource doesn't exist.
     */
    boolean serveStaticResource(HttpServletRequest req, HttpServletResponse rsp, InputStream in, long lastModified, long expiration, int contentLength, String fileName) throws IOException {
        try {
            {// send out Last-Modified, or check If-Modified-Since
                if(lastModified!=0) {
                    String since = req.getHeader("If-Modified-Since");
                    SimpleDateFormat format = HTTP_DATE_FORMAT.get();
                    if(since!=null) {
                        try {
                            long ims = format.parse(since).getTime();
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

                    String lastModifiedStr = format.format(new Date(lastModified));
                    rsp.setHeader("Last-Modified", lastModifiedStr);
                    if(expiration<=0)
                        rsp.setHeader("Expires",lastModifiedStr);
                    else
                        rsp.setHeader("Expires",format.format(new Date(new Date().getTime()+expiration)));
                }
            }


            if(contentLength!=-1)
            rsp.setContentLength(contentLength);

            int idx = fileName.lastIndexOf('/');
            fileName = fileName.substring(idx+1);
            idx = fileName.lastIndexOf('\\');
            fileName = fileName.substring(idx+1);
            String mimeType = getServletContext().getMimeType(fileName);
            if(mimeType==null)  mimeType="application/octet-stream";
            rsp.setContentType(mimeType);

            byte[] buf = new byte[1024];
            int len;
            while((len=in.read(buf))>0)
                rsp.getOutputStream().write(buf,0,len);
            return true;
        } finally {
            in.close();
        }
    }

    /**
     * If the URL is "file://", return its file representation.
     */
    private File toFile(URL url) {
        String urlstr = url.toExternalForm();
        if(!urlstr.startsWith("file:"))
            return null;
        try {
            //when URL contains escapes like %20, this does the conversion correctly
            return new File(url.toURI().getPath());
        } catch (URISyntaxException e) {
            try {
                // some containers, such as Winstone, doesn't escape ' ', and for those
                // we need to do this. This method doesn't fail when urlstr contains '%20',
                // so toURI() has to be tried first.
                return new File(new URI(null,urlstr,null).getPath());
            } catch (URISyntaxException _) {
                // the whole thing could fail anyway.
                return null;
            }
        }
    }


    void invoke(HttpServletRequest req, HttpServletResponse rsp, Object root, String url) throws IOException, ServletException {
        RequestImpl sreq = new RequestImpl(this, req, new ArrayList<AncestorImpl>(), new TokenList(url));
        StaplerRequest oreq = CURRENT_REQUEST.get();
        CURRENT_REQUEST.set(sreq);

        ResponseImpl srsp = new ResponseImpl(this, rsp);
        StaplerResponse orsp = CURRENT_RESPONSE.get();
        CURRENT_RESPONSE.set(srsp);

        try {
            invoke(sreq,srsp,root);
        } finally {
            CURRENT_REQUEST.set(oreq);
            CURRENT_RESPONSE.set(orsp);
        }
    }

    void invoke(RequestImpl req, ResponseImpl rsp, Object node ) throws IOException, ServletException {
        while(node instanceof StaplerProxy) {
            if(LOGGER.isLoggable(Level.FINE))
                LOGGER.fine("Invoking StaplerProxy.getTarget() on "+node);
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
                String target = req.getContextPath() + req.getServletPath() + '/';
                if(LOGGER.isLoggable(Level.FINE))
                    LOGGER.fine("Redirecting to "+target);
                rsp.sendRedirect2(target);
                return;
            }
            // TODO: find the list of welcome pages for this class by reading web.xml
            RequestDispatcher indexJsp = getResourceDispatcher(node,"index.jsp");
            if(indexJsp!=null) {
                if(LOGGER.isLoggable(Level.FINE))
                    LOGGER.fine("Invoking index.jsp on "+node);
                forward(indexJsp,req,rsp);
                return;
            }

            try {
                if(metaClass.loadTearOff(JellyClassTearOff.class).serveIndexJelly(req,rsp,node))
                    return;
            } catch (LinkageError e) {
                // jelly is not present.
                if(!jellyLinkageErrorReported) {
                    jellyLinkageErrorReported = true;
                    getServletContext().log("Jelly not present. Skipped",e);
                }
            }

            URL indexHtml = getSideFileURL(node,"index.html");
            if(indexHtml!=null && serveStaticResource(req,rsp,indexHtml,0))
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
     * Gets the current {@link StaplerRequest} that the calling thread is associated with.
     */
    public static StaplerRequest getCurrentRequest() {
        return CURRENT_REQUEST.get();
    }

    /**
     * Gets the current {@link StaplerResponse} that the calling thread is associated with.
     */
    public static StaplerResponse getCurrentResponse() {
        return CURRENT_RESPONSE.get();
    }

    /**
     * HTTP date format. Notice that {@link SimpleDateFormat} is thread unsafe.
     */
    static final ThreadLocal<SimpleDateFormat> HTTP_DATE_FORMAT =
        new ThreadLocal<SimpleDateFormat>() {
            protected SimpleDateFormat initialValue() {
                return new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);
            }
        };

    private static ThreadLocal<StaplerRequest> CURRENT_REQUEST = new ThreadLocal<StaplerRequest>();
    private static ThreadLocal<StaplerResponse> CURRENT_RESPONSE = new ThreadLocal<StaplerResponse>();

    private static boolean jellyLinkageErrorReported;

    private static final Logger LOGGER = Logger.getLogger(Stapler.class.getName());
}
