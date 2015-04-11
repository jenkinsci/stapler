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

import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONNull;
import net.sf.json.JSONObject;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.Converter;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.jvnet.tiger_types.Lister;
import org.kohsuke.stapler.bind.BoundObjectTable;
import org.kohsuke.stapler.lang.Klass;
import org.kohsuke.stapler.lang.MethodRef;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import static java.util.logging.Level.*;
import static javax.servlet.http.HttpServletResponse.*;

/**
 * {@link StaplerRequest} implementation.
 * 
 * @author Kohsuke Kawaguchi
 */
public class RequestImpl extends HttpServletRequestWrapper implements StaplerRequest {
    /**
     * Tokenized URLs and consumed tokens.
     * This object is modified by {@link Stapler} as we parse through the URL.
     */
    public final TokenList tokens;
    /**
     * Ancesotr nodes traversed so far.
     * This object is modified by {@link Stapler} as we parse through the URL.
     */
    public final List<AncestorImpl> ancestors;

    private final List<Ancestor> ancestorsView;

    public final Stapler stapler;

    private final String originalRequestURI;

    /**
     * Cached result of {@link #getSubmittedForm()}
     */
    private JSONObject structuredForm;

    /**
     * If the request is "multipart/form-data", parsed result goes here.
     *
     * @see #parseMultipartFormData()
     */
    private Map<String, FileItem> parsedFormData;

    private BindInterceptor bindInterceptor = BindInterceptor.NOOP;

    public RequestImpl(Stapler stapler, HttpServletRequest request, List<AncestorImpl> ancestors, TokenList tokens) {
        super(request);
        this.stapler = stapler;
        this.ancestors = ancestors;
        this.ancestorsView = Collections.<Ancestor>unmodifiableList(ancestors);
        this.tokens = tokens;
        this.originalRequestURI = request.getRequestURI();
    }

    public boolean isJavaScriptProxyCall() {
        String ct = getContentType();
        return ct!=null && ct.startsWith("application/x-stapler-method-invocation");
    }

    public BoundObjectTable getBoundObjectTable() {
        return stapler.getWebApp().boundObjectTable;
    }

    public String createJavaScriptProxy(Object toBeExported) {
        return getBoundObjectTable().bind(toBeExported).getProxyScript();
    }

    public Stapler getStapler() {
        return stapler;
    }

    public WebApp getWebApp() {
        return stapler.getWebApp();
    }

    public String getRestOfPath() {
        return tokens.assembleRestOfPath();
    }

    public String getOriginalRestOfPath() {
        return tokens.assembleOriginalRestOfPath();
    }

    public ServletContext getServletContext() {
        return stapler.getServletContext();
    }

    public String getRequestURIWithQueryString() {
        String s = getRequestURI();
        String q = getQueryString();
        if (q!=null)    s+='?'+q;
        return s;
    }

    public StringBuffer getRequestURLWithQueryString() {
        StringBuffer s = getRequestURL();
        String q = getQueryString();
        if (q!=null)    s.append('?').append(q);
        return s;
    }

    public RequestDispatcher getView(Object it,String viewName) throws IOException {
        return getView(Klass.java(it.getClass()),it,viewName);
    }

    public RequestDispatcher getView(Class clazz, String viewName) throws IOException {
        return getView(Klass.java(clazz),null,viewName);
    }

    public RequestDispatcher getView(Klass<?> clazz, String viewName) throws IOException {
        return getView(clazz,null,viewName);
    }

    public RequestDispatcher getView(Klass<?> clazz, Object it, String viewName) throws IOException {
        for( Facet f : stapler.getWebApp().facets ) {
            RequestDispatcher rd = f.createRequestDispatcher(this,clazz,it,viewName);
            if(rd!=null)
                return rd;
        }

        return null;
    }

    public String getRootPath() {
        StringBuffer buf = super.getRequestURL();
        int idx = 0;
        for( int i=0; i<3; i++ )
            idx += buf.substring(idx).indexOf("/")+1;
        buf.setLength(idx-1);
        buf.append(super.getContextPath());
        return buf.toString();
    }

