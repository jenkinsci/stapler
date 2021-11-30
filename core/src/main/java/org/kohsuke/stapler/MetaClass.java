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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import net.sf.json.JSONArray;
import org.apache.commons.io.IOUtils;
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
import java.util.logging.Level;
import java.util.logging.Logger;

import static javax.servlet.http.HttpServletResponse.*;

/**
 * Created one instance each for a {@link Klass},
 * that retains some useful cache about a class and its views.
 *
 * @author Kohsuke Kawaguchi
 * @see WebApp#getMetaClass(Klass)
 */
public class MetaClass extends TearOffSupport {
    private static final Logger LOGGER = Logger.getLogger(MetaClass.class.getName());
    
    /**
     * This meta class wraps this class
     *
     * @deprecated as of 1.177
     *      Use {@link #klass}. If you really want the Java class representation, use {@code klass.toJavaClass()}.
     */
    @Deprecated
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

        dispatchers.add(new DirectoryishDispatcher());

        if (HttpDeletable.class.isAssignableFrom(clazz))
            dispatchers.add(new HttpDeletableDispatcher());

        // check action <obj>.do<token>(...) and other WebMethods
        registerDoToken(node);

        // check action <obj>.doIndex(...)
        for (Function f : node.methods.name("doIndex")) {
            dispatchers.add(new IndexDispatcher(f.contextualize(new WebMethodContext(""))));
        }

        // JavaScript proxy method invocations for <obj>js<token>
        // reacts only to a specific content type
        for (Function f : node.methods.prefix("js") ) {
            String name = camelize(f.getName().substring(2)); // jsXyz -> xyz
            f = f.contextualize(new JavaScriptMethodContext(name));
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
                dispatchers.add(new JavaScriptProxyMethodDispatcher(name,f.contextualize(new JavaScriptMethodContext(name))));
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
            final boolean accepted = webApp.getFilterForFields().keep(f);

            dispatchers.add(new NameBasedDispatcher(f.getName()) {
                final String role = getProtectedRole(f);
                @Override
                public boolean doDispatch(RequestImpl req, ResponseImpl rsp, Object node) throws IOException, ServletException, IllegalAccessException {
                    if (accepted) {
                        if (role != null && !req.isUserInRole(role))
                            throw new IllegalAccessException("Needs to be in role " + role);

                        Dispatcher.anonymizedTraceEval(req, rsp, node, "%s#%s", f.getName());
                        if (traceable())
                            traceEval(req, rsp, node, f.getName());
                        req.getStapler().invoke(req, rsp, f.get(node));
                        return true;
                    } else {
                        return webApp.getFilteredFieldTriggerListener().onFieldTrigger(f, req, rsp, node, f.getQualifiedName());
                    }
                }
                @Override
                public String toString() {
                    if (accepted) {
                        return String.format("%3$s %1$s for url=/%2$s/...", f.getQualifiedName(), f.getName(), f.getReturnType());
                    } else {
                        return String.format("BLOCKED: %3$s %1$s for url=/%2$s/...", f.getQualifiedName(), f.getName(), f.getReturnType());
                    }
                }
            });
        }

        FunctionList getMethods = node.methods.prefix("get").filter(m -> !m.getSignature().equals("method java.lang.Object getClass"));
        FunctionList filteredGetMethods;
        if(LEGACY_GETTER_MODE || webApp.getFilterForGetMethods() == null){
            LOGGER.log(Level.FINE, "Stapler is using the legacy GETTER_MODE");
            filteredGetMethods = getMethods;
        }else{
            filteredGetMethods = getMethods.filter(webApp.getFilterForGetMethods());
            if(LOGGER.isLoggable(Level.FINER)){
                // to ease the debug
                List<Function> excludedByNew = minus(getMethods, filteredGetMethods);
    
                if(!excludedByNew.isEmpty()){
                    for (Function excluded : excludedByNew) {
                        LOGGER.log(Level.FINER, "The following method is now blocked: {0}", excluded.getDisplayName());
                    }
                }
            }
        }

