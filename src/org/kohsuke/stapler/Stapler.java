package org.kohsuke.stapler;

import net.sf.json.JSONObject;
import org.apache.commons.beanutils.ConversionException;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.ConvertUtilsBean;
import org.apache.commons.beanutils.Converter;
import org.apache.commons.beanutils.converters.DoubleConverter;
import org.apache.commons.beanutils.converters.FloatConverter;
import org.apache.commons.beanutils.converters.IntegerConverter;
import org.apache.commons.jelly.expression.ExpressionFactory;
import org.apache.commons.fileupload.FileItem;
import org.kohsuke.stapler.jelly.JellyClassLoaderTearOff;
import static org.kohsuke.stapler.Dispatcher.traceable;
import static org.kohsuke.stapler.Dispatcher.traceEval;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
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
import java.util.HashSet;
import java.util.Locale;
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

    private /*final*/ WebApp webApp;

    public void init(ServletConfig servletConfig) throws ServletException {
        super.init(servletConfig);
        this.context = servletConfig.getServletContext();
        this.webApp = WebApp.get(context);
        String defaultEncodings = servletConfig.getInitParameter("default-encodings");
        if(defaultEncodings!=null) {
            for(String t : defaultEncodings.split(";")) {
                t=t.trim();
                int idx=t.indexOf('=');
                if(idx<0)   throw new ServletException("Invalid format: "+t);
                webApp.defaultEncodingForStaticResources.put(t.substring(0,idx),t.substring(idx+1));
            }
        }
    }

    public WebApp getWebApp() {
        return webApp;
    }

    protected void service(HttpServletRequest req, HttpServletResponse rsp) throws ServletException, IOException {
    	String servletPath = getServletPath(req);
        
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
            OpenConnection con = openResourcePathByLocale(req,servletPath);
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
        invoke( req, rsp, root, servletPath);
    }

    /**
     * Tomcat and GlassFish returns a fresh {@link InputStream} every time
     * {@link URLConnection#getInputStream()} is invoked in their {@code org.apache.naming.resources.DirContextURLConnection}.
     *
     * <p>
     * All the other {@link URLConnection}s in JDK don't do this --- they return the same {@link InputStream},
     * even the one for the file:// URLs.
     *
     * <p>
     * In Tomcat (and most likely in GlassFish, although this is not verifid), resource look up on
     * {@link ServletContext#getResource(String)} successfully returns non-existent URL, and
     * the failure can be only detected by {@link IOException} from {@link URLConnection#getInputStream()}.
     *
     * <p>
     * Therefore, for the whole thing to work without resource leak, once we open {@link InputStream}
     * for the sake of really making sure that the resource exists, we need to hang on to that stream.
     *
     * <p>
     * Hence the need for this tuple.
     */
    private static final class OpenConnection {
        final URLConnection connection;
        final InputStream stream;

        private OpenConnection(URLConnection connection, InputStream stream) {
            this.connection = connection;
            this.stream = stream;
        }

        private OpenConnection(URLConnection connection) throws IOException {
            this(connection,connection.getInputStream());
        }
    }

    private OpenConnection openResourcePathByLocale(HttpServletRequest req,String resourcePath) throws IOException {
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
    OpenConnection selectResourceByLocale(URL url, Locale locale) throws IOException {
        String s = url.toString();
        int idx = s.lastIndexOf('.');
        if(idx<0)   // no file extension, so no locale switch available
            return openURL(url);
        String base = s.substring(0,idx);
        String ext = s.substring(idx);
        if(ext.indexOf('/')>=0) // the '.' we found was not an extension separator
            return openURL(url);

        OpenConnection con;

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
    boolean serveStaticResource(HttpServletRequest req, StaplerResponse rsp, OpenConnection con, long expiration) throws IOException {
        if(con==null)   return false;
        return serveStaticResource(req,rsp, con.stream,
                con.connection.getLastModified(),
                expiration,
                con.connection.getContentLength(),
                con.connection.getURL().toString());
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
    private OpenConnection openURL(URL url) throws IOException {
        if(url==null)   return null;

        // jetty reports directories    as URLs, which isn't what this is intended for,
        // so check and reject.
        File f = toFile(url);
        if(f!=null && f.isDirectory())
            return null;

        URLConnection con = url.openConnection();

        try {
            return new OpenConnection(con);
        } catch (IOException e) {
            // Tomcat only reports a missing resource error here, from URLConnection.getInputStream()
            return null;
        }
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
            if(webApp.defaultEncodingForStaticResources.containsKey(mimeType))
                mimeType += ";charset="+webApp.defaultEncodingForStaticResources.get(mimeType);
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
        RequestImpl oreq = CURRENT_REQUEST.get();
        CURRENT_REQUEST.set(sreq);

        ResponseImpl srsp = new ResponseImpl(this, rsp);
        ResponseImpl orsp = CURRENT_RESPONSE.get();
        CURRENT_RESPONSE.set(srsp);

        try {
            invoke(sreq,srsp,root);
        } finally {
            CURRENT_REQUEST.set(oreq);
            CURRENT_RESPONSE.set(orsp);
        }
    }

    void invoke(RequestImpl req, ResponseImpl rsp, Object node ) throws IOException, ServletException {
        if(traceable())
            traceEval(req,rsp,node);

        if(node instanceof StaplerProxy) {
            if(traceable())
                traceEval(req,rsp,node,"((StaplerProxy)",").getTarget()");
            Object n = ((StaplerProxy)node).getTarget();
            if(n==node || n==null) {
                // if the proxy returns itself, assume that it doesn't want to proxy.
                // if null, no one will handle the request
            } else {
                // recursion helps debugging by leaving the trace in the stack.
                invoke(req,rsp,n);
                return;
            }
        }

        // adds this node to ancestor list
        AncestorImpl a = new AncestorImpl(req.ancestors);
        a.set(node,req);

        if(node==null) {
            // node is null
            if(!Dispatcher.TRACE) {
                rsp.sendError(SC_NOT_FOUND);
            } else {
                // show error page
                rsp.setStatus(SC_NOT_FOUND);
                rsp.setContentType("text/html;charset=UTF-8");
                PrintWriter w = rsp.getWriter();
                w.println("<html><body>");
                w.println("<h1>404 Not Found</h1>");
                w.println("<p>Stapler processed this HTTP request as follows, but couldn't find the resource to consume the request");
                w.println("<pre>");
                EvaluationTrace.get(req).printHtml(w);
                w.println("<font color=red>-&gt; unexpected null!</font>");
                w.println("</pre>");
                w.println("<p>If this 404 is unexpected, double check the last part of the trace to see if it should have evaluated to null.");
                w.println("</body></html>");
            }
            return;
        }

        MetaClass metaClass = webApp.getMetaClass(node.getClass());

        if(!req.tokens.hasMore()) {
            String servletPath = getServletPath(req);
            if(!servletPath.endsWith("/")) {
                String target = req.getContextPath() + servletPath + '/';
                if(LOGGER.isLoggable(Level.FINE))
                    LOGGER.fine("Redirecting to "+target);
                rsp.sendRedirect2(target);
                return;
            }
            
            if(req.getMethod().equals("DELETE")) {
                if(node instanceof HttpDeletable) {
                    ((HttpDeletable)node).delete(req,rsp);
                    return;
                }
            }

            for (Facet f : webApp.facets) {
                if(f.handleIndexRequest(req,rsp,node,metaClass))
                    return;
            }

            URL indexHtml = getSideFileURL(node,"index.html");
            if(indexHtml!=null && serveStaticResource(req,rsp,indexHtml,0))
                return; // done
        }

        try {
            for( Dispatcher d : metaClass.dispatchers ) {
                if(d.dispatch(req,rsp,node)) {
                    if(LOGGER.isLoggable(Level.FINE))
                        LOGGER.fine("Handled by "+d);
                    return;
                }
            }
        } catch (IllegalAccessException e) {
            // this should never really happen
            getServletContext().log("Error while serving "+req.getRequestURL(),e);
            throw new ServletException(e);
        } catch (InvocationTargetException e) {
            getServletContext().log("Error while serving "+req.getRequestURL(),e);
            throw new ServletException(e.getTargetException());
        }

        if(node instanceof StaplerFallback) {
            if(traceable())
                traceEval(req,rsp,node,"((StaplerFallback)",").getStaplerFallback()");
            Object n = ((StaplerFallback)node).getStaplerFallback();
            if(n!=node && n!=null) {
                // delegate to the fallback object
                invoke(req,rsp,n);
                return;
            }
        }

        // we really run out of options.
        if(!Dispatcher.TRACE) {
            rsp.sendError(SC_NOT_FOUND);
        } else {
            // show error page
            rsp.setStatus(SC_NOT_FOUND);
            rsp.setContentType("text/html;charset=UTF-8");
            PrintWriter w = rsp.getWriter();
            w.println("<html><body>");
            w.println("<h1>404 Not Found</h1>");
            w.println("<p>Stapler processed this HTTP request as follows, but couldn't find the resource to consume the request");
            w.println("<pre>");
            EvaluationTrace.get(req).printHtml(w);
            w.printf("<font color=red>-&gt; No matching rule was found on &lt;%s&gt; for \"%s\"</font>\n",node,req.tokens.assembleRestOfPath());
            w.println("</pre>");
            w.printf("<p>&lt;%s&gt; has the following URL mappings, in the order of preference:",node);
            w.println("<ol>");
            for (Dispatcher d : metaClass.dispatchers) {
                w.println("<li>");
                w.println(d.toString());
            }
            w.println("</ol>");
            w.println("</body></html>");
        }
    }

    public void forward(RequestDispatcher dispatcher, StaplerRequest req, HttpServletResponse rsp) throws ServletException, IOException {
        dispatcher.forward(req,new ResponseImpl(this,rsp));
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
     * Sets the classloader used by {@link StaplerRequest#bindJSON(Class, JSONObject)} and its sibling methods.
     *
     * @deprecated
     *      Use {@link WebApp#setClassLoader(ClassLoader)}
     */
    public static void setClassLoader( ServletContext context, ClassLoader classLoader ) {
        WebApp.get(context).setClassLoader(classLoader);
    }

    /**
     * @deprecated
     *      Use {@link WebApp#getClassLoader()}
     */
    public static ClassLoader getClassLoader( ServletContext context ) {
        return WebApp.get(context).getClassLoader();
    }

    /**
     * @deprecated
     *      Use {@link WebApp#getClassLoader()}
     */
    public ClassLoader getClassLoader() {
        return webApp.getClassLoader();
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
     * Gets the current {@link Stapler} that the calling thread is associated with.
     */
    public static Stapler getCurrent() {
        return CURRENT_REQUEST.get().getStapler();
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

    private static ThreadLocal<RequestImpl> CURRENT_REQUEST = new ThreadLocal<RequestImpl>();
    private static ThreadLocal<ResponseImpl> CURRENT_RESPONSE = new ThreadLocal<ResponseImpl>();

    private static final Logger LOGGER = Logger.getLogger(Stapler.class.getName());

    /**
     * Extensions that look like text files.
     */
    private static final Set<String> TEXT_FILES = new HashSet<String>(Arrays.asList(
        "css","js","html","txt","java","htm","c","cpp","h","rb","pl","py","xml"
    ));

    /**
     * IBM JDK workaround, which reports incorrect getServletPath when stapler
     * is bound to '/'.
     *
     * See http://www.nabble.com/WAS---Hudson-tt16026561.html for more details
     */
    private String getServletPath(HttpServletRequest req) {
    	String servletPath = req.getServletPath();
    	String pathInfo = req.getPathInfo();
    	if (pathInfo != null)
            servletPath += pathInfo;
    	return servletPath;
    }


    /**
     * This is the {@link Converter} registry that Stapler uses, primarily
     * for form-to-JSON binding in {@link StaplerRequest#bindJSON(Class, JSONObject)}
     * and its family of methods. 
     */
    public static final ConvertUtilsBean CONVERT_UTILS = new ConvertUtilsBean();

    public static Converter lookupConverter(Class type) {
        Converter c = CONVERT_UTILS.lookup(type);
        if(c!=null) return c;
        // fall back to compatibility behavior
        return ConvertUtils.lookup(type);
    }

    static {
        CONVERT_UTILS.register(new Converter() {
            public Object convert(Class type, Object value) {
                if(value==null) return null;
                try {
                    return new URL(value.toString());
                } catch (MalformedURLException e) {
                    throw new ConversionException(e);
                }
            }
        }, URL.class);

        CONVERT_UTILS.register(new Converter() {
            public FileItem convert(Class type, Object value) {
                if(value==null) return null;
                try {
                    return Stapler.getCurrentRequest().getFileItem(value.toString());
                } catch (ServletException e) {
                    throw new ConversionException(e);
                } catch (IOException e) {
                    throw new ConversionException(e);
                }
            }
        }, FileItem.class);

        // mapping for boxed types should map null to null, instead of null to zero.
        CONVERT_UTILS.register(new IntegerConverter(null),Integer.class);
        CONVERT_UTILS.register(new FloatConverter(null),Float.class);
        CONVERT_UTILS.register(new DoubleConverter(null),Double.class);
    }
}
