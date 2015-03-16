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

import groovy.lang.Binding;
import groovy.lang.Closure;
import groovy.lang.GroovyObject;
import groovy.lang.MissingMethodException;
import groovy.lang.MissingPropertyException;
import groovy.lang.Script;

/**
 * {@link Script} that performs method invocations and property access like {@link Closure} does.
 *
 * <p>
 * For example, when the script is:
 *
 * <pre>{@code
 * a = 1;
 * b(2);
 * }</pre>
 *
 * Using {@link GroovyClosureScript} as the base class would run it as:
 *
 * <pre>{@code
 * delegate.a = 1;
 * delegate.b(2);
 * }</pre>
 *
 * ... whereas in plain {@link Script}, this will be run as:
 *
 * <pre>{@code
 * binding.setProperty("a",1);
 * ((Closure)binding.getProperty("b")).call(2);
 * }</pre>
 *
 * This is convenient for building DSL as you can use an external object to define
 * methods and properties.
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class GroovyClosureScript extends Script {
    private GroovyObject delegate;

    protected GroovyClosureScript() {
        super();
    }

    protected GroovyClosureScript(Binding binding) {
        super(binding);
    }

    /**
     * Sets the delegation target.
     */
    public void setDelegate(GroovyObject delegate) {
        this.delegate = delegate;
    }

    public GroovyObject getDelegate() {
        return delegate;
    }

    public Object invokeMethod(String name, Object args) {
        try {
            return delegate.invokeMethod(name,args);
        } catch (MissingMethodException mme) {
            return super.invokeMethod(name, args);
        }
    }

    public Object getProperty(String property) {
        try {
            return delegate.getProperty(property);
        } catch (MissingPropertyException e) {
            return super.getProperty(property);
        }
    }

    public void setProperty(String property, Object newValue) {
        try {
            delegate.setProperty(property,newValue);
        } catch (MissingPropertyException e) {
            super.setProperty(property,newValue);
        }
    }
}
