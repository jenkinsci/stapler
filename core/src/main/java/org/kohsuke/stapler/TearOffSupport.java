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

import java.lang.reflect.InvocationTargetException;

/**
 * Allows "tear-off" objects to be linked to the parent object.
 *
 * <p>
 * This mechanism is used to avoid static linking optional packages,
 * so that stapler can work even when the optional dependencies are missing.
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class TearOffSupport {
    private final ClassValue<Object> tearOffs = new ClassValue<Object>() {
        @Override
        protected Object computeValue(Class<?> type) {
            try {
                return type.getConstructor(TearOffSupport.this.getClass()).newInstance(TearOffSupport.this);
            } catch (InstantiationException e) {
                throw new InstantiationError(e.getMessage());
            } catch (IllegalAccessException e) {
                throw new IllegalAccessError(e.getMessage());
            } catch (InvocationTargetException e) {
                Throwable ex = e.getTargetException();
                if (ex instanceof RuntimeException) {
                    throw (RuntimeException) ex;
                }
                if (ex instanceof Error) {
                    throw (Error) ex;
                }
                throw new Error(e);
            } catch (NoSuchMethodException e) {
                throw new NoSuchMethodError(e.getMessage());
            }
        }
    };

    /**
     * @deprecated Unused? Use {@link #loadTearOff}.
     */
    @Deprecated
    public final <T> T getTearOff(Class<T> t) {
        return loadTearOff(t);
    }

    public final <T> T loadTearOff(Class<T> t) {
        return t.cast(tearOffs.get(t));
    }

    /**
     * @deprecated Unused?
     */
    @Deprecated
    public <T> void setTearOff(Class<T> type, T instance) {
        throw new UnsupportedOperationException();
    }
}
