package org.kohsuke.stapler.export;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Writes all the property of one {@link ExportedBean} to {@link DataWriter}.
 *
 * @author Kohsuke Kawaguchi
 */
public class Model<T> {
    /**
     * The class being modeled.
     */
    public final Class<T> type;

    /**
     * {@link Model} for the super class.
     */
    public final Model<? super T> superModel;

    private final Property[] properties;

    /*package*/ final ModelBuilder parent;
    /*package*/ final int defaultVisibility;

    /*package*/ Model(ModelBuilder parent, Class<T> type) {
        this.parent = parent;
        this.type = type;
        ExportedBean eb = type.getAnnotation(ExportedBean.class);
        if(eb ==null)
            throw new IllegalArgumentException(type+" doesn't have @ExposedBean");
        this.defaultVisibility = eb.defaultVisibility();
        
        parent.models.put(type,this);

        Class<? super T> sc = type.getSuperclass();
        if(sc!=null && sc.getAnnotation(ExportedBean.class)!=null)
            superModel = parent.get(sc);
        else
            superModel = null;

        List<Property> properties = new ArrayList<Property>();

        // Use reflection to find out what properties are exposed.
        for( Field f : type.getFields() ) {
            if(f.getDeclaringClass()!=type) continue;
            Exported exported = f.getAnnotation(Exported.class);
            if(exported !=null)
                properties.add(new FieldProperty(this,f, exported));
        }

        for( Method m : type.getMethods() ) {
            if(m.getDeclaringClass()!=type) continue;
            Exported exported = m.getAnnotation(Exported.class);
            if(exported !=null)
                properties.add(new MethodProperty(this,m, exported));
        }

        this.properties = properties.toArray(new Property[properties.size()]);
        Arrays.sort(this.properties);
    }

    /**
     * Gets all the exported properties.
     */
    public List<Property> getProperties() {
        return Collections.unmodifiableList(Arrays.asList(properties));
    }

    /**
     * Writes the property values of the given object to the writer.
     */
    public void writeTo(T object, DataWriter writer) throws IOException {
        writer.startObject();
        writeTo(object,1,writer);
        writer.endObject();
    }

    void writeTo(T object, int depth, DataWriter writer) throws IOException {
        if(superModel !=null)
            superModel.writeTo(object,depth,writer);

        for (Property p : properties)
            p.writeTo(object,depth,writer);
    }
}
