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

package org.kohsuke.stapler.jelly.groovy;

import groovy.lang.Closure;
import groovy.lang.GroovyObjectSupport;
import groovy.lang.MissingMethodException;
import groovy.lang.MissingPropertyException;
import groovy.xml.QName;
import org.apache.commons.beanutils.ConvertingWrapDynaBean;
import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.DynaProperty;
import org.apache.commons.jelly.DynaTag;
import org.apache.commons.jelly.JellyContext;
import org.apache.commons.jelly.JellyException;
import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.Script;
import org.apache.commons.jelly.Tag;
import org.apache.commons.jelly.TagLibrary;
import org.apache.commons.jelly.XMLOutput;
import org.apache.commons.jelly.expression.ConstantExpression;
import org.apache.commons.jelly.expression.Expression;
import org.apache.commons.jelly.impl.TagScript;
import org.apache.commons.jelly.impl.TextScript;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXContentHandler;
import org.kohsuke.stapler.MetaClassLoader;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.framework.adjunct.AdjunctManager;
import org.kohsuke.stapler.framework.adjunct.AdjunctsInPage;
import org.kohsuke.stapler.framework.adjunct.NoSuchAdjunctException;
import org.kohsuke.stapler.jelly.CustomTagLibrary;
import org.kohsuke.stapler.jelly.JellyClassLoaderTearOff;
import org.kohsuke.stapler.jelly.JellyClassTearOff;
import org.kohsuke.stapler.lang.Klass;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import javax.servlet.ServletContext;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Drive Jelly scripts from Groovy markup.
 *
 * @author Kohsuke Kawaguchi
 */
public final class JellyBuilder extends GroovyObjectSupport {
    /**
     * Current {@link XMLOutput}.
     */
    private XMLOutput output;

    /**
     * Current {@link Tag} in which we are executing.
     */
    private Tag current;

    private JellyContext context;

    private final Map<Class,GroovyClosureScript> taglibs = new HashMap<Class,GroovyClosureScript>();

    private final StaplerRequest request;
    private StaplerResponse response;
    private String rootURL;
    private final AdjunctManager adjunctManager;

    /**
     * Cached {@Link AttributesImpl} instance.
     */
    private final AttributesImpl attributes = new AttributesImpl();

    public JellyBuilder(JellyContext context,XMLOutput output) {
        this.context = context;
        this.output = output;
        this.request = Stapler.getCurrentRequest();
        this.adjunctManager = AdjunctManager.get(request.getServletContext());
    }

    /**
     * This is used to allow QName to be used for the invocation.
     */
    public Namespace namespace(String nsUri, String prefix) {
        return new Namespace(this,nsUri,prefix);
    }

    public Namespace namespace(String nsUri) {
        return namespace(nsUri,null);
    }

    public <T extends TypedTagLibrary> T namespace(Class<T> type) {
        TagLibraryUri a = type.getAnnotation(TagLibraryUri.class);
        if (a==null)    throw new IllegalArgumentException(type+" doesn't have @TagLibraryUri annotation");

        return namespace(a.value(),null).createInvoker(type);
    }

    public XMLOutput getOutput() {
        return output;
    }

    public JellyContext getContext() {
        return context;
    }

    /**
     * Includes another view.
     */
    public void include(Object it, String view) throws IOException, JellyException {
        _include(it,request.getWebApp().getKlass(it),view);
    }

    /**
     * Includes another view.
     */
    public void include(Class clazz, String view) throws IOException, JellyException {
        _include(null,Klass.java(clazz),view);
    }

    private void _include(Object it, Klass clazz, String view) throws IOException, JellyException {
        JellyClassTearOff t = request.getWebApp().getMetaClass(clazz).loadTearOff(JellyClassTearOff.class);
        Script s = t.findScript(view);
        if(s==null)
            throw new IllegalArgumentException("No such view: "+view+" for "+clazz);

        JellyContext context = new JellyContext(getContext());
        if(it!=null)
            context.setVariable("it",it);
        context.setVariable("from", it);

        ClassLoader old = Thread.currentThread().getContextClassLoader();
        if (clazz.clazz instanceof Class)
            Thread.currentThread().setContextClassLoader(((Class)clazz.clazz).getClassLoader());
        try {
            s.run(context,output);
        } finally {
            Thread.currentThread().setContextClassLoader(old);
        }
    }


    public Object methodMissing(String name, Object args) {
        doInvokeMethod(new QName("",name), args);
        return null;
    }

