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

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyCodeSource;
import org.apache.commons.jelly.XMLOutput;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.kohsuke.stapler.MetaClass;
import org.kohsuke.stapler.MetaClassLoader;

import java.io.IOException;
import java.net.URL;

/**
 * @author Kohsuke Kawaguchi
 */
public class GroovyClassLoaderTearOff {
    private final MetaClassLoader owner;

    private final GroovyClassLoader gcl;

    private final SimpleTemplateParser parser = new SimpleTemplateParser() {
        /**
         * Sends the output via {@link XMLOutput#write(String)}
         */
        @Override
        protected String printCommand() {
            return "output.write";
        }
    };

    public GroovyClassLoaderTearOff(MetaClassLoader owner) {
        this.owner = owner;
        gcl = createGroovyClassLoader();
    }

    private GroovyClassLoader createGroovyClassLoader() {
        CompilerConfiguration cc = new CompilerConfiguration();
        // use GroovyClosureScript class as the base class of the compiled script,
        // so that we can set a delegate.
        cc.setScriptBaseClass(StaplerClosureScript.class.getName());

        // enable re-compilation support
        cc.setRecompileGroovySource(MetaClass.NO_CACHE);
        return new GroovyClassLoader(owner.loader,cc) {
            /**
             * Groovy calls this method to locate .groovy script files,
             * so during the development it's important to check the
             * resource path before target/classes.
             */
            @Override
            public URL getResource(String name) {
                // allow the resource path to take precedence when loading script
                if(MetaClassLoader.debugLoader!=null) {
                    URL res = MetaClassLoader.debugLoader.loader.getResource(name);
                    if(res!=null)
                        return res;
                }
                return super.getResource(name);
            }
        };
    }

    public GroovierJellyScript parse(URL script) throws IOException {
        // we do the caching on our own, so don't let GroovyClassLoader cache this. Or else
        // dynamic reloading won't work
        GroovyCodeSource gcs = new GroovyCodeSource(script);
        gcs.setCachable(false);

        return new GroovierJellyScript(gcl.parseClass(gcs),script);
    }

    public GroovierJellyScript parseGSP(URL res) throws IOException, ClassNotFoundException {
        GroovyCodeSource gcs = new GroovyCodeSource(parser.parse(res), res.toExternalForm(), res.toExternalForm());
        gcs.setCachable(false);

        return new GroovierJellyScript(gcl.parseClass(gcs),res);
    }
}
