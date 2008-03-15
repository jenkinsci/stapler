package org.kohsuke.stapler;

import org.apache.commons.jelly.expression.ExpressionFactory;
import org.kohsuke.stapler.jelly.JellyClassLoaderTearOff;
import org.kohsuke.stapler.jelly.JellyClassTearOff;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
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

    private /*final*/ ServletContext context;

    /**
     * Duck-type wrappers for the given class.
     */
    private Map<Class,Class[]> wrappers;

    public void init(ServletConfig servletConfig) throws ServletException {
        super.init(servletConfig);
        this.context = servletConfig.getServletContext();
        wrappers = (Map<Class,Class[]>) servletConfig.getServletContext().getAttribute("wrappers");
        if(wrappers==null)
            wrappers = new HashMap<Class,Class[]>();
    }

    protected void service(HttpServletRequest req, HttpServletResponse rsp) throws ServletException, IOException {
        String servletPath = req.getServletPath();

        if(LOGGER.isLoggable(Level.FINE))
            LOGGER.fine("Processing request for "+servletPath);

        boolean staticLink = false;

        if(servletPath.startsWith("/static/")) {
            // skip "/static/..../ portion
            int idx = servletPath.indexOf('/',8);
            servletPath=servletPath.substring(idx);
            staticLink = true;
        }

        if(servletPath.length()!=0) {
            // getResource requires '/' prefix (and resin insists on that, too) but servletPath can be empty string (hudson #879)
            URLConnection con = openResourcePathByLocale(req,servletPath);
            if(con!=null) {
                long expires = MetaClass.NO_CACHE ? 0 : 24L * 60 * 60 * 1000; /*1 day*/
                if(staticLink)
                    expires*=365;   // static resources are unique, so we can set a long expiration date
                if(serveStaticResource(req, new ResponseImpl(this, rsp), con, expires))
                    return; // done
            }
        }

        Object root = context.getAttribute("app");
        if(root==null)
            throw new ServletException("there's no \"app\" attribute in the application context.");

        // consider reusing this ArrayList.
        invoke( req, rsp, root, req.getServletPath());
    }

    private URLConnection openResourcePathByLocale(HttpServletRequest req,String resourcePath) throws IOException {
        URL url = getServletContext().getResource(resourcePath);
        if(url==null)   return null;
        return selectResourceByLocale(url,req.getLocale());
    }

    /**
     * Basically works like {@link URL#openConnection()} but it uses the
     * locale specific resource if available, by using the given locale.
     *
     * <p>
     * The syntax of the locale specific resource is the same as property file localization.
     * So Japanese resource for <tt>foo.html</tt> would be named <tt>foo_ja.html</tt>.
     */
    URLConnection selectResourceByLocale(URL url, Locale locale) throws IOException {
        String s = url.toString();
        int idx = s.lastIndexOf('.');
        if(idx<0)   // no file extension, so no locale switch available
            return openURL(url);
        String base = s.substring(0,idx);
        String ext = s.substring(idx);
        if(ext.indexOf('/')>=0) // the '.' we found was not an extension separator
            return openURL(url);

        URLConnection con;

        // try locale specific resources first.
        con = openURL(new URL(base+'_'+ locale.getLanguage()+'_'+ locale.getCountry()+'_'+ locale.getVariant()+ext));
        if(con!=null)   return con;
        con = openURL(new URL(base+'_'+ locale.getLanguage()+'_'+ locale.getCountry()+ext));
        if(con!=null)   return con;
        con = openURL(new URL(base+'_'+ locale.getLanguage()+ext));
        if(con!=null)   return con;
        // default
        return openURL(url);
    }

    /**
     * Serves the specified {@link URLConnection} as a static resource.
     */
    boolean serveStaticResource(HttpServletRequest req, StaplerResponse rsp, URLConnection con, long expiration) throws IOException {
        if(con==null)   return false;
        return serveStaticResource(req,rsp, con.getInputStream(),
                con.getLastModified(), expiration, con.getContentLength(), con.getURL().toString());
    }

    /**
     * Serves the specified {@link URL} as a static resource.
     */
    boolean serveStaticResource(HttpServletRequest req, StaplerResponse rsp, URL url, long expiration) throws IOException {
        return serveStaticResource(req,rsp,openURL(url),expiration);
    }
    
    /**
     * Opens URL, with error handling to absorb container differences.
     */
    private URLConnection openURL(URL url) throws IOException {
        if(url==null)   return null;

        // jetty reports directories as URLs, which isn't what this is intended for,
        // so check and reject.
        File f = toFile(url);
        if(f!=null && f.isDirectory())
            return null;

        URLConnection con = url.openConnection();

        try {
            con.getInputStream();
        } catch (IOException e) {
            // Tomcat only reports a missing resource error here
            return null;
        }

        return con;
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
    boolean serveStaticResource(HttpServletRequest req, StaplerResponse rsp, InputStream in, long lastModified, long expiration, int contentLength, String fileName) throws IOException {
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


            int idx = fileName.lastIndexOf('/');
            fileName = fileName.substring(idx+1);
            idx = fileName.lastIndexOf('\\');
            fileName = fileName.substring(idx+1);
            String mimeType = getServletContext().getMimeType(fileName);
            if(mimeType==null)  mimeType="application/octet-stream";
            rsp.setContentType(mimeType);

            idx = fileName.lastIndexOf('.');
            String ext = fileName.substring(idx+1);

            OutputStream out;
            if(mimeType.startsWith("text/") || TEXT_FILES.contains(ext))
                // with gzip compression, Content-Length header needs to indicate the # of bytes after compression,
                // so we can't compute it upfront.
                out = rsp.getCompressedOutputStream(req);
            else {
                if(contentLength!=-1)
                    rsp.setContentLength(contentLength);
                out = rsp.getOutputStream();
            }

            byte[] buf = new byte[1024];
            int len;
            while((len=in.read(buf))>0)
                out.write(buf,0,len);
            out.close();
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

    /**
     * Performs stapler processing on the given root object and request URL.
     */
    public void invoke(HttpServletRequest req, HttpServletResponse rsp, Object root, String url) throws IOException, ServletException {
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
     * Sets the Jelly {@link ExpressionFactory} to be used to parse views.
     *
     * <p>
     * This method should be invoked from your implementation of
     * {@link ServletContextListener#contextInitialized(ServletContextEvent)}.
     *
     * <p>
     * Once views are parsed, they won't be re-parsed just because you called
     * this method to override the expression factory.
     *
     * <p>
     * The primary use case of this feature is to customize the behavior
     * of JEXL evaluation. 
     */
    public static void setExpressionFactory( ServletContextEvent event, ExpressionFactory factory ) {
        JellyClassLoaderTearOff.EXPRESSION_FACTORY = factory;
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
                // RFC1945 section 3.3 Date/Time Formats states that timezones must be in GMT
                SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);
                format.setTimeZone(TimeZone.getTimeZone("GMT"));
                return format;
            }
        };

    private static ThreadLocal<StaplerRequest> CURRENT_REQUEST = new ThreadLocal<StaplerRequest>();
    private static ThreadLocal<StaplerResponse> CURRENT_RESPONSE = new ThreadLocal<StaplerResponse>();

    private static boolean jellyLinkageErrorReported;

    private static final Logger LOGGER = Logger.getLogger(Stapler.class.getName());

    /**
     * Extensions that look like text files.
     */
    private static final Set<String> TEXT_FILES = new HashSet<String>(Arrays.asList(
        "css","js","html","txt","java","htm","c","cpp","h","rb","pl","py","xml"
    ));
}