    public String getReferer() {
        return getHeader("Referer");
    }

    public List<Ancestor> getAncestors() {
        return ancestorsView;
    }

    public Ancestor findAncestor(Class type) {
        for( int i = ancestors.size()-1; i>=0; i-- ) {
            AncestorImpl a = ancestors.get(i);
            Object o = a.getObject();
            if (type.isInstance(o))
                return a;
        }

        return null;
    }

    public <T> T findAncestorObject(Class<T> type) {
        Ancestor a = findAncestor(type);
        if(a==null) return null;
        return type.cast(a.getObject());
    }

    public Ancestor findAncestor(Object anc) {
        for( int i = ancestors.size()-1; i>=0; i-- ) {
            AncestorImpl a = ancestors.get(i);
            Object o = a.getObject();
            if (o==anc)
                return a;
        }

        return null;
    }

    public boolean hasParameter(String name) {
        return getParameter(name)!=null;
    }

    public String getOriginalRequestURI() {
        return originalRequestURI;
    }

    public boolean checkIfModified(long lastModified, StaplerResponse rsp) {
        return checkIfModified(lastModified,rsp,0);
    }

    public boolean checkIfModified(long lastModified, StaplerResponse rsp, long expiration) {
        if(lastModified<=0)
            return false;

        // send out Last-Modified, or check If-Modified-Since
        String since = getHeader("If-Modified-Since");
        SimpleDateFormat format = Stapler.HTTP_DATE_FORMAT.get();
        if(since!=null) {
            try {
                long ims = format.parse(since).getTime();
                if(lastModified<ims+1000) {
                    // +1000 because date header is second-precision and Java has milli-second precision
                    rsp.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
                    return true;
                }
            } catch (NumberFormatException e) {
                // just ignore and serve the content
            } catch (ParseException e) {
                // just ignore and serve the content
            }
        }
        String tm = format.format(new Date(lastModified));
        rsp.setHeader("Last-Modified", tm);
        if(expiration==0) {
            // don't let browsers
            rsp.setHeader("Expires", tm);
        } else {
            // expire in "NOW+expiration" 
            rsp.setHeader("Expires",format.format(new Date(new Date().getTime()+expiration)));
        }
        return false;
    }

    public boolean checkIfModified(Date timestampOfResource, StaplerResponse rsp) {
        return checkIfModified(timestampOfResource.getTime(),rsp);
    }

    public boolean checkIfModified(Calendar timestampOfResource, StaplerResponse rsp) {
        return checkIfModified(timestampOfResource.getTimeInMillis(),rsp);
    }

    public BindInterceptor getBindInterceptor() {
        return bindInterceptor;
    }

    public BindInterceptor setBindListener(BindInterceptor bindListener) {
        return setBindInterceptor(bindListener);
    }

    public BindInterceptor setBindInterceptpr(BindInterceptor bindListener) {
        return setBindInterceptor(bindListener);
    }

    public BindInterceptor setBindInterceptor(BindInterceptor bindListener) {
        BindInterceptor o = this.bindInterceptor;
        this.bindInterceptor = bindListener;
        return o;
    }

    public void bindParameters(Object bean) {
        bindParameters(bean,"");
    }

    public void bindParameters(Object bean, String prefix) {
        Enumeration e = getParameterNames();
        while(e.hasMoreElements()) {
            String name = (String)e.nextElement();
            if(name.startsWith(prefix))
                fill(bean,name.substring(prefix.length()), getParameter(name) );
        }
    }

