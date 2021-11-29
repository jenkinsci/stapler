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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Kohsuke Kawaguchi
 */
public class ReflectionUtils {
    /**
     * Given the primitive type, returns the VM default value for that type in a boxed form.
     * For reference types, return null.
     */
    public static Object getVmDefaultValueFor(Class<?> type) {
        return defaultPrimitiveValue.get(type);
    }

    private static final Map<Class,Object> defaultPrimitiveValue = new HashMap<>();
    static {
        defaultPrimitiveValue.put(boolean.class,false);
        defaultPrimitiveValue.put(int.class,0);
        defaultPrimitiveValue.put(long.class,0L);
    }

    /**
     * Merge  two sets of annotations. If both contains the same annotation, the definition
     * in 'b' overrides the definition in 'a' and shows up in the result
     */
    public static Annotation[] union(Annotation[] a, Annotation[] b) {
        // fast path
        if (a.length==0)    return b;
        if (b.length==0)    return a;

        // slow path
        List<Annotation> combined = new ArrayList<>(a.length+b.length);
        combined.addAll(Arrays.asList(a));

        OUTER:
        for (Annotation x : b) {
            for (int i=0; i<a.length; i++) {
                if (x.annotationType()==combined.get(i).annotationType()) {
                    combined.set(i,x);  // override
                    continue OUTER;
                }
            }
            // not overlapping. append
            combined.add(x);
        }

        return combined.toArray(new Annotation[0]);
    }

}
