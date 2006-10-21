package org.kohsuke.stapler;

import org.apache.commons.jelly.Script;
import org.kohsuke.stapler.jelly.JellyClassTearOff;
import org.kohsuke.stapler.jelly.groovy.GroovyClassTearOff;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

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

    private MetaClass(Class clazz) {
        this.clazz = clazz;
        this.baseClass = get(clazz.getSuperclass());
        this.classLoader = MetaClassLoader.get(clazz.getClassLoader());

        buildDispatchers(
            new ClassDescriptor(clazz,null/*support wrappers*/));
    }


    private void buildDispatchers( ClassDescriptor node ) {
        // check action <obj>.do<token>(StaplerRequest,StaplerResponse)
        for( final Function f : node.methods.prefix("do").signature(StaplerRequest.class,StaplerResponse.class) ) {
            String name = camelize(f.getName().substring(2)); // 'doFoo' -> 'foo'
            dispatchers.add(new NameBasedDispatcher(name,0) {
                public void doDispatch(RequestImpl req, ResponseImpl rsp, Object node) throws IllegalAccessException, InvocationTargetException {
                    f.invoke(req, node,req,rsp);
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

        try {
            dispatchers.add(new Dispatcher() {
                final JellyClassTearOff tearOff = loadTearOff(JellyClassTearOff.class);

                public boolean dispatch(RequestImpl req, ResponseImpl rsp, Object node) throws IOException, ServletException {
                    // check Jelly view
                    String next = req.tokens.peek();
                    if(next==null)  return false;

                    try {
                        Script script = tearOff.findScript(next+".jelly");

                        if(script==null)        return false;   // no Jelly script found

                        req.tokens.next();

                        JellyClassTearOff.invokeScript(req, rsp, script, node);

                        return true;
                    } catch (RuntimeException e) {
                        throw e;
                    } catch (IOException e) {
                        throw e;
                    } catch (Exception e) {
                        throw new ServletException(e);
                    }
                }
            });
        } catch (LinkageError e) {
            // jelly not present. ignore
        }

        try {
            dispatchers.add(new Dispatcher() {
                final GroovyClassTearOff tearOff = loadTearOff(GroovyClassTearOff.class);

                public boolean dispatch(RequestImpl req, ResponseImpl rsp, Object node) throws IOException, ServletException {
                    // check Groovy view
                    String next = req.tokens.peek();
                    if(next==null)  return false;

                    try {
                        Script script = tearOff.findScript(next+".groovy");
                        if(script==null)        return false;   // no Groovy script found

                        req.tokens.next();

                        JellyClassTearOff.invokeScript(req, rsp, script, node);

                        return true;
                    } catch (RuntimeException e) {
                        throw e;
                    } catch (IOException e) {
                        throw e;
                    } catch (Exception e) {
                        throw new ServletException(e);
                    }
                }
            });
        } catch (LinkageError e) {
            // groovy not present. ignore
        }

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
