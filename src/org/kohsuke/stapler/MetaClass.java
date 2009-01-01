package org.kohsuke.stapler;

import javax.servlet.ServletException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * The stapler version of the {@link Class} object,
 * that retains some useful cache about a class and its view.
 *
 * @author Kohsuke Kawaguchi
 */
public class MetaClass extends TearOffSupport {
    /**
     * This meta class wraps this class
     */
    public final Class clazz;

    /**
     * {@link MetaClassLoader} that wraps {@code clazz.getClassLoader()}.
     * Null if the class is loaded by the bootstrap classloader.
     */
    public final MetaClassLoader classLoader;

    public final List<Dispatcher> dispatchers = new ArrayList<Dispatcher>();

    /**
     * Base metaclass.
     * Note that <tt>baseClass.clazz==clazz.getSuperClass()</tt>
     */
    public final MetaClass baseClass;

    /**
     * {@link WebApp} that owns this meta class.
     */
    public final WebApp webApp;

    /*package*/ MetaClass(WebApp webApp, Class clazz) {
        this.clazz = clazz;
        this.webApp = webApp;
        this.baseClass = webApp.getMetaClass(clazz.getSuperclass());
        this.classLoader = MetaClassLoader.get(clazz.getClassLoader());

        buildDispatchers(
            new ClassDescriptor(clazz,null/*support wrappers*/));
    }

