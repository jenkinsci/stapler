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

package org.kohsuke.stapler;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.*;
import org.kohsuke.stapler.annotations.StaplerMethod;
import org.kohsuke.stapler.annotations.StaplerPath;
import org.kohsuke.stapler.annotations.StaplerRMI;

/**
 * Immutable list of {@link Function}s.
 *
 * @author Kohsuke Kawaguchi
 */
public final class FunctionList extends AbstractList<Function> {
    private static final FunctionList EMPTY = new FunctionList();
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

    @Override
    public Function get(int index) {
        return functions[index];
    }

    public int size() {
        return functions.length;
    }

    /**
     * Compute set unions of two lists.
     */
    public FunctionList union(FunctionList that) {
        Set<Function> combined = new LinkedHashSet<Function>();
        combined.addAll(Arrays.asList(this.functions));
        combined.addAll(Arrays.asList(that.functions));
        return new FunctionList(combined);
    }

    public static FunctionList emptyList() {
        return EMPTY;
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
     * Returns {@link Function}s that are annotated with the given annotation.
     */
    public FunctionList annotated(final Class<? extends Annotation> ann) {
        return filter(new Filter() {
            public boolean keep(Function m) {
                return m.getAnnotation(ann)!=null;
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
                return Arrays.equals(m.getParameterTypes(), args);
            }
        });
    }

    /**
     * Returns {@link Function}s that are either explicitly {@link WebMethod} or
     * implicitly so (by having its name start with 'do')
     */
    public FunctionList webMethods() {
        return filter(new Filter() {
            public boolean keep(Function m) {
                return m.getName().startsWith("do") || m.getAnnotation(WebMethod.class)!=null;
            }
        });
    }

    /**
     * Returns {@link Function}s that have {@link StaplerPath.Helper#isPath(Function)} true.
     */
    public FunctionList staplerPath() {
        return filter(new Filter() {
            @Override
            public boolean keep(Function m) {
                return StaplerPath.Helper.isPath(m);
            }
        });
    }

    /**
     * Returns {@link Function}s that have {@link StaplerPath.Helper#isPath(Function)} false.
     */
    public FunctionList nonStaplerPath() {
        return filter(new Filter() {
            @Override
            public boolean keep(Function m) {
                return !StaplerPath.Helper.isPath(m);
            }
        });
    }

    /**
     * Returns {@link Function}s that have {@link StaplerMethod.Helper#isMethod(Function)} true.
     */
    public FunctionList staplerMethod() {
        return filter(new Filter() {
            @Override
            public boolean keep(Function m) {
                return StaplerMethod.Helper.isMethod(m);
            }
        });
    }

    /**
     * Returns {@link Function}s that have {@link StaplerMethod.Helper#isMethod(Function)} false.
     */
    public FunctionList nonStaplerMethod() {
        return filter(new Filter() {
            @Override
            public boolean keep(Function m) {
                return !StaplerMethod.Helper.isMethod(m);
            }
        });
    }

    /**
     * Returns {@link Function}s that have {@link StaplerRMI}.
     */
    public FunctionList staplerRmi() {
        return filter(new Filter() {
            @Override
            public boolean keep(Function m) {
                return m.getAnnotation(StaplerRMI.class) != null;
            }
        });
    }

    /**
     * Returns {@link Function}s that do not have {@link StaplerRMI}.
     */
    public FunctionList nonStaplerRmi() {
        return filter(new Filter() {
            @Override
            public boolean keep(Function m) {
                return m.getAnnotation(StaplerRMI.class) == null;
            }
        });
    }

    /**
     * Returns {@link Function}s that has the parameters
     * that start with given types (but can have additional parameters.)
     */
    public FunctionList signatureStartsWith(final Class... args) {
        return filter(new Filter() {
            public boolean keep(Function m) {
                Class[] params = m.getParameterTypes();
                if(params.length<args.length)  return false;
                for( int i=0; i<args.length; i++ ) {
                    if(params[i]!=args[i])
                        return false;
                }
                return true;
            }
        });
    }
}
