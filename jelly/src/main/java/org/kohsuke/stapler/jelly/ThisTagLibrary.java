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
import org.apache.commons.jelly.Tag;
import org.apache.commons.jelly.TagLibrary;
import org.apache.commons.jelly.expression.Expression;
import org.apache.commons.jelly.expression.ExpressionSupport;
import org.apache.commons.jelly.impl.TagScript;
import org.kohsuke.stapler.MetaClass;
import org.kohsuke.stapler.WebApp;
import org.kohsuke.stapler.lang.Klass;
import org.xml.sax.Attributes;

/**
 * Loads Jelly views associated with "it" as if it were taglibs.
 *
 * @author Kohsuke Kawaguchi
 */
public class ThisTagLibrary extends TagLibrary {
    private final Expression expr;

    /**
     * @param expr
     *      Expression that evaluates to {@link Class} to resolve scripts from.
     */
    public ThisTagLibrary(Expression expr) {
        this.expr = expr;
    }

    /**
     * IIUC, this method will never be invoked.
     */
    @Override
    public Tag createTag(final String name, Attributes attributes) throws JellyException {
        return null;
    }

    @Override
    public TagScript createTagScript(final String tagName, Attributes atts) throws JellyException {
        return new CallTagLibScript() {
            @Override
            protected Script resolveDefinition(JellyContext context) throws JellyTagException {
                Object it = expr.evaluate(context);
                if (it==null)
                    throw new JellyTagException("'"+ expr.getExpressionText() +"' evaluated to null");
                try {
                    WebApp webApp = WebApp.getCurrent();
                    MetaClass c = webApp.getMetaClass(it instanceof Class ? Klass.java((Class)it):  webApp.getKlass(it));
                    // prefer 'foo.jellytag' to avoid tags from showing up as views,
                    // but for backward compatibility, support the plain .jelly extention as well.
                    Script tag = c.loadTearOff(JellyClassTearOff.class).findScript(tagName+".jellytag");
                    if (tag==null)
                        tag = c.loadTearOff(JellyClassTearOff.class).findScript(tagName+".jelly");
                    if (tag ==null)
                        throw new JellyTagException("No such tag file "+tagName+".jellytag in "+c);
                    return tag;
                } catch (JellyException e) {
                    throw new JellyTagException("Failed to load "+tagName+".jellytag from "+it,e);
                }
            }
        };
    }

    public static final ThisTagLibrary INSTANCE = new ThisTagLibrary(new ExpressionSupport() {
        public String getExpressionText() {
            return "it";
        }

        public Object evaluate(JellyContext context) {
            return context.getVariable("it");
        }
    });
}
