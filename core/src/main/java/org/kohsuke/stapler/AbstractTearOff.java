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

import java.net.URL;

/**
 * Partial default implementation of tear-off class, for convenience of derived classes.
 *
 * @param <CLT>
 *      ClassLoader tear-off.
 * @author Kohsuke Kawaguchi
 */
public abstract class AbstractTearOff<CLT,S,E extends Exception> extends CachingScriptLoader<S,E> {
    protected final MetaClass owner;
    protected final CLT classLoader;

    protected AbstractTearOff(MetaClass owner, Class<CLT> cltClass) {
        this.owner = owner;
        if(owner.classLoader!=null)
            classLoader = owner.classLoader.loadTearOff(cltClass);
        else
            classLoader = null;
    }

    /**
     * Default file extension of this kind of scripts, such as ".jelly"
     */
    protected abstract String getDefaultScriptExtension();

    /**
     * Loads the script just from the target class without considering inherited scripts
     * from its base types.
     */
    public S resolveScript(String name) throws E {
        if (name.lastIndexOf('.')<=name.lastIndexOf('/'))   // no file extension provided
            name += getDefaultScriptExtension();

        URL res = getResource(name);
        if(res==null) {
            // look for 'defaults' file
            int dot = name.lastIndexOf('.');
            // foo/bar.groovy -> foo/bar.default.groovy
            // but don't do foo.bar/test -> foo.default.bar/test
            // as of 2010/9, this behaviour is considered deprecated, but left here for backward compatibility.
            // we need a better way to refer to the resource of the same name in the base type.
            if(name.lastIndexOf('/')<dot)
                res = getResource(name.substring(0, dot) + ".default" + name.substring(dot));
        }
        if(res!=null)
            return parseScript(res);

        return null;
    }

    protected final S loadScript(String name) throws E {
        S s = resolveScript(name);
        if (s!=null)    return s;

        // not found on this class, delegate to the parent
        if(owner.baseClass!=null)
            return ((AbstractTearOff<CLT,S,E>)owner.baseClass.loadTearOff(getClass())).findScript(name);

        return null;
    }

    /**
     * Compiles a script into the compiled form.
     */
    protected abstract S parseScript(URL res) throws E;

    protected URL getResource(String name) {
        return owner.klass.getResource(name);
    }
}