    @SuppressWarnings({"ChainOfInstanceofChecks"})
    protected void doInvokeMethod(QName name, Object args) {
        List list = InvokerHelper.asList(args);

        Map attributes = Collections.EMPTY_MAP;
        Closure closure = null;
        String innerText = null;

        // figure out what parameters are what
        switch (list.size()) {
        case 0:
            break;
        case 1: {
            Object object = list.get(0);
            if (object instanceof Map) {
                attributes = (Map) object;
            } else if (object instanceof Closure) {
                closure = (Closure) object;
                break;
            } else {
                if (object!=null)
                    innerText = object.toString();
            }
            break;
        }
        case 2: {
            Object object1 = list.get(0);
            Object object2 = list.get(1);
            if (object1 instanceof Map) {
                attributes = (Map) object1;
                if (object2 instanceof Closure) {
                    closure = (Closure) object2;
                } else
                if(object2!=null) {
                    innerText = object2.toString();
                }
            } else {
                innerText = object1.toString();
                if (object2 instanceof Closure) {
                    closure = (Closure) object2;
                } else if (object2 instanceof Map) {
                    attributes = (Map) object2;
                } else {
                    throw new MissingMethodException(name.toString(), getClass(), list.toArray());
                }
            }
            break;
        }
        case 3: {
            Object arg0 = list.get(0);
            Object arg1 = list.get(1);
            Object arg2 = list.get(2);
            if (arg0 instanceof Map && arg2 instanceof Closure) {
                closure = (Closure) arg2;
                attributes = (Map) arg0;
                innerText = arg1.toString();
            } else if (arg1 instanceof Map && arg2 instanceof Closure) {
                closure = (Closure) arg2;
                attributes = (Map) arg1;
                innerText = arg0.toString();
            } else {
                throw new MissingMethodException(name.toString(), getClass(), list.toArray());
            }
            break;
        }
        default:
            throw new MissingMethodException(name.toString(), getClass(), list.toArray());
        }

        if (isTag(name)) {// bridge to other Jelly tags
            try {
               TagScript tagScript = createTagScript(name, attributes);
                if (tagScript!=null) {
                    Script body = NULL_SCRIPT;

                    if(closure!=null) {
                        final Closure theClosure = closure;
                        body = new Script() {
                            public Script compile() throws JellyException {
                                return this;
                            }

                            public void run(JellyContext context, XMLOutput output) throws JellyTagException {
                                JellyContext oldc = setContext(context);
                                XMLOutput oldo = setOutput(output);
                                try {
                                    theClosure.setDelegate(JellyBuilder.this);
                                    theClosure.call();
                                } finally {
                                    setContext(oldc);
                                    setOutput(oldo);
                                }
                            }
                        };
                    } else
                    if(innerText!=null)
                        body = new TextScript(innerText);

                    tagScript.setTagBody(body);
                    tagScript.run(context,output);
                    return;
                }
            } catch(JellyException e) {
                throw new RuntimeException(e);
            }
        }

        // static tag
        this.attributes.clear();
        for (Entry e : ((Map<?,?>)attributes).entrySet()) {
            Object v = e.getValue();
            if(v==null) continue;
            String attName = e.getKey().toString();
            this.attributes.addAttribute("",attName,attName,"CDATA", v.toString());
        }
        try {
            output.startElement(name.getNamespaceURI(),name.getLocalPart(),name.getQualifiedName(),this.attributes);
            if(closure!=null) {
                closure.setDelegate(this);
                closure.call();
            }
            if(innerText!=null)
            text(innerText);
            output.endElement(name.getNamespaceURI(),name.getLocalPart(),name.getQualifiedName());
        } catch (SAXException e) {
            throw new RuntimeException(e);  // what's the proper way to handle exceptions in Groovy?
        }
    }

    /**
     * Is this a static XML tag that we just generate, or
     * a jelly tag that needs evaluation?
     */
    private boolean isTag(QName name) {
        return name.getNamespaceURI().length()>0;
    }

    /**
     * Create a tag script if the given QName is a taglib invocation, or return null
     * to handle it like a literal static tag.
     */
    private TagScript createTagScript(QName n, Map<?, ?> attributes) throws JellyException {
        TagLibrary lib = context.getTagLibrary(n.getNamespaceURI());
        if(lib!=null) {
            String localName = n.getLocalPart();

            TagScript tagScript = lib.createTagScript(localName, null/*this parameter appears to be unused.*/);
            if (tagScript==null)    tagScript = lib.createTagScript(localName.replace('_','-'), null);

            if (tagScript!=null) {
                if (attributes != null) {
                    for (Entry e : attributes.entrySet()) {
                        Object v = e.getValue();
                        if (v!=null)
                            tagScript.addAttribute(e.getKey().toString(), new ConstantExpression(v));
                    }
                }

                return tagScript;
            }
        }

        // otherwise treat it as a literal.
        return null;
    }

