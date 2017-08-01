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
import org.kohsuke.stapler.annotations.StaplerObject;
import org.kohsuke.stapler.annotations.StaplerPath;
import org.kohsuke.stapler.bind.JavaScriptMethod;
import org.kohsuke.stapler.lang.FieldRef;
import org.kohsuke.stapler.lang.Klass;
import org.kohsuke.stapler.lang.MethodRef;

import javax.annotation.PostConstruct;
import javax.servlet.ServletException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static javax.servlet.http.HttpServletResponse.*;

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

    public final List<Dispatcher> dispatchers = new ArrayList<>();

    /**
     * Base metaclass.
     * Note that <tt>baseClass.clazz==clazz.getSuperClass()</tt>
     */
    public final MetaClass baseClass;

    /**
     * {@link WebApp} that owns this meta class.
     */
    public final WebApp webApp;

    /**
     * If there's a method annotated with @PostConstruct, that {@link MethodRef} object, linked
     * to the list of the base class.
     */
    private volatile SingleLinkedList<MethodRef> postConstructMethods;

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
        KlassDescriptor<?> node = new KlassDescriptor(klass);
        // TODO abstract this test into Klass so that e.g. Ruby can have idiomatic
        boolean staplerObject = clazz.getAnnotation(StaplerObject.class) != null;

        dispatchers.add(new DirectoryishDispatcher());

        if (HttpDeletable.class.isAssignableFrom(clazz))
            dispatchers.add(new HttpDeletableDispatcher());

        FunctionList staplerPaths = node.methods.staplerPath();
        FunctionList impliedPaths;
        if (staplerObject) {
            impliedPaths = FunctionList.emptyList();
        } else {
            impliedPaths = node.methods.nonStaplerPath();
        }

        for (Function f: staplerPaths.staplerMethod()) {
            for (String name: StaplerPath.Helper.getPaths(f)) {
                final Function ff = f.contextualize(new WebMethodContext(name));
                if (name.length() == 0) {
                    dispatchers.add(new IndexDispatcher(ff));
                } else {
                    dispatchers.add(new InvokeDispatcher(name, ff));
                }
            }
        }
        
        // check action <obj>.do<token>(...) and other WebMethods
        for (Function f : impliedPaths.webMethods()) {
            WebMethod a = f.getAnnotation(WebMethod.class);

            String[] names;
            if(a!=null && a.name().length>0)   names=a.name();
            else    names=new String[]{camelize(f.getName().substring(2))}; // 'doFoo' -> 'foo'

            for (String name : names) {
                final Function ff = f.contextualize(new WebMethodContext(name));
                if (name.length()==0) {
                    dispatchers.add(new IndexDispatcher(ff));
                } else {
                    dispatchers.add(new InvokeDispatcher(name, ff));
                }
            }
        }

        // check action <obj>.doIndex(...)
        for (Function f : impliedPaths.name("doIndex")) {
            dispatchers.add(new IndexDispatcher(f.contextualize(new WebMethodContext(""))));
        }

        // JavaScript proxy method with @StaplerRMI
        // reacts only to a specific content type
        for (final Function f : staplerPaths.staplerRmi()) {
            for (String name : StaplerPath.Helper.getPaths(f))
                dispatchers.add(new JavaScriptProxyMethodDispatcher(name,
                        f.contextualize(new JavaScriptMethodContext(name))));
        }

        // JavaScript proxy method invocations for <obj>js<token>
        // reacts only to a specific content type
        for (Function f : impliedPaths.prefix("js")) {
            String name = camelize(f.getName().substring(2)); // jsXyz -> xyz
            f = f.contextualize(new JavaScriptMethodContext(name));
            dispatchers.add(new JavaScriptProxyMethodDispatcher(name, f));
        }

        // JavaScript proxy method with @JavaScriptMethod
        // reacts only to a specific content type
        for (final Function f : impliedPaths.annotated(JavaScriptMethod.class)) {
            JavaScriptMethod a = f.getAnnotation(JavaScriptMethod.class);

            String[] names;
            if (a != null && a.name().length > 0) names = a.name();
            else names = new String[]{f.getName()};

            for (String name : names)
                dispatchers.add(new JavaScriptProxyMethodDispatcher(name,
                        f.contextualize(new JavaScriptMethodContext(name))));
        }

        for (Facet f : webApp.facets)
            f.buildViewDispatchers(this, dispatchers);

        for (Facet f : webApp.facets)
            f.buildIndexDispatchers(this, dispatchers);

        Dispatcher d = IndexHtmlDispatcher.make(webApp.context, clazz);
        if (d!=null)
            dispatchers.add(d);

        // check public properties of the form NODE.TOKEN
        for (final FieldRef f : node.fields) {
            if (StaplerPath.Helper.isPath(f)) {
                for (String name: StaplerPath.Helper.getPaths(f)) {
                    dispatchers.add(new FieldSelectDispatcher(name, f));
                }
            } else if (!staplerObject) {
                dispatchers.add(new FieldSelectDispatcher(f.getName(), f));
            }
        }

        FunctionList selectorMethods = staplerPaths.nonStaplerMethod().nonStaplerRmi();
        // selector methods with zero args
        for (Function f: selectorMethods.signature()) {
            for (String name: StaplerPath.Helper.getPaths(f)) {
                final Function ff = f.contextualize(new TraversalMethodContext(name));
                dispatchers.add(new NoArgSelectDispatcher(name, ff));
            }
        }
        // selector methods taking just StaplerRequest
        for (Function f : selectorMethods.signature(StaplerRequest.class)) {
            for (String name : StaplerPath.Helper.getPaths(f)) {
                dispatchers.add(new RequestArgSelectDispatcher(name,
                        f.contextualize(new TraversalMethodContext(name))));
            }
        }
        // check public selector methods <obj>.get<Token>(String)
        for (Function f : selectorMethods.signature(String.class)) {
            for (String name : StaplerPath.Helper.getPaths(f)) {
                final Function ff = f.contextualize(new TraversalMethodContext(name));
                dispatchers.add(new StringArgSelectDispatcher(name, ff));
            }
        }

        // check public selector methods <obj>.get<Token>(int)
        for (Function f : selectorMethods.signature(int.class)) {
            for (String name : StaplerPath.Helper.getPaths(f)) {
                dispatchers.add(new IntArgSelectDispatcher(name, f.contextualize(new TraversalMethodContext(name))));
            }
        }

        // check public selector methods <obj>.get<Token>(long)
        // TF: I'm sure these for loop blocks could be dried out in some way.
        for (Function f : selectorMethods.signature(long.class)) {
            for (String name : StaplerPath.Helper.getPaths(f)) {
                dispatchers.add(new LongArgSelectDispatcher(name, f.contextualize(new TraversalMethodContext(name))));
            }
        }

        FunctionList getMethods = impliedPaths.prefix("get");

        // check public selector methods of the form NODE.getTOKEN()
        for (Function f : getMethods.signature()) {
            String name = nameOfGetter(f.getName());
            if (name != null) {
                dispatchers.add(new NoArgSelectDispatcher(name, f.contextualize(new TraversalMethodContext(name))));
            }
        }

        // check public selector methods of the form static NODE.getTOKEN(StaplerRequest)
        for (Function f : getMethods.signature(StaplerRequest.class)) {
            String name = nameOfGetter(f.getName());
            if (name != null) {
                dispatchers.add(new RequestArgSelectDispatcher(name, f.contextualize(new TraversalMethodContext(name))));
            }
        }

        // check public selector methods <obj>.get<Token>(String)
        for (Function f : getMethods.signature(String.class)) {
            String name = nameOfGetter(f.getName());
            if (name != null) {
                dispatchers.add(new StringArgSelectDispatcher(name, f.contextualize(new TraversalMethodContext(name))));
            }
        }

        // check public selector methods <obj>.get<Token>(int)
        for (Function f : getMethods.signature(int.class)) {
            String name = nameOfGetter(f.getName());
            if (name != null) {
                dispatchers.add(new IntArgSelectDispatcher(name, f.contextualize(new TraversalMethodContext(name))));
            }
        }

        // check public selector methods <obj>.get<Token>(long)
        // TF: I'm sure these for loop blocks could be dried out in some way.
        for (Function f : getMethods.signature(long.class)) {
            String name = nameOfGetter(f.getName());
            if (name != null) {
                dispatchers.add(new LongArgSelectDispatcher(name, f.contextualize(new TraversalMethodContext(name))));
            }
        }

        if (klass.isArray()) {
            dispatchers.add(new Dispatcher() {
                public boolean dispatch(RequestImpl req, ResponseImpl rsp, Object node) throws IOException, ServletException {
                    if(!req.tokens.hasMore())
                        return false;
                    try {
                        int index = req.tokens.nextAsInt();
                        if (traceable())
                            traceEval(req, rsp, node, "", "[" + index + "]");
                        req.getStapler().invoke(req, rsp, klass.getArrayElement(node, index));
                        return true;
                    } catch (IndexOutOfBoundsException e) {
                        if(traceable())
                            trace(req,rsp,"-> IndexOutOfRange");
                        rsp.sendError(SC_NOT_FOUND);
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

        if(klass.isMap()) {
            dispatchers.add(new Dispatcher() {
                public boolean dispatch(RequestImpl req, ResponseImpl rsp, Object node) throws IOException, ServletException {
                    if(!req.tokens.hasMore())
                        return false;
                    try {
                        String key = req.tokens.peek();
                        if(traceable())
                            traceEval(req,rsp,"",".get(\""+key+"\")");

                        Object item = klass.getMapElement(node,key);
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

        for (Facet f : webApp.facets) {
            f.buildFallbackDispatchers(this, dispatchers);
        }

        // check public selector methods <obj>.getDynamic(<token>,...)
        for (Function f : selectorMethods) {
            if (StaplerPath.Helper.isDynamic(f)) {
                final Function ff = f.contextualize(new TraversalMethodContext(TraversalMethodContext.DYNAMIC));
                dispatchers.add(new DynamicSelectDispatcher(ff));
            }
        }
        for (Function f : getMethods.signatureStartsWith(String.class).name("getDynamic")) {
            final Function ff = f.contextualize(new TraversalMethodContext(TraversalMethodContext.DYNAMIC));
            dispatchers.add(new DynamicSelectDispatcher(ff));
        }

        for (Function f : staplerPaths.staplerMethod()) {
            if (StaplerPath.Helper.isDynamic(f)) {
                final Function ff = f.contextualize(new WebMethodContext(WebMethodContext.DYNAMIC));
                dispatchers.add(new DynamicInvokeDispatcher(ff));
            }
        }

        // check action <obj>.doDynamic(...)
        for (Function f : impliedPaths.name("doDynamic")) {
            final Function ff = f.contextualize(new WebMethodContext(WebMethodContext.DYNAMIC));
            dispatchers.add(new DynamicInvokeDispatcher(ff));
        }
    }

    /**
     * Returns all the methods in the ancestry chain annotated with {@link PostConstruct}
     * from those defined in the derived type toward those defined in the base type.
     *
     * Normally invocation requires visiting the list in the reverse order.
     * @since 1.220
     */
    public SingleLinkedList<MethodRef> getPostConstructMethods() {
        if (postConstructMethods ==null) {
            SingleLinkedList<MethodRef> l = baseClass==null ? SingleLinkedList.<MethodRef>empty() : baseClass.getPostConstructMethods();

            for (MethodRef mr : klass.getDeclaredMethods()) {
                if (mr.hasAnnotation(PostConstruct.class)) {
                    l = l.grow(mr);
                }
            }
            postConstructMethods = l;
        }
        return postConstructMethods;
    }

    private String getProtectedRole(FieldRef f) {
        try {
            LimitedTo a = f.getAnnotation(LimitedTo.class);
            return (a!=null)?a.value():null;
        } catch (LinkageError e) {
            return null;    // running in JDK 1.4
        }
    }

    @Override
    public String toString() {
        return "MetaClass["+klass+"]";
    }

    private static String nameOfGetter(String name) {
        return name.length() <= 3 ? null : camelize(name.substring(3));
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
            Type[] genericTypes = f.getGenericParameterTypes();
            if (args.length != types.length) {
                throw new IllegalArgumentException("argument count mismatch between " + jsargs + " and " + Arrays.toString(genericTypes));
            }

            for (int i=0; i<args.length; i++)
                args[i] = req.bindJSON(genericTypes[i],types[i],jsargs.get(i));

            return f.bindAndInvokeAndServeResponse(node,req,rsp,args);
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

    private static class IntArgSelectDispatcher extends NameBasedDispatcher {
        private final Function ff;

        public IntArgSelectDispatcher(String name, Function ff) {
            super(name, 1);
            this.ff = ff;
        }

        public boolean doDispatch(RequestImpl req, ResponseImpl rsp, Object node) throws IOException, ServletException, IllegalAccessException,

                InvocationTargetException {
            int idx = req.tokens.nextAsInt();
            if(traceable())
                traceEval(req,rsp,node, ff.getName()+"("+idx+")");
            req.getStapler().invoke(req,rsp, ff.invoke(req, rsp, node,idx));
            return true;
        }

        public String toString() {
            return String.format("%1$s(int) for url=/%2$s/N/...", ff.getQualifiedName(),name);
        }
    }

    private static class LongArgSelectDispatcher extends NameBasedDispatcher {

        private final Function ff;

        public LongArgSelectDispatcher(String name, Function ff) {
            super(name, 1);
            this.ff = ff;
        }

        public boolean doDispatch(RequestImpl req, ResponseImpl rsp, Object node)
                throws IOException, ServletException, IllegalAccessException, InvocationTargetException {
            long idx = req.tokens.nextAsLong();
            if (traceable())
                traceEval(req, rsp, node, ff.getName() + "(" + idx + ")");
            req.getStapler().invoke(req, rsp, ff.invoke(req, rsp, node, idx));
            return true;
        }
        public String toString() {
            return String.format("%1$s(long) for url=/%2$s/N/...", ff.getQualifiedName(), name);
        }

    }

    private static class StringArgSelectDispatcher extends NameBasedDispatcher {
        private final Function ff;

        public StringArgSelectDispatcher(String name, Function ff) {
            super(name, 1);
            this.ff = ff;
        }

        public boolean doDispatch(RequestImpl req, ResponseImpl rsp, Object node)
                throws IOException, ServletException, IllegalAccessException, InvocationTargetException {
            String token = req.tokens.next();
            if (traceable())
                traceEval(req, rsp, node, ff.getName() + "(\"" + token + "\")");
            req.getStapler().invoke(req, rsp, ff.invoke(req, rsp, node, token));
            return true;
        }

        public String toString() {
            return String.format("%1$s(String) for url=/%2$s/TOKEN/...", ff.getQualifiedName(), name);
        }
    }

    private static class RequestArgSelectDispatcher extends NameBasedDispatcher {
        private final Function ff;

        public RequestArgSelectDispatcher(String name, Function ff) {
            super(name);
            this.ff = ff;
        }

        public boolean doDispatch(RequestImpl req, ResponseImpl rsp, Object node) throws IOException, ServletException, IllegalAccessException,

                InvocationTargetException {
            if(traceable())
                traceEval(req,rsp,node, ff.getName()+"(...)");
            req.getStapler().invoke(req,rsp, ff.invoke(req, rsp, node, req));
            return true;
        }

        public String toString() {
            return String.format("%1$s(StaplerRequest) for url=/%2$s/...", ff.getQualifiedName(),name);
        }
    }

    private static class NoArgSelectDispatcher extends NameBasedDispatcher {
        private final Function ff;

        public NoArgSelectDispatcher(String name, Function ff) {
            super(name);
            this.ff = ff;
        }

        public boolean doDispatch(RequestImpl req, ResponseImpl rsp, Object node) throws IOException, ServletException, IllegalAccessException,

                InvocationTargetException {
            if(traceable())
                traceEval(req,rsp,node, ff.getName()+"()");
            req.getStapler().invoke(req,rsp, ff.invoke(req, rsp, node));
            return true;
        }

        public String toString() {
            return String.format("%1$s() for url=/%2$s/...", ff.getQualifiedName(),name);
        }
    }

    private static class DynamicSelectDispatcher extends Dispatcher {
        private final Function ff;

        public DynamicSelectDispatcher(Function ff) {
            this.ff = ff;
        }

        public boolean dispatch(RequestImpl req, ResponseImpl rsp, Object node) throws IllegalAccessException,
                InvocationTargetException, IOException, ServletException {
            if(!req.tokens.hasMore())
                return false;
            String token = req.tokens.next();
            if(traceable())
                traceEval(req,rsp,node,ff.getName() + "(\""+token+"\",...)");

            Object target = ff.bindAndInvoke(node, req,rsp, token);
            if(target!=null) {
                req.getStapler().invoke(req,rsp, target);
                return true;
            } else {
                if(traceable())
                    // indent:    "-> evaluate(
                    trace(req,rsp,"            %s.%s(\"%s\",...)==null. Back tracking.",node,ff.getName(),token);
                req.tokens.prev(); // cancel the next effect
                return false;
            }
        }

        public String toString() {
            return String.format("%s(String,StaplerRequest,StaplerResponse) for url=/TOKEN/...", ff.getQualifiedName());
        }
    }

    private static class InvokeDispatcher extends NameBasedDispatcher {
        private final Function ff;

        public InvokeDispatcher(String name, Function ff) {
            super(name);
            this.ff = ff;
        }

        public boolean doDispatch(RequestImpl req, ResponseImpl rsp, Object node)
                throws IllegalAccessException, InvocationTargetException, ServletException,
                IOException {
            if (traceable())
                trace(req, rsp, "-> <%s>.%s(...)", node, ff.getName());
            return ff.bindAndInvokeAndServeResponse(node, req, rsp);
        }

        public String toString() {
            return ff.getQualifiedName() + "(...) for url=/" + name + "/...";
        }
    }

    private static class DynamicInvokeDispatcher extends Dispatcher {
        private final Function ff;

        public DynamicInvokeDispatcher(Function ff) {
            this.ff = ff;
        }

        public boolean dispatch(RequestImpl req, ResponseImpl rsp, Object node) throws IllegalAccessException,
                InvocationTargetException, ServletException, IOException {
            if(traceable())
                trace(req,rsp,"-> <%s>.%s(...)",node, ff.getName());
            return ff.bindAndInvokeAndServeResponse(node,req,rsp);
        }

        public String toString() {
            return String.format("%s(StaplerRequest,StaplerResponse) for any URL", ff.getQualifiedName());
        }
    }

    private class FieldSelectDispatcher extends NameBasedDispatcher {
        final String role;
        private final FieldRef f;

        public FieldSelectDispatcher(String name, FieldRef f) {
            super(name);
            this.f = f;
            role = getProtectedRole(f);
        }

        public boolean doDispatch(RequestImpl req, ResponseImpl rsp, Object node) throws IOException, ServletException, IllegalAccessException {
            if(role!=null && !req.isUserInRole(role))
                throw new IllegalAccessException("Needs to be in role "+role);

            if(traceable())
                traceEval(req,rsp,node, f.getName());
            req.getStapler().invoke(req, rsp, f.get(node));
            return true;
        }

        public String toString() {
            return String.format("%1$s for url=/%2$s/...", f.getQualifiedName(), f.getName());
        }
    }
}