    public <T>
    List<T> bindParametersToList(Class<T> type, String prefix) {
        List<T> r = new ArrayList<T>();

        int len = Integer.MAX_VALUE;

        Enumeration e = getParameterNames();
        while(e.hasMoreElements()) {
            String name = (String)e.nextElement();
            if(name.startsWith(prefix))
                len = Math.min(len,getParameterValues(name).length);
        }

        if(len==Integer.MAX_VALUE)
            return r;   // nothing

        try {
            new ClassDescriptor(type).loadConstructorParamNames();
            // use the designated constructor for databinding
            for( int i=0; i<len; i++ )
                r.add(bindParameters(type,prefix,i));
        } catch (NoStaplerConstructorException _) {
            // no designated data binding constructor. use reflection
            try {
                for( int i=0; i<len; i++ ) {
                    T t = type.newInstance();
                    r.add(t);

                    e = getParameterNames();
                    while(e.hasMoreElements()) {
                        String name = (String)e.nextElement();
                        if(name.startsWith(prefix))
                            fill(t, name.substring(prefix.length()), getParameterValues(name)[i] );
                    }
                }
            } catch (InstantiationException x) {
                throw new InstantiationError(x.getMessage());
            } catch (IllegalAccessException x) {
                throw new IllegalAccessError(x.getMessage());
            }
        }

        return r;
    }

    public <T> T bindParameters(Class<T> type, String prefix) {
        return bindParameters(type,prefix,0);
    }

    public <T> T bindParameters(Class<T> type, String prefix, int index) {
        String[] names = new ClassDescriptor(type).loadConstructorParamNames();

        // the actual arguments to invoke the constructor with.
        Object[] args = new Object[names.length];

        // constructor
        Constructor<T> c = findConstructor(type, names.length);
        Class[] types = c.getParameterTypes();

        // convert parameters
        for( int i=0; i<names.length; i++ ) {
            String[] values = getParameterValues(prefix + names[i]);
            String param;
            if(values!=null)
                param = values[index];
            else
                param = null;

            Converter converter = Stapler.lookupConverter(types[i]);
            if (converter==null)
                throw new IllegalArgumentException("Unable to convert to "+types[i]);

            args[i] = converter.convert(types[i],param);
        }

        return invokeConstructor(c, args);
    }

    public <T> T bindJSON(Class<T> type, JSONObject src) {
        return type.cast(bindJSON(type, type, src));
    }

    public Object bindJSON(Type type, Class erasure, Object json) {
        return new TypePair(type,erasure).convertJSON(json);
    }

    public void bindJSON(Object bean, JSONObject src) {
        try {
            for( String key : (Set<String>)src.keySet() ) {
                TypePair type = getPropertyType(bean, key);
                if(type==null)
                    continue;

                fill(bean,key, type.convertJSON(src.get(key)));
            }
        } catch (IllegalAccessException e) {
            IllegalAccessError x = new IllegalAccessError(e.getMessage());
            x.initCause(e);
            throw x;
        } catch (InvocationTargetException x) {
            Throwable e = x.getTargetException();
            if(e instanceof RuntimeException)
                throw (RuntimeException)e;
            if(e instanceof Error)
                throw (Error)e;
            throw new RuntimeException(x);
        }
    }

    public <T> List<T> bindJSONToList(Class<T> type, Object src) {
        ArrayList<T> r = new ArrayList<T>();
        if (src instanceof JSONObject) {
            JSONObject j = (JSONObject) src;
            r.add(bindJSON(type,j));
        }
        if (src instanceof JSONArray) {
            JSONArray a = (JSONArray) src;
            for (Object o : a) {
                if (o instanceof JSONObject) {
                    JSONObject j = (JSONObject) o;
                    r.add(bindJSON(type,j));
                }
            }
        }
        return r;
    }


    private <T> T invokeConstructor(Constructor<T> c, Object[] args) {
        try {
            return c.newInstance(args);
        } catch (InstantiationException e) {
            InstantiationError x = new InstantiationError(e.getMessage());
            x.initCause(e);
            throw x;
        } catch (IllegalAccessException e) {
            IllegalAccessError x = new IllegalAccessError(e.getMessage());
            x.initCause(e);
            throw x;
        } catch (InvocationTargetException e) {
            Throwable x = e.getTargetException();
            if(x instanceof Error)
                throw (Error)x;
            if(x instanceof RuntimeException)
                throw (RuntimeException)x;
            throw new IllegalArgumentException(x);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Failed to invoke "+c+" with "+ Arrays.asList(args),e);
        }
    }

