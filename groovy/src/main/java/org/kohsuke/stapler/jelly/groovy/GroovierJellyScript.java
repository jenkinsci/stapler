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

package org.kohsuke.stapler.jelly.groovy;

import org.apache.commons.jelly.JellyContext;
import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.Script;
import org.apache.commons.jelly.XMLOutput;
import org.codehaus.groovy.reflection.CachedClass;
import org.codehaus.groovy.reflection.CachedMethod;
import org.codehaus.groovy.runtime.InvokerHelper;
import groovy.lang.Binding;
import groovy.lang.MetaClassImpl;
import groovy.lang.MutableMetaClass;

import java.lang.reflect.Method;
import java.net.URL;
import java.util.Arrays;
import java.util.Objects;

/**
 * Wraps a Groovy-driven Jelly script into {@link Script}
 * (so that it can be called from other Jelly scripts.) 
 *
 * @author Kohsuke Kawaguchi
 */
public class GroovierJellyScript implements Script {
    /**
     * Compiled Groovy class.
     */
    private final Class clazz;

    /**
     * Where was this script loaded from? Used for diagnostics.
     */
    private final URL scriptURL;

    public GroovierJellyScript(Class clazz, URL scriptURL) {
        this.clazz = clazz;
        this.scriptURL = scriptURL;
    }
    
    @Override
    public Script compile() {
        return this;
    }

    @Override
    public void run(JellyContext context, XMLOutput output) throws JellyTagException {
        run(new JellyBuilder(context, output));
    }

    public void run(JellyBuilder builder) {
        StaplerClosureScript gcs;
        try {
            gcs = (StaplerClosureScript) InvokerHelper.createScript(clazz, new Binding());
        } catch (LinkageError e) {
            throw new LinkageError("Failed to run " + clazz + " from " + scriptURL, e);
        }
        gcs.setDelegate(builder);

        // gettext() used to be called _() in earlier versions.
        MutableMetaClass metaClass = new MetaClassImpl(clazz);
        Arrays.stream(clazz.getMethods())
                .filter(method -> method.getName().equals("gettext"))
                .map(CachedMethod::find)
                .map(method -> new AliasMetaMethod(method.getDeclaringClass(), method.getCachedMethod(), "_"))
                .forEach(method -> metaClass.addMetaMethod(method));
        metaClass.initialize();
        gcs.setMetaClass(metaClass);

        gcs.scriptURL = scriptURL;
        gcs.run();
    }

    private static final class AliasMetaMethod extends CachedMethod {
        private final String alias;

        public AliasMetaMethod(CachedClass clazz, Method method, String alias) {
            super(clazz, method);
            this.alias = Objects.requireNonNull(alias);
        }

        @Override
        public String getName() {
            return alias;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            if (!super.equals(o)) {
                return false;
            }
            AliasMetaMethod that = (AliasMetaMethod) o;
            return Objects.equals(alias, that.alias);
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), alias);
        }
    }
}
