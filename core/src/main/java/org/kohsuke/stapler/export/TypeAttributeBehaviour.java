package org.kohsuke.stapler.export;

import org.jvnet.tiger_types.Types;

import java.lang.reflect.Type;

/**
 * @author Kohsuke Kawaguchi
 */
public abstract class TypeAttributeBehaviour {
    private final String name;

    // no subtyping outside this package
    private TypeAttributeBehaviour(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return super.toString()+'['+name+']';
    }

    abstract Class map(Type expected, Class actual);

    String print(Type expected, Class actual) {
        return print(map(expected,actual));
    }

    protected String print(Class t) {
        return t==null ? null : t.getName();
    }

    public static final TypeAttributeBehaviour NONE = new TypeAttributeBehaviour("NONE") {
        @Override
        Class map(Type expected, Class actual) {
            return null;
        }
    };

    public static final TypeAttributeBehaviour ALWAYS = new TypeAttributeBehaviour("ALWAYS") {
        @Override
        Class map(Type expected, Class actual) {
            return actual;
        }
    };

    public static final TypeAttributeBehaviour IF_NEEDED = new TypeAttributeBehaviour("IF_NEEDED") {
        @Override
        Class map(Type expected, Class actual) {
            if (actual==null)
                return null;    // nothing to write
            if (expected==actual)
                return null;    // fast pass when we don't need it
            if (expected==null)
                return actual;  // we need to print it
            if (Types.erasure(expected)==actual)
                return null;    // slow pass
            return actual;
        }
    };

    public TypeAttributeBehaviour simple() {
        final TypeAttributeBehaviour outer = this;
        return new TypeAttributeBehaviour(this.name+"+simple") {
            @Override
            Class map(Type expected, Class actual) {
                return outer.map(expected,actual);
            }

            @Override
            protected String print(Class t) {
                return t.getSimpleName();
            }
        };
    }
}
