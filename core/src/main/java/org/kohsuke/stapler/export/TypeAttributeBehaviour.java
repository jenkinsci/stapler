package org.kohsuke.stapler.export;

import org.jvnet.tiger_types.Types;

import java.lang.reflect.Type;

/**
 * @author Kohsuke Kawaguchi
 */
enum TypeAttributeBehaviour {
    NONE, IF_NEEDED, ALWAYS;

    String map(Type expected, Class actual) {
        if (actual==null)   return null;    // nothing to write

        switch (this) {
        case NONE:
            return null;    // shouldn't be writing anything
        case IF_NEEDED:
            if (expected==actual)
                return null;    // fast pass when we don't need it
            if (expected==null)
                return print(actual);
            if (Types.erasure(expected)==actual)
                return null;    // slow pass
            return print(actual);
        case ALWAYS:;
            return print(actual);    // always writing the name
        default:
            throw new AssertionError();
        }
    }

    protected String print(Class t) {
        return t.getName();
    }
}