    private <T> Constructor<T> findConstructor(Class<T> type, int length) {
        Constructor<?>[] ctrs = type.getConstructors();
        // one with DataBoundConstructor is the most reliable
        for (Constructor c : ctrs) {
            if(c.getAnnotation(DataBoundConstructor.class)!=null) {
                if(c.getParameterTypes().length!=length)
                    throw new IllegalArgumentException(c+" has @DataBoundConstructor but it doesn't match with your .stapler file. Try clean rebuild");
                return c;
            }
        }
        // if not, maybe this was from @stapler-constructor,
        // so look for the constructor with the expected argument length.
        // this is not very reliable.
        for (Constructor c : ctrs) {
            if(c.getParameterTypes().length==length)
                return c;
        }
        throw new IllegalArgumentException(type+" does not have a constructor with "+length+" arguments");
    }

    private static void fill(Object bean, String key, Object value) {
        StringTokenizer tokens = new StringTokenizer(key);
        while(tokens.hasMoreTokens()) {
            String token = tokens.nextToken();
            boolean last = !tokens.hasMoreTokens();  // is this the last token?

            try {
                if(last) {
                    copyProperty(bean,token,value);
                } else {
                    bean = BeanUtils.getProperty(bean,token);
                }
            } catch (IllegalAccessException x) {
                throw new IllegalAccessError(x.getMessage());
            } catch (InvocationTargetException x) {
                Throwable e = x.getTargetException();
                if(e instanceof RuntimeException)
                    throw (RuntimeException)e;
                if(e instanceof Error)
                    throw (Error)e;
                throw new RuntimeException(x);
            } catch (NoSuchMethodException e) {
                // ignore if there's no such property
            }
        }
    }

    /**
     * Information about the type.
     */
    private final class TypePair {
        final Type genericType;
        /**
         * Erasure of {@link #genericType}
         */
        final Class type;

        TypePair(Type genericType, Class type) {
            this.genericType = genericType;
            this.type = type;
        }

        TypePair(Field f) {
            this(f.getGenericType(),f.getType());
        }