        // check public selector methods of the form NODE.getTOKEN()
        for (final Function f : getMethods.signature()) {
            if(f.getName().length()<=3)
                continue;

            String name = camelize(f.getName().substring(3)); // 'getFoo' -> 'foo'
            final Function ff = f.contextualize(new TraversalMethodContext(name));
            final boolean isAccepted = filteredGetMethods.contains(f);

            dispatchers.add(new NameBasedDispatcher(name) {
                @Override
                public boolean doDispatch(RequestImpl req, ResponseImpl rsp, Object node) throws IOException, ServletException, IllegalAccessException, InvocationTargetException {
                    if(isAccepted){
                        Dispatcher.anonymizedTraceEval(req, rsp, node, "%s#%s()", ff.getName());
                        if(traceable())
                            traceEval(req,rsp,node,ff.getName()+"()");
                        req.getStapler().invoke(req,rsp, ff.invoke(req, rsp, node));
                        return true;
                    }else{
                        return webApp.getFilteredGetterTriggerListener().onGetterTrigger(f, req, rsp, node, ff.getName()+"()");
                    }
                }
                @Override
                public String toString() {
                    if(isAccepted){
                        return String.format("%3$s %1$s() for url=/%2$s/...",ff.getQualifiedName(),name, ff.getReturnType().getName());
                    }else{
                        return String.format("BLOCKED: %3$s %1$s() for url=/%2$s/...",ff.getQualifiedName(),name, ff.getReturnType().getName());
                    }
                }
            });
        }

        // check public selector methods of the form static NODE.getTOKEN(StaplerRequest)
        for (final Function f : getMethods.signature(StaplerRequest.class)) {
            if(f.getName().length()<=3)
                continue;
            String name = camelize(f.getName().substring(3)); // 'getFoo' -> 'foo'
            final Function ff = f.contextualize(new TraversalMethodContext(name));
            final boolean isAccepted = filteredGetMethods.contains(f);

            dispatchers.add(new NameBasedDispatcher(name) {
                @Override
                public boolean doDispatch(RequestImpl req, ResponseImpl rsp, Object node) throws IOException, ServletException, IllegalAccessException, InvocationTargetException {
                    if(isAccepted){
                        Dispatcher.anonymizedTraceEval(req, rsp, node, "%s#%s(...)", ff.getName());
                        if(traceable())
                            traceEval(req,rsp,node,ff.getName()+"(...)");
                        req.getStapler().invoke(req,rsp, ff.invoke(req, rsp, node, req));
                        return true;
                    }else{
                        return webApp.getFilteredGetterTriggerListener().onGetterTrigger(f, req, rsp, node, ff.getName()+"(...)");
                    }
                }
                @Override
                public String toString() {
                    if(isAccepted) {
                        return String.format("%3$s %1$s(StaplerRequest) for url=/%2$s/...", ff.getQualifiedName(), name, ff.getReturnType().getName());
                    }else{
                        return String.format("BLOCKED: %3$s %1$s(StaplerRequest) for url=/%2$s/...", ff.getQualifiedName(), name, ff.getReturnType().getName());
                    }
                }
            });
        }

        // check public selector methods <obj>.get<Token>(String)
        for (final Function f : getMethods.signature(String.class)) {
            if(f.getName().length()<=3)
                continue;
            String name = camelize(f.getName().substring(3)); // 'getFoo' -> 'foo'
            final Function ff = f.contextualize(new TraversalMethodContext(name));
            final boolean isAccepted = filteredGetMethods.contains(f);

            dispatchers.add(new NameBasedDispatcher(name,1) {
                @Override
                public boolean doDispatch(RequestImpl req, ResponseImpl rsp, Object node) throws IOException, ServletException, IllegalAccessException, InvocationTargetException {
                    if(isAccepted){
                        String token = req.tokens.next();
                        Dispatcher.anonymizedTraceEval(req, rsp, node, "%s#%s(String)", ff.getName());
                        if(traceable())
                            traceEval(req,rsp,node,ff.getName()+"(\""+token+"\")");
                        req.getStapler().invoke(req,rsp, ff.invoke(req, rsp, node,token));
                        return true;
                    }else{
                        String token = req.tokens.next();
                        try{
                            return webApp.getFilteredGetterTriggerListener().onGetterTrigger(f, req, rsp, node, ff.getName()+"(\""+token+"\")");
                        }
                        finally{
                            req.tokens.prev();
                        }
                    }
                }
                @Override
                public String toString() {
                    if(isAccepted) {
                        return String.format("%3$s %1$s(String) for url=/%2$s/TOKEN/...", ff.getQualifiedName(), name, ff.getReturnType().getName());
                    }else{
                        return String.format("BLOCKED: %3$s %1$s(String) for url=/%2$s/TOKEN/...", ff.getQualifiedName(), name, ff.getReturnType().getName());
                    }
                }
            });
        }