    /**
     * Build {@link #dispatchers}.
     *
     * <p>
     * This is the meat of URL dispatching. It looks at the class
     * via reflection and figures out what URLs are handled by who. 
     */
    private void buildDispatchers( ClassDescriptor node ) {
        // check action <obj>.do<token>(...)
        for( final Function f : node.methods.prefix("do") ) {
            WebMethod a = f.getAnnotation(WebMethod.class);
            
            String[] names;
            if(a!=null && a.name().length>0)   names=a.name();
            else    names=new String[]{camelize(f.getName().substring(2))}; // 'doFoo' -> 'foo'

            for (String name : names) {
                dispatchers.add(new NameBasedDispatcher(name,0) {
                    public void doDispatch(RequestImpl req, ResponseImpl rsp, Object node) throws IllegalAccessException, InvocationTargetException, ServletException {
                        if(traceable())
                            trace(req,rsp,"-> <%s>.%s(...)",node,f.getName());
                        f.bindAndInvoke(node,req,rsp);
                    }
                });
            }
        }
        

        for (Facet f : webApp.facets)
            f.buildViewDispatchers(this, dispatchers);

        // check action <obj>.doIndex(req,rsp)
        for( final Function f : node.methods
            .signature(StaplerRequest.class,StaplerResponse.class)
            .name("doIndex") ) {

            dispatchers.add(new Dispatcher() {
                public boolean dispatch(RequestImpl req, ResponseImpl rsp, Object node) throws IllegalAccessException, InvocationTargetException {
                    if(req.tokens.hasMore())
                        return false;   // applicable only when there's no more token

                    if(traceable())
                        trace(req,rsp,"-> <%s>.doIndex(...)",node);

                    f.invoke(req,node,req,rsp);
                    return true;
                }
            });
        }

        // check public properties of the form NODE.TOKEN
        for (final Field f : node.fields) {
            dispatchers.add(new NameBasedDispatcher(f.getName()) {
                final String role = getProtectedRole(f);
                public void doDispatch(RequestImpl req, ResponseImpl rsp, Object node) throws IOException, ServletException, IllegalAccessException {
                    if(role!=null && !req.isUserInRole(role))
                        throw new IllegalAccessException("Needs to be in role "+role);

                    if(traceable())
                        traceEval(req,rsp,node,f.getName());
                    req.getStapler().invoke(req, rsp, f.get(node));
                }
            });
        }

        FunctionList getMethods = node.methods.prefix("get");

        // check public selector methods of the form NODE.getTOKEN()
        for( final Function f : getMethods.signature() ) {
            if(f.getName().length()<=3)
                continue;

            WebMethod a = f.getAnnotation(WebMethod.class);

            String[] names;
            if(a!=null && a.name().length>0)   names=a.name();
            else    names=new String[]{camelize(f.getName().substring(3))}; // 'getFoo' -> 'foo'


            for (String name : names) {
                dispatchers.add(new NameBasedDispatcher(name) {
                    public void doDispatch(RequestImpl req, ResponseImpl rsp, Object node) throws IOException, ServletException, IllegalAccessException, InvocationTargetException {
                        if(traceable())
                            traceEval(req,rsp,node,f.getName()+"()");
                        req.getStapler().invoke(req,rsp, f.invoke(req, node));
                    }
                });
            }
        }

        // check public selector methods of the form static NODE.getTOKEN(StaplerRequest)
        for( final Function f : getMethods.signature(StaplerRequest.class) ) {
            if(f.getName().length()<=3)
                continue;
            String name = camelize(f.getName().substring(3)); // 'getFoo' -> 'foo'
            dispatchers.add(new NameBasedDispatcher(name) {
                public void doDispatch(RequestImpl req, ResponseImpl rsp, Object node) throws IOException, ServletException, IllegalAccessException, InvocationTargetException {
                    if(traceable())
                        traceEval(req,rsp,node,f.getName()+"(...)");
                    req.getStapler().invoke(req,rsp, f.invoke(req, node, req));
                }
            });
        }

        // check public selector methods <obj>.get<Token>(String)
        for( final Function f : getMethods.signature(String.class) ) {
            if(f.getName().length()<=3)
                continue;
            String name = camelize(f.getName().substring(3)); // 'getFoo' -> 'foo'
            dispatchers.add(new NameBasedDispatcher(name,1) {
                public void doDispatch(RequestImpl req, ResponseImpl rsp, Object node) throws IOException, ServletException, IllegalAccessException, InvocationTargetException {
                    String token = req.tokens.next();
                    if(traceable())
                        traceEval(req,rsp,node,f.getName()+"(\""+token+"\")");
                    req.getStapler().invoke(req,rsp, f.invoke(req,node,token));
                }
            });
        }

        // check public selector methods <obj>.get<Token>(int)
        for( final Function f : getMethods.signature(int.class) ) {
            if(f.getName().length()<=3)
                continue;
            String name = camelize(f.getName().substring(3)); // 'getFoo' -> 'foo'
            dispatchers.add(new NameBasedDispatcher(name,1) {
                public void doDispatch(RequestImpl req, ResponseImpl rsp, Object node) throws IOException, ServletException, IllegalAccessException, InvocationTargetException {
                    int idx = req.tokens.nextAsInt();
                    if(traceable())
                        traceEval(req,rsp,node,f.getName()+"("+idx+")");
                    req.getStapler().invoke(req,rsp, f.invoke(req,node,idx));
                }
            });
        }

        if(node.clazz.isArray()) {
            dispatchers.add(new Dispatcher() {
                public boolean dispatch(RequestImpl req, ResponseImpl rsp, Object node) throws IOException, ServletException {
                    if(!req.tokens.hasMore())
                        return false;
                    try {
                        int index = req.tokens.nextAsInt();
                        if(traceable())
                            traceEval(req,rsp,node,"((Object[])",")["+index+"]");
                        req.getStapler().invoke(req,rsp, ((Object[]) node)[index]);
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
                    if(!req.tokens.hasMore())
                        return false;
                    try {
                        int index = req.tokens.nextAsInt();
                        if(traceable())
                            traceEval(req,rsp,node,"((List)",").get("+index+")");
                        req.getStapler().invoke(req,rsp, ((List) node).get(index));
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
                    if(!req.tokens.hasMore())
                        return false;
                    try {
                        String key = req.tokens.peek();
                        if(traceable())
                            traceEval(req,rsp,"((Map)",").get(\""+key+"\")");

                        Object item = ((Map)node).get(key);
                        if(item!=null) {
                            req.tokens.next();
                            req.getStapler().invoke(req,rsp,item);
                            return true;
                        } else {
                            // otherwise just fall through
                            if(traceable())
                                trace(req,rsp,"Map.get(\""+key+"\")==null. Back tracking.");
                            return false;
                        }
                    } catch (NumberFormatException e) {
                        return false; // try next
                    }
                }
            });
        }

        // TODO: check if we can route to static resources
        // which directory shall we look up a resource from?

        // check action <obj>.doDynamic(req,rsp)
        for( final Function f : node.methods
            .signature(StaplerRequest.class,StaplerResponse.class)
            .name("doDynamic") ) {

            dispatchers.add(new Dispatcher() {
                public boolean dispatch(RequestImpl req, ResponseImpl rsp, Object node) throws IllegalAccessException, InvocationTargetException {
                    if(traceable())
                        trace(req,rsp,"-> <%s>.doDynamic(...)",node);

                    f.invoke(req,node,req,rsp);
                    return true;
                }
            });
        }

        // check public selector methods <obj>.getDynamic(<token>,req,rsp)
        for( final Function f : getMethods.signature(String.class,StaplerRequest.class,StaplerResponse.class).name("getDynamic")) {
            dispatchers.add(new Dispatcher() {
                public boolean dispatch(RequestImpl req, ResponseImpl rsp, Object node) throws IllegalAccessException, InvocationTargetException, IOException, ServletException {
                    if(!req.tokens.hasMore())
                        return false;
                    String token = req.tokens.next();
                    if(traceable())
                        traceEval(req,rsp,node,"getDynamic(\""+token+"\",...)");

                    Object target = f.invoke(req, node, token, req, rsp);
                    if(target!=null) {
                        req.getStapler().invoke(req,rsp, target);
                        return true;
                    } else {
                        if(traceable())
                            traceEval(req,rsp,"getDynamic(\""+token+"\",...)==null. Back tracking.");
                        req.tokens.prev(); // cancel the next effect
                        return false;
                    }
                }
            });
        }
    }

    private String getProtectedRole(Field f) {
        try {
            LimitedTo a = f.getAnnotation(LimitedTo.class);
            return (a!=null)?a.value():null;
        } catch (LinkageError e) {
            return null;    // running in JDK 1.4
        }
    }

    private static String camelize(String name) {
        return Character.toLowerCase(name.charAt(0))+name.substring(1);
    }
    
    /**
     * Don't cache anything in memory, so that any change
     * will take effect instantly.
     */
    public static boolean NO_CACHE = false;

    static {
        try {
            NO_CACHE = Boolean.getBoolean("stapler.jelly.noCache");
        } catch (SecurityException e) {
            // ignore.
        }
    }
}