    /*
     * Copyright 2002,2004 The Apache Software Foundation.
     *
     * Licensed under the Apache License, Version 2.0 (the "License");
     * you may not use this file except in compliance with the License.
     * You may obtain a copy of the License at
     *
     *      http://www.apache.org/licenses/LICENSE-2.0
     *
     * Unless required by applicable law or agreed to in writing, software
     * distributed under the License is distributed on an "AS IS" BASIS,
     * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     * See the License for the specific language governing permissions and
     * limitations under the License.
     */
    private void configureTag(Tag tag, Map attributes) throws JellyException {
        if ( tag instanceof DynaTag ) {
            DynaTag dynaTag = (DynaTag) tag;

            for (Object o : attributes.entrySet()) {
                Entry entry = (Entry) o;
                String name = (String) entry.getKey();
                if(name.equals("xmlns"))    continue;   // we'll process this by ourselves


                Object value = getValue(entry, dynaTag.getAttributeType(name));
                dynaTag.setAttribute(name, value);
            }
        } else {
            // treat the tag as a bean
            DynaBean dynaBean = new ConvertingWrapDynaBean( tag );
            for (Object o : attributes.entrySet()) {
                Entry entry = (Entry) o;
                String name = (String) entry.getKey();
                if(name.equals("xmlns"))    continue;   // we'll process this by ourselves

                DynaProperty property = dynaBean.getDynaClass().getDynaProperty(name);
                if (property == null) {
                    throw new JellyException("This tag does not understand the '" + name + "' attribute");
                }

                dynaBean.set(name, getValue(entry,property.getType()));
            }
        }
    }

    /**
     * Obtains the value from the map entry in the right type.
     */
    private Object getValue(Entry entry, Class type) {
        Object value = entry.getValue();
        if (type== Expression.class)
            value = new ConstantExpression(entry.getValue());
        return value;
    }

    private Attributes toAttributes(Map attributes) {
        AttributesImpl atts = new AttributesImpl();
        for (Object o : attributes.entrySet()) {
            Entry e = (Entry) o;
            if(e.getKey().toString().equals("xmlns"))   continue;   // we'll process them outside attributes
            atts.addAttribute("", e.getKey().toString(), e.getKey().toString(), null, e.getValue().toString());
        }
        return atts;
    }


    JellyContext setContext(JellyContext newValue) {
        JellyContext old = context;
        context = newValue;
        return old;
    }

    public XMLOutput setOutput(XMLOutput newValue) {
        XMLOutput old = output;
        output = newValue;
        return old;
    }

    /**
     * Executes the closure with the specified {@link XMLOutput}.
     */
    public Object with(XMLOutput out, Closure c) {
        XMLOutput old = setOutput(out);
        try {
            c.setDelegate(this);
            return c.call();
        } finally {
            setOutput(old);
        }
    }

    /**
     * Captures the XML fragment generated by the given closure into dom4j DOM tree
     * and return the root element.
     *
     * @return null
     *      if nothing was generated.
     */
    public Element redirectToDom(Closure c) {
        SAXContentHandler sc = new SAXContentHandler();
        with(new XMLOutput(sc),c);
        return sc.getDocument().getRootElement();
    }

    /**
     * Allows values from {@link JellyContext} to be read
     * like global variables. These includes 'request', 'response', etc.
     *
     * @see JellyClassTearOff
     */
    public Object getProperty(String property) {
        try {
            return super.getProperty(property);
        } catch (MissingPropertyException e) {
            Object r = context.getVariableWithDefaultValue(property,MISSING);
            if (r==MISSING) throw e;
            return r;
        }
    }

    /**
     * Sets the value to {@link JellyContext} (typically as a pre-cursor to calling into Jelly tags.)
     */
    public void set(String var, Object value) {
        context.setVariable(var,value);
    }
    
    /**
     * Gets the "it" object.
     *
     * In Groovy "it" is reserved word with a specific meaning,
     * so instead use "my" as the word.
     */
    public Object getMy() {
        return context.getVariable("it");
    }

    /**
     * Writes PCDATA.
     *
     * <p>
     * Any HTML unsafe characters in the string representation of the given object is
     * properly escaped.
     *
     * @see #raw(Object) 
     */
    public void text(Object o) throws SAXException {
        if (o!=null)
            output.write(escape(o.toString()));
    }

    /**
     * Generates HTML fragment from string.
     *
     * <p>
     * The string representation of the object is assumed to produce proper HTML.
     * No further escaping is performed.
     *
     * @see #text(Object)
     */
    public void raw(Object o) throws SAXException {
        if (o!=null)
            output.write(o.toString());
    }

