package org.kohsuke.stapler;

import org.apache.commons.jelly.JellyContext;
import org.apache.commons.jelly.JellyException;
import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.Script;
import org.apache.commons.jelly.XMLOutput;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * The stapler version of the {@link Class} object,
 * that retains some useful cache about a class and its view.
 *
 * @author Kohsuke Kawaguchi
 */
public class MetaClass {
    /**
     * This meta class wraps this class
     */
    private final Class clazz;

    private final MetaClassLoader classLoader;

    public final List<Dispatcher> dispatchers = new ArrayList<Dispatcher>();

    /**
     * Base metaclass.
     * Note that <tt>baseClass.clazz==clazz.getSuperClass()</tt>
     */
    private final MetaClass baseClass;

    /**
     * Compiled jelly script views of this class.
     * Access needs to be synchronized.
     */
    private final Map<String,Script> scripts = new HashMap<String,Script>();


    private MetaClass(Class clazz) {
        this.clazz = clazz;
        this.baseClass = get(clazz.getSuperclass());
        this.classLoader = MetaClassLoader.get(clazz.getClassLoader());

        buildDispatchers(
            new ClassDescriptor(clazz,null/*support wrappers*/));
    }


    private void buildDispatchers( ClassDescriptor node ) {
        // check public properties of the form NODE.TOKEN
        for (final Field f : node.fields) {
            dispatchers.add(new NameBasedDispatcher(f.getName()) {
                final String role = getProtectedRole(f);
                public void doDispatch(RequestImpl req, ResponseImpl rsp, Object node) throws IOException, ServletException, IllegalAccessException {
                    if(role!=null && !req.isUserInRole(role))
                        throw new IllegalAccessException("Needs to be in role "+role);
                    req.getStapler().invoke(req, rsp, f.get(node));
                }
            });
        }

        FunctionList getMethods = node.methods.prefix("get");

        // check public selector methods of the form NODE.getTOKEN()
        for( final Function f : getMethods.signature() ) {
            if(f.getName().length()<=3)
                continue;
            String name = camelize(f.getName().substring(3)); // 'getFoo' -> 'foo'
            dispatchers.add(new NameBasedDispatcher(name) {
                public void doDispatch(RequestImpl req, ResponseImpl rsp, Object node) throws IOException, ServletException, IllegalAccessException, InvocationTargetException {
                    req.getStapler().invoke(req,rsp,f.invoke(req, node));
                }
            });
        }

        // check public selector methods of the form static NODE.getTOKEN(StaplerRequest)
        for( final Function f : getMethods.signature(StaplerRequest.class) ) {
            if(f.getName().length()<=3)
                continue;
            String name = camelize(f.getName().substring(3)); // 'getFoo' -> 'foo'
            dispatchers.add(new NameBasedDispatcher(name) {
                public void doDispatch(RequestImpl req, ResponseImpl rsp, Object node) throws IOException, ServletException, IllegalAccessException, InvocationTargetException {
                    req.getStapler().invoke(req,rsp,f.invoke(req, node,req));
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
                    req.getStapler().invoke(req,rsp,f.invoke(req, node,req.tokens.next()));
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
                    int idx = Integer.valueOf(req.tokens.next());
                    req.getStapler().invoke(req,rsp,f.invoke(req, node,idx));
                }
            });
        }

        // check action <obj>.do<token>(StaplerRequest,StaplerResponse)
        for( final Function f : node.methods.prefix("do").signature(StaplerRequest.class,StaplerResponse.class) ) {
            String name = camelize(f.getName().substring(2)); // 'doFoo' -> 'foo'
            dispatchers.add(new NameBasedDispatcher(name,0) {
                public void doDispatch(RequestImpl req, ResponseImpl rsp, Object node) throws IllegalAccessException, InvocationTargetException {
                    f.invoke(req, node,req,rsp);
                }
            });
        }

        if(node.clazz.isArray()) {
            dispatchers.add(new Dispatcher() {
                public boolean dispatch(RequestImpl req, ResponseImpl rsp, Object node) throws IOException, ServletException {
                    if(!req.tokens.hasMore())
                        return false;
                    try {
                        req.getStapler().invoke(req,rsp,((Object[])node)[req.tokens.nextAsInt()]);
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
                        req.getStapler().invoke(req,rsp,((List)node).get(req.tokens.nextAsInt()));
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
                        Object item = ((Map)node).get(req.tokens.peek());
                        if(item!=null) {
                            req.tokens.next();
                            req.getStapler().invoke(req,rsp,item);
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

                Stapler stapler = req.getStapler();

                RequestDispatcher disp = stapler.getResourceDispatcher(node,next+".jsp");
                if(disp==null)  return false;

                req.tokens.next();
                stapler.forward(disp,req,rsp);
                return true;
            }
        });

        dispatchers.add(new Dispatcher() {
            public boolean dispatch(RequestImpl req, ResponseImpl rsp, Object node) throws IOException, ServletException {
                // check Jelly view
                String next = req.tokens.peek();
                if(next==null)  return false;

                try {
                    Script script = findScript(next);

                    if(script==null)        return false;   // no Jelly script found

                    req.tokens.next();

                    invoke(req, rsp, script, node);

                    return true;
                } catch (JellyException e) {
                    throw new ServletException(e);
                }
            }
        });

        // check action <obj>.doIndex(req,rsp)
        for( final Function f : node.methods
            .signature(StaplerRequest.class,StaplerResponse.class)
            .name("doIndex") ) {

            dispatchers.add(new Dispatcher() {
                public boolean dispatch(RequestImpl req, ResponseImpl rsp, Object node) throws IllegalAccessException, InvocationTargetException {
                    if(req.tokens.hasMore())
                        return false;   // applicable only when there's no more token
                    f.invoke(req,node,req,rsp);
                    return true;
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
                    req.getStapler().invoke(req,rsp,f.invoke(req,node,token,req,rsp));
                    return true;
                }
            });
        }
    }

    private void invoke(RequestImpl req, ResponseImpl rsp, Script script, Object it) throws IOException, JellyTagException {
        // invoke Jelly script to render result
        JellyContext context = new JellyContext();
        context.setVariable("request",req);
        context.setVariable("response",rsp);
        context.setVariable("it",it);

        OutputStream output = rsp.getOutputStream();
        output = new FilterOutputStream(output) {
            public void flush() {
                // flushing ServletOutputStream causes Tomcat to
                // send out headers, making it impossible to set contentType from the script.
                // so don't let Jelly flush.
            }
        };
        XMLOutput xmlOutput = XMLOutput.createXMLOutput(output);
        script.run(context,xmlOutput);
        xmlOutput.flush();
        xmlOutput.close();
        output.close();
    }

    /**
     * Locates the Jelly script view of the given name.
     */
    public Script findScript(String name) throws JellyException {
        Script script;
        synchronized(scripts) {
            script = scripts.get(name);
            if(script==null || NO_CACHE) {
                URL res = clazz.getClassLoader().
                    getResource(clazz.getName().replace('.','/').replace('$','/')+'/'+name +".jelly");
                if(res!=null) {
                    script = classLoader.craeteContext().compileScript(res);
                    scripts.put(name,script);
                }
            }
        }
        if(script!=null)
            return script;

        // not found on this class, delegate to the parent
        if(baseClass!=null)
            return baseClass.findScript(name);

        return null;
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


    public static boolean NO_CACHE = false;

    static {
        try {
            NO_CACHE = Boolean.getBoolean("stapler.jelly.noCache");
        } catch (SecurityException e) {
            // ignore.
        }
    }


    public static MetaClass get(Class c) {
        if(c==null)     return null;
        synchronized(classMap) {
            MetaClass mc = classMap.get(c);
            if(mc==null) {
                mc = new MetaClass(c);
                classMap.put(c,mc);
            }
            return mc;
        }
    }

    /**
     * All {@link MetaClass}es.
     *
     * Avoids class leaks by {@link WeakHashMap}.
     */
    private static final Map<Class,MetaClass> classMap = new WeakHashMap<Class,MetaClass>();
}