        /**
         * Converts the given JSON object (either {@link JSONObject}, {@link JSONArray}, or other primitive types
         * in JSON, to the type represented by the 'this' object.
         */
        public Object convertJSON(Object o) {
            Object r = bindInterceptor.onConvert(genericType, type, o);
            if (r!= BindInterceptor.DEFAULT)    return r; // taken over by the interceptor

            for (BindInterceptor i : getWebApp().bindInterceptors) {
                r = i.onConvert(genericType, type, o);
                if (r!= BindInterceptor.DEFAULT)    return r; // taken over by the interceptor
            }

            if(o==null || o instanceof JSONNull) {
                // this method returns null if the type is not primitive, which works.
                return ReflectionUtils.getVmDefaultValueFor(type);
            }

            if (type==JSONArray.class) {
                if (o instanceof JSONArray) return o;

                JSONArray a = new JSONArray();
                a.add(o);
                return a;
            }

            Lister l = Lister.create(type,genericType);

            if (o instanceof JSONObject) {
                JSONObject j = (JSONObject) o;

                if (j.isNullObject())   // another flavor of null. json-lib sucks.
                    return ReflectionUtils.getVmDefaultValueFor(type);

                if(l==null) {// single value conversion
                    try {
                        Class actualType = type;
                        String className = null;
                        if(j.has("stapler-class")) {
                            // deprecated as of 2.4-jenkins-4 but left here for a while until we are sure nobody uses this
                            className = j.getString("stapler-class");
                            LOGGER.log(FINE, "stapler-class is deprecated in favor of $class: {0}", className);
                        }
                        if(j.has("$class")) {
                            className = j.getString("$class");
                        }

                        if (className != null) {
                            // sub-type is specified in JSON.
                            // note that this can come from malicious clients, so we need to make sure we don't have security issues.

                            ClassLoader cl = stapler.getWebApp().getClassLoader();
                            try {
                                Class<?> subType = cl.loadClass(className);
                                if(!actualType.isAssignableFrom(subType))
                                    throw new IllegalArgumentException("Specified type "+subType+" is not assignable to the expected "+actualType);
                                actualType = (Class)subType; // I'm being lazy here
                            } catch (ClassNotFoundException e) {
                                throw new IllegalArgumentException("Class "+className+" is specified in JSON, but no such class found in "+cl,e);
                            }
                        }

                        return instantiate(actualType, j);
                    } catch (IllegalArgumentException e) {
                        throw new IllegalArgumentException("Failed to instantiate "+type+" from "+j,e);
                    }
                } else {// collection conversion
                    if(j.has("stapler-class-bag")) {
                        // this object is a hash from class names to their parameters
                        // build them into a collection via Lister

                        ClassLoader cl = stapler.getWebApp().getClassLoader();
                        for (Map.Entry<String,Object> e : (Set<Map.Entry<String,Object>>)j.entrySet()) {
                            Object v = e.getValue();

                            String className = e.getKey().replace('-','.'); // decode JSON-safe class name escaping
                            try {
                                Class<?> itemType = cl.loadClass(className);
                                if (v instanceof JSONObject) {
                                    l.add(bindJSON(itemType, (JSONObject) v));
                                }
                                if (v instanceof JSONArray) {
                                    for(Object i : bindJSONToList(itemType, (JSONArray) v))
                                        l.add(i);
                                }
                            } catch (ClassNotFoundException e1) {
                                // ignore unrecognized element
                            }
                        }
                    } else if (Enum.class.isAssignableFrom(l.itemType)) {
                        // this is a hash of element names as enum constant names
                        for (Map.Entry<String,Object> e : (Set<Map.Entry<String,Object>>)j.entrySet()) {
                            Object v = e.getValue();
                            if (v==null || (v instanceof Boolean && !(Boolean)v))
                                continue;       // skip if the value is null or false

                            l.add(Enum.valueOf(l.itemType,e.getKey()));
                        }
                    } else {
                        // only one value given to the collection
                        l.add(new TypePair(l.itemGenericType,l.itemType).convertJSON(j));
                    }
                    return l.toCollection();
                }
            }
            if (o instanceof JSONArray) {
                JSONArray a = (JSONArray) o;
                TypePair itemType = new TypePair(l.itemGenericType,l.itemType);
                for (Object item : a)
                    l.add(itemType.convertJSON(item));
                return l.toCollection();
            }

            if(Enum.class.isAssignableFrom(type))
                return Enum.valueOf(type,o.toString());

            if (l==null) {// single value conversion
                Converter converter = Stapler.lookupConverter(type);
                if (converter==null)
                    throw new IllegalArgumentException("Unable to convert to "+type);

                return converter.convert(type,o);
            } else {// single value in a collection
                Converter converter = Stapler.lookupConverter(l.itemType);
                if (converter==null)
                    throw new IllegalArgumentException("Unable to convert to "+l.itemType);

                l.add(converter.convert(type,o));
                return l.toCollection();
            }
        }
    }

    /**
     * Called after the actual type of the binding is figured out.
     */
    private Object instantiate(Class actualType, JSONObject j) {
        Object r = bindInterceptor.instantiate(actualType,j);
        if (r!=BindInterceptor.DEFAULT) return r;
        for (BindInterceptor bi : getWebApp().bindInterceptors) {
            r = bi.instantiate(actualType,j);
            if (r!=BindInterceptor.DEFAULT) return r;
        }

        if (actualType==JSONObject.class || actualType==JSON.class) return actualType.cast(j);

        String[] names = new ClassDescriptor(actualType).loadConstructorParamNames();

        // the actual arguments to invoke the constructor with.
        Object[] args = new Object[names.length];

        // constructor
        Constructor c = findConstructor(actualType, names.length);
        Class[] types = c.getParameterTypes();
        Type[] genTypes = c.getGenericParameterTypes();

        // convert parameters
        for( int i=0; i<names.length; i++ ) {
            try {
                args[i] = bindJSON(genTypes[i],types[i],j.get(names[i]));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Failed to convert the "+names[i]+" parameter of the constructor "+c,e);
            }
        }

        Object o = injectSetters(invokeConstructor(c, args), j, Arrays.asList(names));
        o = bindResolve(o,j);

        return o;
    }

