/*
 * Copyright (c) 2022, CloudBees, Inc.
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

import java.lang.reflect.Proxy;

/**
 * Like {@link ClassValue} but for a whole {@link ClassLoader}.
 */
abstract class ClassLoaderValue<T> {

    private final ClassValue<T> storage = new ClassValue<T>() {
        @Override
        protected T computeValue(Class<?> type) {
            return ClassLoaderValue.this.computeValue(type.getClassLoader());
        }
    };

    protected abstract T computeValue(ClassLoader cl);

    // A Class and its ClassLoader strongly refer to one another,
    // so we can delegate to ClassValue if we can supply a Class loaded by a given ClassLoader,
    // which is most easily done by asking for a Proxy of an otherwise unused interface.
    public interface X {}

    public final T get(ClassLoader cl) {
        Class<?> x;
        try {
            x = cl.loadClass(X.class.getName());
            // OK, X is visible to cl, can use trick; note that x != X.class when using PowerMock
        } catch (ClassNotFoundException e) {
            // fallback, no caching; could use WeakHashMap though typically values would strongly hold keys so both
            // could leak
            return computeValue(cl);
        }
        Class<?> type = Proxy.getProxyClass(cl, x);
        assert type.getClassLoader() == cl;
        return storage.get(type);
    }
}
