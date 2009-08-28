package org.kohsuke.stapler.export;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.lang.reflect.Array;
import java.net.URL;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
    private final boolean inline;

    Property(Model parent, String name, Exported exported) {
        this.parent = parent;
        this.owner = parent.parent;
        this.name = exported.name().length()>1 ? exported.name() : name;
        int v = exported.visibility();
        if(v==0)
            v = parent.defaultVisibility;
        this.visibility = v;
        this.inline = exported.inline();
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
     * @param depth
     *      The current level of object nesting that the writer is in.
     *      If it's beyond a certain cut-off, the property won't be written
     *      and this method becomes no-op.
     */
    public void writeTo(Object object, int depth, DataWriter writer) throws IOException {
        if(visibility<depth)    return; // not visible

        try {
            writer.name(name);
            writeValue(getValue(object),depth-(inline?1:0),writer);
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
     * Writes one value of the property to {@link DataWriter}.
     */
    private void writeValue(Object value, int depth, DataWriter writer) throws IOException {
        writeValue(value,depth,writer,false);
    }

    /**
     * Writes one value of the property to {@link DataWriter}.
     */
    private void writeValue(Object value, int depth, DataWriter writer, boolean skipIfFail) throws IOException {
        if(value==null) {
            writer.valueNull();
            return;
        }

        if(value instanceof CustomExportedBean) {
            writeValue(((CustomExportedBean)value).toExportedObject(),depth,writer);
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
            writer.startArray();
            if (value instanceof Object[]) {
                // typical case
                for (Object item : (Object[]) value)
                    writeValue(item,depth,writer,true);
            } else {
                // more generic case
                int len = Array.getLength(value);
                for (int i=0; i<len; i++)
                    writeValue(Array.get(value,i),depth,writer,true);
            }
            writer.endArray();
            return;
        }
        if(value instanceof Collection) {
            writer.startArray();
            for (Object item : (Collection) value)
                writeValue(item,depth,writer,true);
            writer.endArray();
            return;
        }
        if(value instanceof Map) {
            writer.startObject();
            for (Map.Entry e : ((Map<?,?>) value).entrySet()) {
                writer.name(e.getKey().toString());
                writeValue(e.getValue(),depth,writer);
            }
            writer.endObject();
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
            model = owner.get(c);
        } catch (NotExportableException e) {
            if(!skipIfFail)
                throw e;
            // otherwise ignore this error by writing empty object
        }
        if(model!=null)
            model.writeNestedObjectTo(value,depth+1,writer);
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
