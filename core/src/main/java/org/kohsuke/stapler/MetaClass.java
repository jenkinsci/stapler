/*
 * Copyright (c) 2004-2010, Kohsuke Kawaguchi
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided
 * that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice, this list of
 *       conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright notice, this list of
 *       conditions and the following disclaimer in the documentation and/or other materials
 *       provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS
 * OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
 * AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER
 * IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF
 * THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.kohsuke.stapler;

import net.sf.json.JSONArray;
import org.apache.commons.io.IOUtils;
import org.kohsuke.stapler.bind.JavaScriptMethod;
import org.kohsuke.stapler.lang.Klass;

import javax.servlet.ServletException;
import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created one instance each for a {@link Klass},
 * that retains some useful cache about a class and its views.
 *
 * @author Kohsuke Kawaguchi
 * @see WebApp#getMetaClass(Klass)
 */
public class MetaClass extends TearOffSupport {
    /**
     * This meta class wraps this class
     * 
     * @deprecated as of 1.177
     *      Use {@link #klass}. If you really want the Java class representation, use {@code klass.toJavaClass()}.
     */
    public final Class clazz;
    
    public final Klass<?> klass;

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

    /*package*/ MetaClass(WebApp webApp, Klass<?> klass) {
        this.clazz = klass.toJavaClass();
        this.klass = klass;
        this.webApp = webApp;
        this.baseClass = webApp.getMetaClass(klass.getSuperClass());
        this.classLoader = MetaClassLoader.get(clazz.getClassLoader());
        buildDispatchers();
    }

