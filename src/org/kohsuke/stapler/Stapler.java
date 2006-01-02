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
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Collections;
import java.util.WeakHashMap;
import java.util.HashMap;

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
        URLConnection con = url.openConnection();
        InputStream in;
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

    private List<Dispatcher> getDispatchers( Class node ) {
        List<Dispatcher> r = dispatchers.get(node);
        if(r!=null)
            return r;

        synchronized(dispatchers) {
            r = dispatchers.get(node);
            if(r!=null)     return r;

            // TODO: duck typing wrappers
            r = new ArrayList<Dispatcher>();
            buildDispatchers(new ClassDescriptor(node,wrappers.get(node)),r);
            dispatchers.put(node,r);
            return r;
        }
    }

    void invoke(HttpServletRequest req, HttpServletResponse rsp, Object root, String url) throws IOException, ServletException {
        invoke(
            new RequestImpl(this,req,new ArrayList<AncestorImpl>(),new TokenList(url)),
            new ResponseImpl(this,rsp),
            root );
    }

    private void invoke(RequestImpl req, ResponseImpl rsp, Object node ) throws IOException, ServletException {
        // adds this node to ancestor list
        AncestorImpl a = new AncestorImpl(req.ancestors);
        a.set(node,req);

        if(node==null) {
            rsp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        if(!req.tokens.hasMore()) {
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
            forward(indexJsp,req,rsp);
            return;
        }

        try {
            for( Dispatcher d : getDispatchers(node.getClass()) ) {
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

    private void forward(RequestDispatcher dispatcher, StaplerRequest req, HttpServletResponse rsp) throws ServletException, IOException {
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

    private static String camelize(String name) {
        return Character.toLowerCase(name.charAt(0))+name.substring(1);
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

    private interface Dispatcher {
        boolean dispatch(RequestImpl req, ResponseImpl rsp, Object node)
            throws IOException, ServletException, IllegalAccessException, InvocationTargetException;
    }

    private abstract class NameBasedDispatcher implements Dispatcher {
        private final String name;
        private final int argCount;

        protected NameBasedDispatcher(String name, int argCount) {
            this.name = name;
            this.argCount = argCount;
        }

        protected NameBasedDispatcher(String name) {
            this(name,0);
        }

        public final boolean dispatch(RequestImpl req, ResponseImpl rsp, Object node)
            throws IOException, ServletException, IllegalAccessException, InvocationTargetException {
            if(!req.tokens.peek().equals(name))
                return false;
            if(req.tokens.countRemainingTokens()<=argCount)
                return false;
            req.tokens.next();
            doDispatch(req,rsp,node);
            return true;
        }

        protected abstract void doDispatch(RequestImpl req, ResponseImpl rsp, Object node)
            throws IOException, ServletException, IllegalAccessException, InvocationTargetException;
    }


    private void buildDispatchers( ClassDescriptor node, List<Dispatcher> dispatchers ) {
        // check public properties of the form NODE.TOKEN
        for (final Field f : node.fields) {
            dispatchers.add(new NameBasedDispatcher(f.getName()) {
                public void doDispatch(RequestImpl req, ResponseImpl rsp, Object node) throws IOException, ServletException, IllegalAccessException {
                    invoke(req, rsp, f.get(node));
                }
            });
        }

        FunctionList getMethods = node.methods.prefix("get");

        // check public selector methods of the form NODE.getTOKEN()
        for( final Function f : getMethods.signature() ) {
            String name = camelize(f.getName().substring(3)); // 'getFoo' -> 'foo'
            dispatchers.add(new NameBasedDispatcher(name) {
                public void doDispatch(RequestImpl req, ResponseImpl rsp, Object node) throws IOException, ServletException, IllegalAccessException, InvocationTargetException {
                    invoke(req,rsp,f.invoke(node));
                }
            });
        }

        // check public selector methods of the form static NODE.getTOKEN(StaplerRequest)
        for( final Function f : getMethods.signature(StaplerRequest.class) ) {
            String name = camelize(f.getName().substring(3)); // 'getFoo' -> 'foo'
            dispatchers.add(new NameBasedDispatcher(name) {
                public void doDispatch(RequestImpl req, ResponseImpl rsp, Object node) throws IOException, ServletException, IllegalAccessException, InvocationTargetException {
                    invoke(req,rsp,f.invoke(node,req));
                }
            });
        }

        // check public selector methods <obj>.get<Token>(String)
        for( final Function f : getMethods.signature(String.class) ) {
            String name = camelize(f.getName().substring(3)); // 'getFoo' -> 'foo'
            dispatchers.add(new NameBasedDispatcher(name,1) {
                public void doDispatch(RequestImpl req, ResponseImpl rsp, Object node) throws IOException, ServletException, IllegalAccessException, InvocationTargetException {
                    invoke(req,rsp,f.invoke(node,req.tokens.next()));
                }
            });
        }

        // check public selector methods <obj>.get<Token>(int)
        for( final Function f : getMethods.signature(int.class) ) {
            String name = camelize(f.getName().substring(3)); // 'getFoo' -> 'foo'
            dispatchers.add(new NameBasedDispatcher(name,1) {
                public void doDispatch(RequestImpl req, ResponseImpl rsp, Object node) throws IOException, ServletException, IllegalAccessException, InvocationTargetException {
                    int idx = Integer.valueOf(req.tokens.next());
                    invoke(req,rsp,f.invoke(node,idx));
                }
            });
        }

        // check action <obj>.do<token>(StaplerRequest,StaplerResponse)
        for( final Function f : node.methods.prefix("do").signature(StaplerRequest.class,StaplerResponse.class) ) {
            String name = camelize(f.getName().substring(2)); // 'doFoo' -> 'foo'
            dispatchers.add(new NameBasedDispatcher(name,0) {
                public void doDispatch(RequestImpl req, ResponseImpl rsp, Object node) throws IllegalAccessException, InvocationTargetException {
                    f.invoke(node,req,rsp);
                }
            });
        }

        if(node.clazz.isArray()) {
            dispatchers.add(new Dispatcher() {
                public boolean dispatch(RequestImpl req, ResponseImpl rsp, Object node) throws IOException, ServletException {
                    try {
                        invoke(req,rsp,((Object[])node)[req.tokens.nextAsInt()]);
                        return true;
                    } catch (NumberFormatException e) {
                        return false; // try next
                    }
                }
            });
        }

        if(List.class.isAssignableFrom(node.clazz)) {
            dispatchers.add(new Dispatcher() {
                public boolean dispatch(RequestImpl req, ResponseImpl rsp, Object node) throws IOException, ServletException {
                    try {
                        invoke(req,rsp,((List)node).get(req.tokens.nextAsInt()));
                        return true;
                    } catch (NumberFormatException e) {
                        return false; // try next
                    }
                }
            });
        }

        if(Map.class.isAssignableFrom(node.clazz)) {
            dispatchers.add(new Dispatcher() {
                public boolean dispatch(RequestImpl req, ResponseImpl rsp, Object node) throws IOException, ServletException {
                    try {
                        Object item = ((Map)node).get(req.tokens.peek());
                        if(item!=null) {
                            req.tokens.next();
                            invoke(req,rsp,item);
                            return true;
                        } else {
                            // otherwise just fall through
                            return false;
                        }
                    } catch (NumberFormatException e) {
                        return false; // try next
                    }
                }
            });
        }

        dispatchers.add(new Dispatcher() {
            public boolean dispatch(RequestImpl req, ResponseImpl rsp, Object node) throws IOException, ServletException {
                // check JSP views
                // I thought about generalizing this to invoke other resources (such as static images, etc)
                // but I realized that those would require a very different handling.
                // so for now we just assume it's a JSP
                String next = req.tokens.peek();
                if(next==null)  return false;

                RequestDispatcher disp = getResourceDispatcher(node,next+".jsp");
                if(disp==null)  return false;

                req.tokens.next();
                forward(disp,req,rsp);
                return true;
            }
        });

        // TODO: check if we can route to static resources
        // which directory shall we look up a resource from?

        // check action <obj>.doDynamic()
        for( final Function f : node.methods
            .signature(StaplerRequest.class,StaplerResponse.class)
            .name("doDynamic") ) {

            dispatchers.add(new Dispatcher() {
                public boolean dispatch(RequestImpl req, ResponseImpl rsp, Object node) throws IllegalAccessException, InvocationTargetException {
                    f.invoke(node,req,rsp);
                    return true;
                }
            });
        }
    }
}
