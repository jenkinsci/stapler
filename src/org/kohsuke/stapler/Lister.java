package org.kohsuke.stapler;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Collection;

abstract class Lister {
    final Class itemType;
    protected final List r = new ArrayList();

    protected Lister(Class itemType) {
        this.itemType = itemType;
    }

    void add(Object o) {
        r.add(o);
    }
    abstract Object toCollection();

    /**
     * @param t
     *      The generified type version of 'c'.
     */
    public static Lister create(Class c, Type t) {
        if(c.isArray()) {
            // array
            return new Lister(c.getComponentType()) {
                Object toCollection() {
                    return r.toArray((Object[])Array.newInstance(itemType,r.size()));
                }
            };
        }
        if(Collection.class.isAssignableFrom(c)) {
            // TODO
        }
        return null;
    }
}