    /**
     * Build {@link #dispatchers}.
     *
     * <p>
     * This is the meat of URL dispatching. It looks at the class
     * via reflection and figures out what URLs are handled by who. 
     */
    /*package*/ void buildDispatchers() {
        this.dispatchers.clear();
        ClassDescriptor node = new ClassDescriptor(clazz,null/*TODO:support wrappers*/);

        // check action <obj>.do<token>(...)
        for( final Function f : node.methods.prefix("do") ) {
            WebMethod a = f.getAnnotation(WebMethod.class);
            
            String[] names;
            if(a!=null && a.name().length>0)   names=a.name();
            else    names=new String[]{camelize(f.getName().substring(2))}; // 'doFoo' -> 'foo'

            for (String name : names) {
                dispatchers.add(new NameBasedDispatcher(name,0) {
                    public boolean doDispatch(RequestImpl req, ResponseImpl rsp, Object node) throws IllegalAccessException, InvocationTargetException, ServletException, IOException {
                        if(traceable())
                            trace(req,rsp,"-> <%s>.%s(...)",node,f.getName());
                        f.bindAndInvokeAndServeResponse(node, req, rsp);
                        return true;
                    }
                    public String toString() {
                        return f.getQualifiedName()+"(...) for url=/"+name+"/...";
                    }
                });
            }
        }

        // JavaScript proxy method invocations for <obj>js<token>
        // reacts only to a specific content type
        for( final Function f : node.methods.prefix("js") ) {
            String name = camelize(f.getName().substring(2)); // jsXyz -> xyz

            dispatchers.add(new JavaScriptProxyMethodDispatcher(name, f));
        }

        // JavaScript proxy method with @JavaScriptMethod
        // reacts only to a specific content type
        for( final Function f : node.methods.annotated(JavaScriptMethod.class) ) {
            JavaScriptMethod a = f.getAnnotation(JavaScriptMethod.class);

            String[] names;
            if(a!=null && a.name().length>0)   names=a.name();
            else    names=new String[]{f.getName()};

            for (String name : names)
                dispatchers.add(new JavaScriptProxyMethodDispatcher(name,f));
        }

        for (Facet f : webApp.facets)
            f.buildViewDispatchers(this, dispatchers);

        // check action <obj>.doIndex(...)
        for( final Function f : node.methods.name("doIndex") ) {

            dispatchers.add(new Dispatcher() {
                public boolean dispatch(RequestImpl req, ResponseImpl rsp, Object node) throws IllegalAccessException, InvocationTargetException, ServletException, IOException {
                    if(req.tokens.hasMore())
                        return false;   // applicable only when there's no more token

                    if(traceable())
                        trace(req,rsp,"-> <%s>.doIndex(...)",node);

                    f.bindAndInvokeAndServeResponse(node,req,rsp);
                    return true;
                }
                public String toString() {
                    return f.getQualifiedName()+"(StaplerRequest,StaplerResponse) for url=/";
                }
            });
        }

        // check public properties of the form NODE.TOKEN
        for (final Field f : node.fields) {
            dispatchers.add(new NameBasedDispatcher(f.getName()) {
                final String role = getProtectedRole(f);
                public boolean doDispatch(RequestImpl req, ResponseImpl rsp, Object node) throws IOException, ServletException, IllegalAccessException {
                    if(role!=null && !req.isUserInRole(role))
                        throw new IllegalAccessException("Needs to be in role "+role);

                    if(traceable())
                        traceEval(req,rsp,node,f.getName());
                    req.getStapler().invoke(req, rsp, f.get(node));
                    return true;
                }
                public String toString() {
                    return String.format("%1$s.%2$s for url=/%2$s/...",f.getDeclaringClass().getName(),f.getName());
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
                    public boolean doDispatch(RequestImpl req, ResponseImpl rsp, Object node) throws IOException, ServletException, IllegalAccessException, InvocationTargetException {
                        if(traceable())
                            traceEval(req,rsp,node,f.getName()+"()");
                        req.getStapler().invoke(req,rsp, f.invoke(req, node));
                        return true;
                    }
                    public String toString() {
                        return String.format("%1$s() for url=/%2$s/...",f.getQualifiedName(),name);
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
                public boolean doDispatch(RequestImpl req, ResponseImpl rsp, Object node) throws IOException, ServletException, IllegalAccessException, InvocationTargetException {
                    if(traceable())
                        traceEval(req,rsp,node,f.getName()+"(...)");
                    req.getStapler().invoke(req,rsp, f.invoke(req, node, req));
                    return true;
                }
                public String toString() {
                    return String.format("%1$s(StaplerRequest) for url=/%2$s/...",f.getQualifiedName(),name);
                }
            });
        }

        // check public selector methods <obj>.get<Token>(String)
        for( final Function f : getMethods.signature(String.class) ) {
            if(f.getName().length()<=3)
                continue;
            String name = camelize(f.getName().substring(3)); // 'getFoo' -> 'foo'
            dispatchers.add(new NameBasedDispatcher(name,1) {
                public boolean doDispatch(RequestImpl req, ResponseImpl rsp, Object node) throws IOException, ServletException, IllegalAccessException, InvocationTargetException {
                    String token = req.tokens.next();
                    if(traceable())
                        traceEval(req,rsp,node,f.getName()+"(\""+token+"\")");
                    req.getStapler().invoke(req,rsp, f.invoke(req,node,token));
                    return true;
                }
                public String toString() {
                    return String.format("%1$s(String) for url=/%2$s/TOKEN/...",f.getQualifiedName(),name);
                }
            });
        }

        // check public selector methods <obj>.get<Token>(int)
        for( final Function f : getMethods.signature(int.class) ) {
            if(f.getName().length()<=3)
                continue;
            String name = camelize(f.getName().substring(3)); // 'getFoo' -> 'foo'
            dispatchers.add(new NameBasedDispatcher(name,1) {
                public boolean doDispatch(RequestImpl req, ResponseImpl rsp, Object node) throws IOException, ServletException, IllegalAccessException, InvocationTargetException {
                    int idx = req.tokens.nextAsInt();
                    if(traceable())
                        traceEval(req,rsp,node,f.getName()+"("+idx+")");
                    req.getStapler().invoke(req,rsp, f.invoke(req,node,idx));
                    return true;
                }
                public String toString() {
                    return String.format("%1$s(int) for url=/%2$s/N/...",f.getQualifiedName(),name);
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
                public String toString() {
                    return "Array look-up for url=/N/...";
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
                        List list = (List) node;
                        if (0<=index && index<list.size())
                            req.getStapler().invoke(req,rsp, list.get(index));
                        else {
                            if(traceable())
                                trace(req,rsp,"-> IndexOutOfRange [0,%d)",list.size());
                            rsp.sendError(SC_NOT_FOUND);
                        }

                        return true;
                    } catch (NumberFormatException e) {
                        return false; // try next
                    }
                }
                public String toString() {
                    return "List.get(int) look-up for url=/N/...";
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
                public String toString() {
                    return "Map.get(String) look-up for url=/TOKEN/...";
                }
            });
        }

        // TODO: check if we can route to static resources
        // which directory shall we look up a resource from?

        for (Facet f : webApp.facets)
            f.buildFallbackDispatchers(this, dispatchers);

        // check action <obj>.doDynamic(...)
        for( final Function f : node.methods.name("doDynamic") ) {

            dispatchers.add(new Dispatcher() {
                public boolean dispatch(RequestImpl req, ResponseImpl rsp, Object node) throws IllegalAccessException, InvocationTargetException, ServletException, IOException {
                    if(traceable())
                        trace(req,rsp,"-> <%s>.doDynamic(...)",node);
                    f.bindAndInvokeAndServeResponse(node,req,rsp);
                    return true;
                }
                public String toString() {
                    return String.format("%s(StaplerRequest,StaplerResponse) for any URL",f.getQualifiedName());
                }
            });
        }

        // check public selector methods <obj>.getDynamic(<token>,...)
        for( final Function f : getMethods.signatureStartsWith(String.class).name("getDynamic")) {
            dispatchers.add(new Dispatcher() {
                public boolean dispatch(RequestImpl req, ResponseImpl rsp, Object node) throws IllegalAccessException, InvocationTargetException, IOException, ServletException {
                    if(!req.tokens.hasMore())
                        return false;
                    String token = req.tokens.next();
                    if(traceable())
                        traceEval(req,rsp,node,"getDynamic(\""+token+"\",...)");

                    Object target = f.bindAndInvoke(node, req,rsp, token);
                    if(target!=null) {
                        req.getStapler().invoke(req,rsp, target);
                        return true;
                    } else {
                        if(traceable())
                            // indent:    "-> evaluate(
                            trace(req,rsp,"            %s.getDynamic(\"%s\",...)==null. Back tracking.",node,token);
                        req.tokens.prev(); // cancel the next effect
                        return false;
                    }
                }
                public String toString() {
                    return String.format("%s(String,StaplerRequest,StaplerResponse) for url=/TOKEN/...",f.getQualifiedName());
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

    private static class JavaScriptProxyMethodDispatcher extends NameBasedDispatcher {
        private final Function f;

        public JavaScriptProxyMethodDispatcher(String name, Function f) {
            super(name, 0);
            this.f = f;
        }

        public boolean doDispatch(RequestImpl req, ResponseImpl rsp, Object node) throws IllegalAccessException, InvocationTargetException, ServletException, IOException {
            if (!req.isJavaScriptProxyCall())
                return false;

            req.stapler.getWebApp().getCrumbIssuer().validateCrumb(req,req.getHeader("Crumb"));

            if(traceable())
                trace(req,rsp,"-> <%s>.%s(...)",node, f.getName());

            JSONArray jsargs = JSONArray.fromObject(IOUtils.toString(req.getReader()));
            Object[] args = new Object[jsargs.size()];
            Class[] types = f.getParameterTypes();
            Type[] genericTypes = f.getParameterTypes();

            for (int i=0; i<args.length; i++)
                args[i] = req.bindJSON(genericTypes[i],types[i],jsargs.get(i));

            f.bindAndInvokeAndServeResponse(node,req,rsp,args);
            return true;
        }

        public String toString() {
            return f.getQualifiedName()+"(...) for url=/"+name+"/...";
        }
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