    /**
     * Calls {@link DataBoundResolvable#bindResolve(StaplerRequest, JSONObject)} if the object has it.
     */
    private Object bindResolve(Object o, JSONObject src) {
        if (o instanceof DataBoundResolvable) {
            DataBoundResolvable dbr = (DataBoundResolvable) o;
            o = dbr.bindResolve(this,src);
        }
        return o;
    }

    /**
     * Performs {@link DataBoundSetter} injections.
     *
     * @param exclusions
     *      Properties that are already injected through the constructor, thus not subject of the setter injection.
     */
    private <T> T injectSetters(T r, JSONObject j, Collection<String> exclusions) {
        // try to assign rest of the properties
        OUTER:
        for (String key : (Set<String>)j.keySet()) {
            if (!exclusions.contains(key)) {
                try {
                    // try field injection first
                    for (Class c=r.getClass(); c!=null; c=c.getSuperclass()) {
                        try {
                            Field f = c.getDeclaredField(key);
                            if (f.getAnnotation(DataBoundSetter.class)!=null) {
                                f.setAccessible(true);
                                f.set(r, bindJSON(f.getGenericType(), f.getType(), j.get(key)));
                                continue OUTER;
                            }
                        } catch (NoSuchFieldException e) {
                            // recurse into parents
                        }
                    }

                    PropertyDescriptor pd = PropertyUtils.getPropertyDescriptor(r, key);
                    if (pd==null)   continue;

                    Method wm = pd.getWriteMethod();
                    if (wm==null)   continue;
                    if (wm.getAnnotation(DataBoundSetter.class)==null) {
                        LOGGER.warning("Tried to set value to "+key+" but method "+wm+" is missing @DataBoundSetter");
                        continue;
                    }

                    Class<?>[] pt = wm.getParameterTypes();

                    if (pt.length!=1) {
                        LOGGER.log(WARNING, "Setter method "+wm+" is expected to have 1 parameter");
                        continue;
                    }

                    // only invoking public methods for security reasons
                    wm.invoke(r, bindJSON(wm.getGenericParameterTypes()[0], pt[0], j.get(key)));
                } catch (IllegalAccessException e) {
                    LOGGER.log(WARNING, "Cannot access property " + key + " of " + r.getClass(), e);
                } catch (InvocationTargetException e) {
                    LOGGER.log(WARNING, "Cannot access property " + key + " of " + r.getClass(), e);
                } catch (NoSuchMethodException e) {
                    LOGGER.log(WARNING, "Cannot access property " + key + " of " + r.getClass(), e);
                }
            }
        }

        invokePostConstruct(getWebApp().getMetaClass(r).getPostConstructMethods(), r);

        return r;
    }

    /**
     * Invoke PostConstruct method from the base class to subtypes.
     */
    private void invokePostConstruct(SingleLinkedList<MethodRef> methods, Object r) {
        if (methods.isEmpty())  return;

        invokePostConstruct(methods.tail,r);
        try {
            methods.head.invoke(r);
        } catch (InvocationTargetException e) {
            throw new IllegalArgumentException("Unable to post-construct "+r,e);
        } catch (IllegalAccessException e) {
            throw (Error)new IllegalAccessError().initCause(e);
        }

    }

    /**
     * Gets the type of the field/property designate by the given name.
     */
    private TypePair getPropertyType(Object bean, String name) throws IllegalAccessException, InvocationTargetException {
        try {
            PropertyDescriptor propDescriptor = PropertyUtils.getPropertyDescriptor(bean, name);
            if(propDescriptor!=null) {
                Method m = propDescriptor.getWriteMethod();
                if(m!=null)
                    return new TypePair(m.getGenericParameterTypes()[0], m.getParameterTypes()[0]);
            }
        } catch (NoSuchMethodException e) {
            // no such property
        }

        // try a field
        try {
            return new TypePair(bean.getClass().getField(name));
        } catch (NoSuchFieldException e) {
            // no such field
        }

        return null;
    }

