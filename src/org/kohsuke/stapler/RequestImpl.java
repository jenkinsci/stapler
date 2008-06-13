package org.kohsuke.stapler;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.Converter;
import org.apache.commons.beanutils.PropertyUtils;
import org.jvnet.tiger_types.Lister;
import org.kohsuke.stapler.jelly.JellyClassTearOff;
import org.kohsuke.stapler.jelly.groovy.GroovyClassTearOff;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

/**
 * {@link StaplerRequest} implementation.
 * 
 * @author Kohsuke Kawaguchi
 */
class RequestImpl extends HttpServletRequestWrapper implements StaplerRequest {
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

    private final Stapler stapler;

    private final String originalRequestURI;

    public RequestImpl(Stapler stapler, HttpServletRequest request, List<AncestorImpl> ancestors, TokenList tokens) {
        super(request);
        this.stapler = stapler;
        this.ancestors = ancestors;
        this.ancestorsView = Collections.<Ancestor>unmodifiableList(ancestors);
        this.tokens = tokens;
        this.originalRequestURI = request.getRequestURI();
    }

    public Stapler getStapler() {
        return stapler;
    }

    public String getRestOfPath() {
        return tokens.assembleRestOfPath();
    }

    public ServletContext getServletContext() {
        return stapler.getServletContext();
    }

    public RequestDispatcher getView(Object it,String viewName) throws IOException {
        // TODO: this hard-coding totally sucks

        // check JSP view first
        RequestDispatcher rd = stapler.getResourceDispatcher(it, viewName);
        if(rd!=null)    return rd;

        // then Jelly view
        try {
            rd = MetaClass.get(it.getClass()).loadTearOff(JellyClassTearOff.class).createDispatcher(it,viewName);
            if(rd!=null)
                return rd;
        } catch (LinkageError e) {
            // jelly not present
        }

        // then Groovy view
        try {
            rd = MetaClass.get(it.getClass()).loadTearOff(GroovyClassTearOff.class).createDispatcher(it,viewName);
            if(rd!=null)
                return rd;
        } catch (LinkageError e) {
            // jelly not present
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
            loadConstructorParamNames(type);
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
        String[] names = loadConstructorParamNames(type);

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
        String[] names = loadConstructorParamNames(type);

        // the actual arguments to invoke the constructor with.
        Object[] args = new Object[names.length];

        // constructor
        Constructor<T> c = findConstructor(type, names.length);
        Class[] types = c.getParameterTypes();
        Type[] genTypes = c.getGenericParameterTypes();

        // convert parameters
        for( int i=0; i<names.length; i++ ) {
            args[i] = convertJSON(src.get(names[i]),types[i],genTypes[i]);
        }

        return invokeConstructor(c, args);
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

    private Object convertJSON(Object o, Class target, Type genericType) {
        if(o==null)     return null;

        Lister l = Lister.create(target,genericType);

        if (o instanceof JSONObject) {
            JSONObject j = (JSONObject) o;

            if(l==null) {
                // single value conversion
                return bindJSON(target,j);
            } else {
                // only one value given to the collection
                l.add(convertJSON(j,l.itemType,l.itemGenericType));
                return l.toCollection();
            }
        }
        if (o instanceof JSONArray) {
            JSONArray a = (JSONArray) o;
            for (Object item : a)
                l.add(convertJSON(item,l.itemType,l.itemGenericType));
            return l.toCollection();
        }

        Converter converter = Stapler.lookupConverter(target);
        if (converter==null)
            throw new IllegalArgumentException("Unable to convert to "+target);

        return converter.convert(target,o);
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

    /**
     * Loads the property file and determines the constructor parameter names.
     */
    private String[] loadConstructorParamNames(Class<?> type) {
        String resourceName = type.getName().replace('.', '/').replace('$','/') + ".stapler";
        InputStream s = type.getClassLoader().getResourceAsStream(resourceName);
        if(s==null)
            throw new NoStaplerConstructorException(
                "Unable to find "+resourceName+". "+
                "Have you put @DataBoundConstructor javadoc tag on a constructor?");

        try {
            Properties p = new Properties();
            p.load(s);
            s.close();

            String v = p.getProperty("constructor");
            if(v.length()==0)   return new String[0];
            return v.split(",");
        } catch (IOException e) {
            throw new IllegalArgumentException("Unable to load "+resourceName,e);
        }
    }

    private static void fill(Object bean, String key, String value) {
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
}
