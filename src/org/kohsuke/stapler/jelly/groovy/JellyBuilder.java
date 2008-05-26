package org.kohsuke.stapler.jelly.groovy;

import groovy.lang.Closure;
import groovy.lang.GroovyObjectSupport;
import groovy.lang.MissingMethodException;
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
import org.apache.commons.jelly.TagSupport;
import org.apache.commons.jelly.XMLOutput;
import org.apache.commons.jelly.expression.Expression;
import org.apache.commons.jelly.expression.ConstantExpression;
import org.apache.commons.jelly.impl.TextScript;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import java.util.Collections;
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

    public JellyBuilder(JellyContext context,XMLOutput output) {
        this.context = context;
        this.output = output;
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

    @Override
    public Object invokeMethod(String name, Object args) {
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
                } else {
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

        Tag parent = current;

        try {
            Tag t = createTag(name,attributes);
            if (parent != null)
                t.setParent(parent);
            t.setContext(context);
            if(closure!=null) {
                final Closure theClosure = closure;
                t.setBody(new Script() {
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
                });
            } else
            if(innerText!=null)
                t.setBody(new TextScript(innerText));
            else
                t.setBody(NULL_SCRIPT);

            current = t;

            t.doTag(output);
        } catch(JellyException e) {
            throw new RuntimeException(e);  // what's the proper way to handle exceptions in Groovy?
        } finally {
            current = parent;
        }
    }

    private Tag createTag(final QName n, final Map attributes) throws JellyException {
        TagLibrary lib = context.getTagLibrary(n.getNamespaceURI());
        if(lib!=null) {
            Tag t = lib.createTag(n.getLocalPart(), toAttributes(attributes));
            if(t!=null) {
                configureTag(t,attributes);
                return t;
            }
        }

        // otherwise treat it as a literal.
        return new TagSupport() {
            public void doTag(XMLOutput output) throws JellyTagException {
                try {
                    List<Namespace> nsList = (List<Namespace>)InvokerHelper.asList(attributes.get("xmlns"));
                    if(nsList!=null) {
                        for (Namespace ns : nsList)
                            ns.startPrefixMapping(output);
                    }

                    output.startElement(n.getNamespaceURI(), n.getLocalPart(), n.getQualifiedName(), toAttributes(attributes));
                    invokeBody(output);
                    output.endElement(n.getNamespaceURI(), n.getLocalPart(), n.getQualifiedName());

                    if(nsList!=null) {
                        for (Namespace ns : nsList)
                            ns.endPrefixMapping(output);
                    }
                } catch (SAXException e) {
                    throw new JellyTagException(e);
                }
            }
        };
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
     * {@link Script} that does nothing.
     */
    private static final Script NULL_SCRIPT = new Script() {
        public Script compile() {
            return this;
        }

        public void run(JellyContext context, XMLOutput output) {
        }
    };
}