    /**
     * Sets the property/field value of the given name, by performing a value type conversion if necessary.
     */
    private static void copyProperty(Object bean, String name, Object value) throws IllegalAccessException, InvocationTargetException {
        PropertyDescriptor propDescriptor;
        try {
            propDescriptor =
                PropertyUtils.getPropertyDescriptor(bean, name);
        } catch (NoSuchMethodException e) {
            propDescriptor = null;
        }
        if ((propDescriptor != null) &&
            (propDescriptor.getWriteMethod() == null)) {
            propDescriptor = null;
        }
        if (propDescriptor != null) {
            Converter converter = Stapler.lookupConverter(propDescriptor.getPropertyType());
            if (converter != null)
                value = converter.convert(propDescriptor.getPropertyType(), value);
            try {
                PropertyUtils.setSimpleProperty(bean, name, value);
            } catch (NoSuchMethodException e) {
                throw new NoSuchMethodError(e.getMessage());
            }
            return;
        }

        // try a field
        try {
            Field field = bean.getClass().getField(name);
            Converter converter = ConvertUtils.lookup(field.getType());
            if (converter != null)
                value = converter.convert(field.getType(), value);
            field.set(bean,value);
        } catch (NoSuchFieldException e) {
            // no such field
        }
    }

    private void parseMultipartFormData() throws ServletException {
        if(parsedFormData!=null)    return;

        parsedFormData = new HashMap<String,FileItem>();
        ServletFileUpload upload = new ServletFileUpload(new DiskFileItemFactory());
        try {
            for( FileItem fi : (List<FileItem>)upload.parseRequest(this) )
                parsedFormData.put(fi.getFieldName(),fi);
        } catch (FileUploadException e) {
            throw new ServletException(e);
        }
    }

    public JSONObject getSubmittedForm() throws ServletException {
        if(structuredForm==null) {
            String ct = getContentType();
            String p = null;
            boolean isSubmission; // for error diagnosis, if something is submitted, set to true

            if(ct!=null && ct.startsWith("multipart/")) {
                isSubmission=true;
                parseMultipartFormData();
                FileItem item = parsedFormData.get("json");
                if(item!=null) {
                    if (item.getContentType() == null && getCharacterEncoding() != null) {
                        // JENKINS-11543: If client doesn't set charset per part, use request encoding
                        try {
                            p = item.getString(getCharacterEncoding());
                        } catch (java.io.UnsupportedEncodingException uee) {
                            LOGGER.log(WARNING, "Request has unsupported charset, using default for 'json' parameter", uee);
                            p = item.getString();
                        }
                    } else {
                        p = item.getString();
                    }
                }
            } else {
                p = getParameter("json");
                isSubmission = !getParameterMap().isEmpty();
            }
            
            if(p==null || p.length() == 0) {
                // no data submitted
                try {
                    StaplerResponse rsp = Stapler.getCurrentResponse();
                    if(isSubmission)
                        rsp.sendError(SC_BAD_REQUEST,"This page expects a form submission");
                    else
                        rsp.sendError(SC_BAD_REQUEST,"Nothing is submitted");
                    throw new ServletException("This page expects a form submission but had only " + getParameterMap());
                } catch (IOException e) {
                    throw new ServletException(e);
                }
            }
            try {
                structuredForm = JSONObject.fromObject(p);
            } catch (JSONException e) {
                throw new ServletException("Failed to parse JSON:" + p, e);
            }
        }
        return structuredForm;
    }

    public FileItem getFileItem(String name) throws ServletException, IOException {
        parseMultipartFormData();
        if(parsedFormData==null)    return null;
        FileItem item = parsedFormData.get(name);
        if(item==null || item.isFormField())    return null;
        return item;
    }

    private static final Logger LOGGER = Logger.getLogger(RequestImpl.class.getName());
}