        // check public selector methods <obj>.get<Token>(int)
        for (final Function f : getMethods.signature(int.class)) {
            if(f.getName().length()<=3)
                continue;
            String name = camelize(f.getName().substring(3)); // 'getFoo' -> 'foo'
            final Function ff = f.contextualize(new TraversalMethodContext(name));
            final boolean isAccepted = filteredGetMethods.contains(f);

            dispatchers.add(new NameBasedDispatcher(name,1) {
                @Override
                public boolean doDispatch(RequestImpl req, ResponseImpl rsp, Object node) throws IOException, ServletException, IllegalAccessException, InvocationTargetException {
                    if(isAccepted){
                        int idx = req.tokens.nextAsInt();
                        Dispatcher.anonymizedTraceEval(req, rsp, node, "%s#%s(int)", ff.getName());
                        if(traceable())
                            traceEval(req,rsp,node,ff.getName()+"("+idx+")");
                        req.getStapler().invoke(req,rsp, ff.invoke(req, rsp, node,idx));
                        return true;
                    }else{
                        int idx = req.tokens.nextAsInt();
                        try{
                            return webApp.getFilteredGetterTriggerListener().onGetterTrigger(f, req, rsp, node, ff.getName()+"("+idx+")");
                        }
                        finally{
                            req.tokens.prev();
                        }
                    }
                }
                @Override
                public String toString() {
                    if(isAccepted){
                        return String.format("%3$s %1$s(int) for url=/%2$s/N/...",ff.getQualifiedName(),name, ff.getReturnType().getName());
                    }else{
                        return String.format("BLOCKED: %3$s %1$s(int) for url=/%2$s/N/...",ff.getQualifiedName(),name, ff.getReturnType().getName());
                    }
                }
            });
        }

        // check public selector methods <obj>.get<Token>(long)
        // TF: I'm sure these for loop blocks could be dried out in some way.
        for (final Function f : getMethods.signature(long.class)) {
            if(f.getName().length()<=3)
                continue;
            String name = camelize(f.getName().substring(3)); // 'getFoo' -> 'foo'
            final Function ff = f.contextualize(new TraversalMethodContext(name));
            final boolean isAccepted = filteredGetMethods.contains(f);

            dispatchers.add(new NameBasedDispatcher(name,1) {
                @Override
                public boolean doDispatch(RequestImpl req, ResponseImpl rsp, Object node) throws IOException, ServletException, IllegalAccessException, InvocationTargetException {
                    if(isAccepted){
                        long idx = req.tokens.nextAsLong();
                        Dispatcher.anonymizedTraceEval(req, rsp, node, "%s#%s(long)", ff.getName());
                        if(traceable())
                            traceEval(req,rsp,node,ff.getName()+"("+idx+")");
                        req.getStapler().invoke(req,rsp, ff.invoke(req, rsp, node,idx));
                        return true;
                    }else{
                        long idx = req.tokens.nextAsLong();
                        try{
                            return webApp.getFilteredGetterTriggerListener().onGetterTrigger(f, req, rsp, node, ff.getName()+"("+idx+")");
                        }
                        finally{
                            req.tokens.prev();
                        }
                    }
                }
                @Override
                public String toString() {
                    if(isAccepted) {
                        return String.format("%3$s %1$s(long) for url=/%2$s/N/...", ff.getQualifiedName(), name, ff.getReturnType().getName());
                    }else{
                        return String.format("BLOCKED: %3$s %1$s(long) for url=/%2$s/N/...", ff.getQualifiedName(), name, ff.getReturnType().getName());
                    }
                }
            });
        }

