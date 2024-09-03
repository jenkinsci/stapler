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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.servlet.http.HttpServletRequest;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.jelly.JellyContext;
import org.apache.commons.jelly.JellyException;
import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.Script;
import org.apache.commons.jelly.TagLibrary;
import org.apache.commons.jelly.XMLOutput;
import org.apache.commons.jelly.impl.DefaultTagFactory;
import org.apache.commons.jelly.impl.TagScript;
import org.xml.sax.Attributes;

/**
 * @author Kohsuke Kawaguchi
 */
public class StaplerTagLibrary extends TagLibrary {
    public StaplerTagLibrary() {
        registerTag("adjunct", AdjunctTag.class);
        registerTag("bind", BindTag.class);
        registerTag("compress", CompressTag.class);
        registerTag("contentType", ContentTypeTag.class);
        registerTag("copyStream", CopyStreamTag.class);
        registerTag("doctype", DoctypeTag.class);
        registerTag("findAncestor", FindAncestorTag.class);
        registerTag("header", HeaderTag.class); // deprecated. for compatibility
        registerTag("addHeader", HeaderTag.class);
        registerTag("setHeader", SetHeaderTag.class);
        registerTag("include", IncludeTag.class);
        registerTag("isUserInRole", IsUserInRoleTag.class);
        registerTag("nbsp", NbspTag.class);
        registerTag("out", OutTag.class);
        registerTag("parentScope", ParentScopeTag.class);
        registerTag("redirect", RedirectTag.class);
        registerTag("statusCode", StatusCodeTag.class);
        registerTag("structuredMessageArgument", StructuredMessageArgumentTag.class);
        registerTag("structuredMessageFormat", StructuredMessageFormatTag.class);
    }

    @Override
    public TagScript createTagScript(String name, Attributes attributes) throws JellyException {
        // performance optimization
        if (name.equals("documentation")) {
            return new TagScript() {
                @Override
                public void run(JellyContext context, XMLOutput output) throws JellyTagException {
                    // noop
                }

                @Override
                public void setTagBody(Script tagBody) {
                    // noop, we don't evaluate the body, so don't even keep it in memory.
                }
            };
        }

        if (name.equals("getOutput")) {
            return new TagScript() {
                /**
                 * Adds {@link XMLOutput} to the context.
                 */
                @Override
                public void run(JellyContext context, XMLOutput output) throws JellyTagException {
                    context.setVariable(getAttribute("var").evaluateAsString(context), output);
                }
            };
        }

        if (name.equals("once")) {
            return new TagScript() {
                /**
                 * Adds {@link XMLOutput} to the context.
                 */
                @Override
                public void run(JellyContext context, XMLOutput output) throws JellyTagException {
                    HttpServletRequest request = (HttpServletRequest) context.getVariable("request2");
                    Set<String> executedScripts = (Set<String>) request.getAttribute(ONCE_TAG_KEY);
                    if (executedScripts == null) {
                        request.setAttribute(ONCE_TAG_KEY, executedScripts = new HashSet<>());
                    }

                    String key = getFileName() + ':' + getLineNumber() + ':' + getColumnNumber();

                    if (executedScripts.add(key)) { // run it just for the first time
                        getTagBody().run(context, output);
                    }
                }
            };
        }

        if (!DISABLE_INCLUDE_TAG_CLASS_ATTRIBUTE_REWRITING && name.equals("include")) {
            // Retain backward compatibility with all views setting the obsolete 'class' attribute.
            // See IncludeTag#setClazz for details.
            final AttributeNameRewritingTagScript script = new AttributeNameRewritingTagScript("class", "clazz");
            script.setTagFactory(new DefaultTagFactory(IncludeTag.class));
            return script;
        }

        return super.createTagScript(name, attributes);
    }

    private static final String ONCE_TAG_KEY = "stapler.once";

    // Disable the st:include compatibility workaround to allow testing
    @SuppressFBWarnings(value = "MS_SHOULD_BE_FINAL", justification = "Exposed for tests")
    public static /* non-final */ boolean DISABLE_INCLUDE_TAG_CLASS_ATTRIBUTE_REWRITING = false;
}
