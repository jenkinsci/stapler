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

package org.kohsuke.stapler.jelly;

import org.apache.commons.jelly.JellyContext;
import org.apache.commons.jelly.JellyException;
import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.Script;
import org.apache.commons.jelly.XMLOutput;
import org.kohsuke.stapler.lang.Klass;

import java.net.URL;
import java.util.logging.Logger;

/**
 * Represents a loaded Jelly view script that remembers where it came from.
 *
 * @author Kohsuke Kawaguchi
 */
public final class JellyViewScript implements Script {

    private static final Logger LOGGER = Logger.getLogger(JellyViewScript.class.getName());

    /**
     * Which class is this view loaded from?
     * @deprecated as of 1.177 
     *      Use {@link #fromKlass}
     */
    public final Class from;
    
    public final Klass<?> fromKlass;
    
    /**
     * Full URL that points to the source of the script.
     */
    public final URL source;

    private Script base;

    /**
     * @deprecated as of 1.177
     *      use {@link #JellyViewScript(Klass, URL, Script)}
     */
    public JellyViewScript(Class from, URL source, Script base) {
        this.from = from;
        this.fromKlass = Klass.java(from);
        this.source = source;
        this.base = base;
    }
    
    public JellyViewScript(Klass from, URL source, Script base) {
        this.from = from.toJavaClass();
        this.fromKlass = from;
        this.source = source;
        this.base = base;
    }

    public Script compile() throws JellyException {
        base = base.compile();
        return this;
    }

    public void run(JellyContext context, XMLOutput output) throws JellyTagException {
        Thread t = Thread.currentThread();
        String n = t.getName();
        // JellyViewScript.getName() is a bit too verbose for this purpose (we do not really need the package prefix):
        String url = source.toExternalForm();
        String c = from.getName();
        c = c.substring(c.lastIndexOf('.') + 1);
        String n2 = n + " " + c.replace('$', '/') + "/" + url.substring(url.lastIndexOf('/') + 1);
        t.setName(n2);
        LOGGER.fine(n2);
        try {
            base.run(context,output);
        } finally {
            t.setName(n);
        }
    }

    public String getName() {
        // get to the file name portion
        String url = source.toExternalForm();
        url = url.substring(url.lastIndexOf('/') +1);
        url = url.substring(url.lastIndexOf('\\') +1);

        return from.getName().replace('.','/').replace('$','/')+'/'+url;
    }
}