        if (klass.isArray()) {
            dispatchers.add(new Dispatcher() {
                @Override
                public boolean dispatch(RequestImpl req, ResponseImpl rsp, Object node) throws IOException, ServletException {
                    if(!req.tokens.hasMore())
                        return false;
                    try {
                        int index = req.tokens.nextAsInt();
                        Dispatcher.anonymizedTraceEval(req, rsp, node, "%s[idx]");
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
                @Override
                public String toString() {
                    return "Array look-up for url=/N/...";
                }
            });
        }

        if(klass.isMap()) {
            dispatchers.add(new Dispatcher() {
                @Override
                public boolean dispatch(RequestImpl req, ResponseImpl rsp, Object node) throws IOException, ServletException {
                    if(!req.tokens.hasMore())
                        return false;
                    try {
                        String key = req.tokens.peek();
                        Dispatcher.anonymizedTraceEval(req, rsp, node, "%s: Map access");
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
                @Override
                public String toString() {
                    return "Map.get(String) look-up for url=/TOKEN/...";
                }
            });
        }

        // TODO: check if we can route to static resources
        // which directory shall we look up a resource from?

        for (Facet f : webApp.facets)
            f.buildFallbackDispatchers(this, dispatchers);

        // check public selector methods <obj>.getDynamic(<token>,...)
        for (Function f : getMethods.signatureStartsWith(String.class).name("getDynamic")) {
            final Function ff = f.contextualize(new TraversalMethodContext(TraversalMethodContext.DYNAMIC));
            dispatchers.add(new Dispatcher() {
                @Override
                public boolean dispatch(RequestImpl req, ResponseImpl rsp, Object node) throws IllegalAccessException, InvocationTargetException, IOException, ServletException {
                    if(!req.tokens.hasMore())
                        return false;
                    String token = req.tokens.next();
                    Dispatcher.anonymizedTraceEval(req, rsp, node, "%s#getDynamic(...)");
                    if(traceable())
                        traceEval(req,rsp,node,"getDynamic(\""+token+"\",...)");

                    Object target = ff.bindAndInvoke(node, req,rsp, token);
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
                @Override
                public String toString() {
                    return String.format("%2$s %s(String,StaplerRequest,StaplerResponse) for url=/TOKEN/...",ff.getQualifiedName(), ff.getReturnType().getName());
                }
            });
        }

        // check action <obj>.doDynamic(...)
        for (Function f : node.methods.name("doDynamic")) {
            final Function ff = f.contextualize(new WebMethodContext(WebMethodContext.DYNAMIC));
            dispatchers.add(new Dispatcher() {
                @Override
                public boolean dispatch(RequestImpl req, ResponseImpl rsp, Object node) throws IllegalAccessException, InvocationTargetException, ServletException, IOException {
                    Dispatcher.anonymizedTraceEval(req, rsp, node, "%s#doDynamic(...)");
                    if(traceable())
                        trace(req,rsp,"-> <%s>.doDynamic(...)",node);
                    return ff.bindAndInvokeAndServeResponse(node,req,rsp);
                }
                @Override
                public String toString() {
                    return String.format("%s(StaplerRequest,StaplerResponse) for any URL",ff.getQualifiedName());
                }
            });
        }

        // provide a "last chance" for the application to add/remove dispatchers
        DispatchersFilter dispatchersFilter = webApp.getDispatchersFilter();
        if(dispatchersFilter != null){
            dispatchersFilter.applyOn(this, node.methods, dispatchers);
        }
    }

    private void registerDoToken(KlassDescriptor<?> node){
        final FunctionList filteredFunctions;
        FunctionList functions;
        if(LEGACY_WEB_METHOD_MODE || webApp.getFilterForDoActions() == null){
            functions = node.methods.webMethodsLegacy();
            filteredFunctions = functions;
            LOGGER.log(Level.FINE, "Stapler is using the legacy METHOD_MODE");
        } else {
            functions = node.methods.webMethodsLegacy();
            filteredFunctions = functions.filter(webApp.getFilterForDoActions());
            if(LOGGER.isLoggable(Level.FINER)){
                List<Function> excludedByNew = minus(functions, filteredFunctions);
                
                if(!excludedByNew.isEmpty()){
                    for (Function excluded : excludedByNew) {
                        LOGGER.log(Level.FINER, "The following method is now blocked: {0}", excluded.getDisplayName());
                    }
                }
            }
        }
        
        for (final Function f : functions) {
            WebMethod a = f.getAnnotation(WebMethod.class);
            
            String[] names;
            if(a!=null && a.name().length>0)   names=a.name();
            else    names=new String[]{camelize(f.getName().substring(2))}; // 'doFoo' -> 'foo'
            
            for (String name : names) {
                final Function ff = f.contextualize(new WebMethodContext(name));
                if (name.length()==0) {
                    dispatchers.add(new IndexDispatcher(ff));
                } else {
                    final boolean isAccepted = filteredFunctions.contains(f);
                    dispatchers.add(new NameBasedDispatcher(name) {
                        @Override
                        public boolean doDispatch(RequestImpl req, ResponseImpl rsp, Object node) throws IllegalAccessException, InvocationTargetException, ServletException, IOException {
                            if(isAccepted){
                                Dispatcher.anonymizedTraceEval(req, rsp, node, "%s#%s", ff.getName());
                                if (traceable())
                                    trace(req, rsp, "-> <%s>.%s(...)", node, ff.getName());
                                return ff.bindAndInvokeAndServeResponse(node, req, rsp);
                            }else{
                                return webApp.getFilteredDoActionTriggerListener().onDoActionTrigger(f, req, rsp, node);
                            }
                        }
                        
                        @Override
                        public String toString() {
                            if(isAccepted){
                                return String.format("%1$s(...) for url=/%2$s/...", ff.getQualifiedName(), name);
                            }else{
                                return String.format("BLOCKED: %1$s(...) for url=/%2$s/...", ff.getQualifiedName(), name);
                            }
                        }
                    });
                }
            }
        }
    }
    
    /**
     * Return (A - B)
     */
    private List<Function> minus(FunctionList a, FunctionList b){
        List<Function> aMinusB = new ArrayList<>();
        for(Function f : a){
            if(!b.contains(f)){
                aMinusB.add(f);
            }
        }
    
        return aMinusB;
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
            SingleLinkedList<MethodRef> l = baseClass==null ? SingleLinkedList.empty() : baseClass.getPostConstructMethods();

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
            return a != null ? a.value() : null;
        } catch (LinkageError e) {
            return null;    // running in JDK 1.4
        }
    }

    @Override
    public String toString() {
        return "MetaClass["+klass+"]";
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

        @Override
        public boolean doDispatch(RequestImpl req, ResponseImpl rsp, Object node) throws IllegalAccessException, InvocationTargetException, ServletException, IOException {
            if (!req.isJavaScriptProxyCall())
                return false;

            req.stapler.getWebApp().getCrumbIssuer().validateCrumb(req,req.getHeader("Crumb"));

            Dispatcher.anonymizedTraceEval(req, rsp, node, "%s#%s", f.getName());
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

        @Override
        public String toString() {
            return f.getQualifiedName()+"(...) for url=/"+name+"/...";
        }
    }

    /**
     * Don't cache anything in memory, so that any change
     * will take effect instantly.
     */
    @SuppressFBWarnings(value = "MS_SHOULD_BE_REFACTORED_TO_BE_FINAL", justification = "Legacy switch.")
    public static boolean NO_CACHE = false;
    /**
     * In case the breaking changes are not desired. They are recommended for security reason.
     */
    @SuppressFBWarnings(value = "MS_SHOULD_BE_REFACTORED_TO_BE_FINAL", justification = "Legacy switch.")
    public static boolean LEGACY_GETTER_MODE = false;
    /**
     * In case the breaking changes are not desired. They are recommended for security reason.
     */
    @SuppressFBWarnings(value = "MS_SHOULD_BE_REFACTORED_TO_BE_FINAL", justification = "Legacy switch.")
    public static boolean LEGACY_WEB_METHOD_MODE = false;

    static {
        try {
            NO_CACHE = Boolean.getBoolean("stapler.jelly.noCache");
            LEGACY_GETTER_MODE = Boolean.getBoolean("stapler.legacyGetterDispatcherMode");
            LEGACY_WEB_METHOD_MODE = Boolean.getBoolean("stapler.legacyWebMethodDispatcherMode");
        } catch (SecurityException e) {
            // ignore.
        }
    }
}
