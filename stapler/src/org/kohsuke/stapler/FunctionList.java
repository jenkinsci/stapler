package org.kohsuke.stapler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Immutable list of {@link Function}s.
 *
 * @author Kohsuke Kawaguchi
 */
final class FunctionList implements Iterable<Function> {
    private final Function[] functions;

    public FunctionList(Function... functions) {
        this.functions = functions;
    }

    public FunctionList(Collection<Function> functions) {
        this.functions = functions.toArray(new Function[0]);
    }

    private FunctionList filter(Filter f) {
        List<Function> r = new ArrayList<Function>();
        for (Function m : functions)
            if (f.keep(m))
                r.add(m);
        return new FunctionList(r.toArray(new Function[0]));
    }

    public Iterator<Function> iterator() {
        return Arrays.asList(functions).iterator();
    }

    //public int length() {
    //    return functions.length;
    //}
    //
    //public Method get(int i) {
    //    return functions[i];
    //}

    public interface Filter {
        boolean keep(Function m);
    }

    /**
     * Returns {@link Function}s that start with the given prefix.
     */
    public FunctionList prefix(final String prefix) {
        return filter(new Filter() {
            public boolean keep(Function m) {
                return m.getName().startsWith(prefix);
            }
        });
    }

    /**
     * Returns {@link Function}s that have the given name.
     */
    public FunctionList name(final String name) {
        return filter(new Filter() {
            public boolean keep(Function m) {
                return m.getName().equals(name);
            }
        });
    }

    /**
     * Returns {@link Function}s that has the given type parameters
     */
    public FunctionList signature(final Class... args) {
        return filter(new Filter() {
            public boolean keep(Function m) {
                return Arrays.equals(m.getParameterTypes(),args);
            }
        });
    }
}
