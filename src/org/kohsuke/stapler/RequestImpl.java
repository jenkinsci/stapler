package org.kohsuke.stapler;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.beanutils.Converter;
import org.apache.commons.beanutils.ConvertUtils;
import org.kohsuke.stapler.jelly.JellyClassTearOff;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.StringTokenizer;
import java.beans.PropertyDescriptor;

/**
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

    private final Stapler stapler;

    // lazily computed
    private String rest;

    private final String originalRequestURI;

    public RequestImpl(Stapler stapler, HttpServletRequest request, List<AncestorImpl> ancestors, TokenList tokens) {
        super(request);
        this.stapler = stapler;
        this.ancestors = ancestors;
        this.tokens = tokens;
        this.originalRequestURI = request.getRequestURI();
    }

    public Stapler getStapler() {
        return stapler;
    }

    public String getRestOfPath() {
        if(rest==null)
            rest = assembleRestOfPath(tokens);
        return rest;
    }

    public ServletContext getServletContext() {
        return stapler.getServletContext();
    }

    private static String assembleRestOfPath(TokenList tokens) {
        StringBuffer buf = new StringBuffer();
        for( int i=tokens.idx; i<tokens.length(); i++ ) {
            buf.append('/');
            buf.append(tokens.get(i));
        }
        return buf.toString();
    }

    public RequestDispatcher getView(Object it,String viewName) throws IOException {
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

        return null;
    }

    public String getRootPath() {
        StringBuffer buf = super.getRequestURL();
        int idx = 0;
        for( int i=0; i<3; i++ )
            idx = buf.substring(idx).indexOf("/")+1;
        buf.setLength(idx-1);
        buf.append(super.getContextPath());
        return buf.toString();
    }

    public List getAncestors() {
        return ancestors;
    }

    public String getOriginalRequestURI() {
        return originalRequestURI;
    }

    public boolean checkIfModified(long lastModified, StaplerResponse rsp) {
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
        rsp.setHeader("Last-Modified",format.format(new Date(lastModified)));
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

        return r;
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
            Converter converter = ConvertUtils.lookup(propDescriptor.getPropertyType());
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