    private String escape(String v) {
        StringBuffer buf = new StringBuffer(v.length()+64);
        for( int i=0; i<v.length(); i++ ) {
            char ch = v.charAt(i);
            if(ch=='<')
                buf.append("&lt;");
            else
            if(ch=='>')
                buf.append("&gt;");
            else
            if(ch=='&')
                buf.append("&amp;");
            else
                buf.append(ch);
        }
        return buf.toString();
    }

    /**
     * {@link Script} that does nothing.
     */
    private static final Script NULL_SCRIPT = new Script() {
        public Script compile() {
            return this;
        }

        public void run(JellyContext context, XMLOutput output) {
        }
    };

    /**
     * Loads a Groovy tag lib instance.
     *
     * <p>
     * A groovy tag library is really just a script class with bunch of method definitions,
     * without any explicit class definition. Such a class is loaded as a subtype of
     * {@link GroovyClosureScript} so that it can use this builder as the delegation target.
     *
     * <p>
     * This method instanciates the class (if not done so already for this request),
     * and return it.
     */
    public Object taglib(Class type) throws IllegalAccessException, InstantiationException, IOException, SAXException {
        GroovyClosureScript o = taglibs.get(type);
        if(o==null) {
            o = (GroovyClosureScript) type.newInstance();
            o.setDelegate(this);
            taglibs.put(type,o);

            adjunct(type.getName());
        }
        return o;
    }

    /**
     * Includes the specified adjunct.
     *
     * This method is useful for including adjunct dynamically on demand.
     */
    public void adjunct(String name) throws IOException, SAXException {
        try {
            AdjunctsInPage aip = AdjunctsInPage.get();
            aip.generate(output,name);
        } catch (NoSuchAdjunctException e) {
            // that's OK.
        }
    }

    /**
     * Loads a jelly tag library.
     *
     * @param t
     *      If this is a subtype of {@link TagLibrary}, then that tag library is loaded and bound to the
     *      {@link Namespace} object, which you can later use to call tags.
     *      Otherwise, t has to have 'taglib' file in the resource and sibling "*.jelly" files will be treated
     *      as tag files. 
     */
    public Namespace jelly(Class t) {
        String n = t.getName();

        if(TagLibrary.class.isAssignableFrom(t)) {
            // if the given class is a tag library itself, just record it
            context.registerTagLibrary(n,n);
        } else {
            String path = n.replace('.', '/');
            URL res = t.getClassLoader().getResource(path+"/taglib");
            if(res!=null) {
                // this class itself is not a tag library, but it contains Jelly side files that are tag files.
                // (some of them might be views, but some of them are tag files, anyway.
                JellyContext parseContext = MetaClassLoader.get(t.getClassLoader()).loadTearOff(JellyClassLoaderTearOff.class).createContext();

                context.registerTagLibrary(n,
                    new CustomTagLibrary(parseContext, t.getClassLoader(), n, path));
            } else {
                throw new IllegalArgumentException("Cannot find taglib from "+t);
            }
        }

        return new Namespace(this,n,"-"); // doesn't matter what the prefix is, since they are known to be taglibs
    }

    public ServletContext getServletContext() {
        return getRequest().getServletContext();
    }

    public StaplerRequest getRequest() {
        return request;
    }

    public StaplerResponse getResponse() {
        if(response==null)
            response = Stapler.getCurrentResponse();
        return response;
    }

    public JellyBuilder getBuilder() {
        return this;
    }

    /**
     * Gets the absolute URL to the top of the webapp.
     *
     * @see StaplerRequest#getContextPath()
     */
    public String getRootURL() {
        if(rootURL==null)
            rootURL = getRequest().getContextPath();
        return rootURL;
    }

    /**
     * Generates an {@code <IMG>} tag to the resource.
     */
    public void img(Object base, String localName) throws SAXException {
        output.write(
            "<IMG src='"+res(base,localName)+"'>");
    }

    /**
     * Yields a URL to the given resource.
     *
     * @param base
     *      The base class/object for which the 'localName' parameter
     *      is resolved from. If this is class, 'localName' is assumed
     *      to be a resource of this class. If it's other objects,
     *      'localName' is assumed to be a resource of the class of this object.
     */
    public String res(Object base, String localName) {
        Class c;
        if (base instanceof Class)
            c = (Class) base;
        else
            c = base.getClass();
        return adjunctManager.rootURL+'/'+c.getName().replace('.','/')+'/'+localName;
    }

    private static final Object MISSING = new Object();
}
