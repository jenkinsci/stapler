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
import org.apache.commons.jelly.Tag;
import org.apache.commons.jelly.TagLibrary;
import org.apache.commons.jelly.XMLOutput;
import org.apache.commons.jelly.expression.ConstantExpression;
import org.apache.commons.jelly.expression.Expression;
import org.apache.commons.jelly.impl.ExpressionAttribute;
import org.apache.commons.jelly.impl.StaticTag;
import org.apache.commons.jelly.impl.StaticTagScript;
import org.apache.commons.jelly.impl.TagScript;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * Jelly tag library for static tags.
 *
 * <p>
 * Unlike {@link StaticTagScript}, this doesn't even try to see if the tag name is available as a dynamic tag.
 * By not doing so, this implementation achieves a better performance both in speed and memory usage. 
 *
 * <p>
 * Jelly by default uses {@link StaticTagScript} instance to represent a tag that's parsed as a static tag,
 * and for each invocation, this code checks if the tag it represents is now defined as a dynamic tag.
 * Plus it got the code to cache {@link StaticTag} instances per thread, which consumes more space and time.
 *
 * @author Kohsuke Kawaguchi
 * @since 1.342
 */
public class ReallyStaticTagLibrary extends TagLibrary {
    /**
     * IIUC, this method will never be invoked.
     */
    @Override
    public Tag createTag(final String name, Attributes attributes) throws JellyException {
        return null;
    }

    @Override
    public TagScript createTagScript(String tagName, Attributes atts) throws JellyException {
        return createTagScript();
    }

    /**
     * Creates a new instance of {@link TagScript} that generates a literal element.
     */
    public static TagScript createTagScript() {
        return new TagScript() {
            /**
             * If all the attributes are constant, as is often the case with literal tags,
             * then we can skip the attribute expression evaluation altogether.
             */
            private boolean allAttributesAreConstant = true;

            @Override
            public void addAttribute(String name, Expression expression) {
                allAttributesAreConstant &= expression instanceof ConstantExpression;
                super.addAttribute(name, expression);
            }

            @Override
            public void addAttribute(String name, String prefix, String nsURI, Expression expression) {
                allAttributesAreConstant &= expression instanceof ConstantExpression;
                super.addAttribute(name, prefix, nsURI, expression);
            }

            public void run(JellyContext context, XMLOutput output) throws JellyTagException {
                Attributes actual = (allAttributesAreConstant && !EMIT_LOCATION) ? getSaxAttributes() : buildAttributes(context);

                try {
                    output.startElement(getNsUri(),getLocalName(),getElementName(),actual);
                    getTagBody().run(context,output);
                    output.endElement(getNsUri(),getLocalName(),getElementName());
                } catch (SAXException x) {
                    throw new JellyTagException(x);
                }
            }

            private AttributesImpl buildAttributes(JellyContext context) {
                AttributesImpl actual = new AttributesImpl();

                for (ExpressionAttribute att : attributes.values()) {
                    Expression expression = att.exp;
                    String v = expression.evaluateAsString(context);
                    if (v==null)    continue; // treat null as no attribute
                    actual.addAttribute(att.nsURI, att.name, att.qname(),"CDATA", v);
                }

                if (EMIT_LOCATION) {
                    actual.addAttribute("","file","file","CDATA",String.valueOf(getFileName()));
                    actual.addAttribute("","line","line","CDATA",String.valueOf(getLineNumber()));

                    // try to obtain the meaningful part of the script and put it in CSS with a
                    // class name like "jelly-foo-bar-xyz" given "file://path/to/src/tree/src/main/resources/foo/bar/xyz.jelly"
                    String form = getFileName().replace('\\','/');
                    for (String suffix : SUFFIX) {
                        int idx = form.lastIndexOf(suffix);
                        if (idx>0)  form=form.substring(idx+suffix.length());
                    }

                    int c = actual.getIndex("class");
                    if (c>=0)   actual.setValue(c, actual.getValue(c)+" "+form);
                    else        actual.addAttribute("","class","class","CDATA",form);
                }

                return actual;
            }
        };
    }
    /**
     * Reusable instance.
     */
    public static final TagLibrary INSTANCE = new ReallyStaticTagLibrary();

    /**
     * If true, emit the location information.
     */
    public static boolean EMIT_LOCATION = false;

    private static final String[] SUFFIX = {"src/main/resources/","src/test/resources/"};
}
