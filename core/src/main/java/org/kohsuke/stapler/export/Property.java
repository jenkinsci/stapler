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

package org.kohsuke.stapler.export;

import org.kohsuke.stapler.export.TreePruner.ByDepth;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.lang.reflect.Array;
import java.net.URL;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Exposes one {@link Exported exposed property} of {@link ExportedBean} to
 * {@link DataWriter}.
 *  
 * @author Kohsuke Kawaguchi
 */
public abstract class Property implements Comparable<Property> {
    /**
     * Name of the property.
     */
    public final String name;
    final ModelBuilder owner;
    /**
     * Visibility depth level of this property.
     *
     * @see Exported#visibility()
     */
    public final int visibility;

    /**
     * Model to which this property belongs to.
     * Never null.
     */
    public final Model parent;

    /**
     * @see Exported#inline()
     */
    public final boolean inline;

    private String[] verboseMap;

    Property(Model parent, String name, Exported exported) {
        this.parent = parent;
        this.owner = parent.parent;
        this.name = exported.name().length()>1 ? exported.name() : name;
        int v = exported.visibility();
        if(v==0)
            v = parent.defaultVisibility;
        this.visibility = v;
        this.inline = exported.inline();
        String[] s = exported.verboseMap().split("/");
        if (s.length<2)
            this.verboseMap = null;
        else
            this.verboseMap = s;
    }

    public int compareTo(Property that) {
        return this.name.compareTo(that.name);
    }

    public abstract Type getGenericType();
    public abstract Class getType();

    /**
     * Gets the associated javadoc, if any, or null.
     */
    public abstract String getJavadoc();

    /**
     * Writes one property of the given object to {@link DataWriter}.
     *
     * @param pruner
     *      Determines how to prune the object graph tree.
     */
    public void writeTo(Object object, TreePruner pruner, DataWriter writer) throws IOException {
        TreePruner child = pruner.accept(object, this);
        if (child==null)        return;

        try {
            writer.name(name);
            writeValue(getValue(object),child,writer);
        } catch (IllegalAccessException e) {
            IOException x = new IOException("Failed to write " + name);
            x.initCause(e);
            throw x;
        } catch (InvocationTargetException e) {
            IOException x = new IOException("Failed to write " + name);
            x.initCause(e);
            throw x;
        }
    }

    /**
     * @deprecated as of 1.139
     */
    public void writeTo(Object object, int depth, DataWriter writer) throws IOException {
        writeTo(object,new ByDepth(depth),writer);
    }

    /**
     * Writes one value of the property to {@link DataWriter}.
     */
    private void writeValue(Object value, TreePruner pruner, DataWriter writer) throws IOException {
        writeValue(value,pruner,writer,false);
    }

    /**
     * Writes one value of the property to {@link DataWriter}.
     */
    private void writeValue(Object value, TreePruner pruner, DataWriter writer, boolean skipIfFail) throws IOException {
        if(value==null) {
            writer.valueNull();
            return;
        }

        if(value instanceof CustomExportedBean) {
            writeValue(((CustomExportedBean)value).toExportedObject(),pruner,writer);
            return;
        }

        Class c = value.getClass();

        if(STRING_TYPES.contains(c)) {
            writer.value(value.toString());
            return;
        }
        if(PRIMITIVE_TYPES.contains(c)) {
            writer.valuePrimitive(value);
            return;
        }
        if(c.getComponentType()!=null) { // array
            Range r = pruner.getRange();
            writer.startArray();
            if (value instanceof Object[]) {
                // typical case
                for (Object item : r.apply((Object[]) value)) {
                    writeValue(item,pruner,writer,true);
                }
            } else {
                // more generic case
                int len = Math.min(r.max,Array.getLength(value));
                for (int i=r.min; i<len; i++) {
                    writeValue(Array.get(value,i),pruner,writer,true);
                }
            }
            writer.endArray();
            return;
        }
        if(value instanceof Collection) {
            writer.startArray();
            for (Object item : pruner.getRange().apply((Collection) value)) {
                writeValue(item,pruner,writer,true);
            }
            writer.endArray();
            return;
        }
        if(value instanceof Map) {
            if (verboseMap!=null) {// verbose form
                writer.startArray();
                for (Map.Entry e : ((Map<?,?>) value).entrySet()) {
                    writer.startObject();
                    writer.name(verboseMap[0]);
                    writeValue(e.getKey(),pruner,writer);
                    writer.name(verboseMap[1]);
                    writeValue(e.getValue(),pruner,writer);
                    writer.endObject();
                }
                writer.endArray();
            } else {// compact form
                writer.startObject();
                for (Map.Entry e : ((Map<?,?>) value).entrySet()) {
                    writer.name(e.getKey().toString());
                    writeValue(e.getValue(),pruner,writer);
                }
                writer.endObject();
            }
            return;
        }
        if(value instanceof Date) {
            writer.valuePrimitive(((Date) value).getTime());
            return;
        }
        if(value instanceof Calendar) {
            writer.valuePrimitive(((Calendar) value).getTimeInMillis());
            return;
        }
        if(value instanceof Enum) {
            writer.value(value.toString());
            return;
        }

        // otherwise handle it as a bean
        writer.startObject();
        Model model = null;
        try {
            model = owner.get(c, parent.type, name);
        } catch (NotExportableException e) {
            if (skipIfFail) {
                Logger.getLogger(Property.class.getName()).log(Level.FINE, e.getMessage());
            } else {
                throw e;
            }
            // otherwise ignore this error by writing empty object
        }
        if(model!=null)
            model.writeNestedObjectTo(value, pruner, writer, Collections.<String>emptySet());
        writer.endObject();
    }

    /**
     * Gets the value of this property from the bean.
     */
    protected abstract Object getValue(Object bean) throws IllegalAccessException, InvocationTargetException;

    /*package*/ static final Set<Class> STRING_TYPES = new HashSet<Class>(Arrays.asList(
        String.class,
        URL.class
    ));

    /*package*/ static final Set<Class> PRIMITIVE_TYPES = new HashSet<Class>(Arrays.asList(
        Integer.class,
        Long.class,
        Boolean.class,
        Short.class,
        Character.class,
        Float.class,
        Double.class
    ));
}
