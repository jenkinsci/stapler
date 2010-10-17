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
import java.util.HashMap;
import java.util.Map;

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
    private volatile Map<Class,Object> tearOffs;

    public final <T> T getTearOff(Class<T> t) {
        Map<Class,Object> m = tearOffs;
        if(m==null)     return null;
        return (T)m.get(t);
    }

    public final <T> T loadTearOff(Class<T> t) {
        T o = getTearOff(t);
        if(o==null) {
            try {
                o = t.getConstructor(getClass()).newInstance(this);
                setTearOff(t,o);
            } catch (InstantiationException e) {
                throw new InstantiationError(e.getMessage());
            } catch (IllegalAccessException e) {
                throw new IllegalAccessError(e.getMessage());
            } catch (InvocationTargetException e) {
                Throwable ex = e.getTargetException();
                if(ex instanceof RuntimeException)
                    throw (RuntimeException)ex;
                if(ex instanceof Error)
                    throw (Error)ex;
                throw new Error(e);
            } catch (NoSuchMethodException e) {
                throw new NoSuchMethodError(e.getMessage());
            }
        }
        return o;
    }

    public synchronized <T> void setTearOff(Class<T> type, T instance) {
        Map<Class,Object> m = tearOffs;
        Map<Class,Object> r = m!=null ? new HashMap<Class, Object>(tearOffs) : new HashMap<Class,Object>();
        r.put(type,instance);
        tearOffs = r;
    }
}
