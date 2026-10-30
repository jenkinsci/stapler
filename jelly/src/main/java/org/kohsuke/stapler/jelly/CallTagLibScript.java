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

import java.util.HashMap;
import java.util.Map;
import org.apache.commons.jelly.JellyContext;
import org.apache.commons.jelly.JellyException;
import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.Script;
import org.apache.commons.jelly.XMLOutput;
import org.apache.commons.jelly.expression.Expression;
import org.apache.commons.jelly.impl.DynamicTag;
import org.apache.commons.jelly.impl.ExpressionAttribute;
import org.apache.commons.jelly.impl.TagScript;
import org.xml.sax.SAXException;

/**
 * {@link TagScript} that invokes a {@link Script} as a tag.
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class CallTagLibScript extends TagScript {
    /**
     * Resolves to the definition of the script to call to.
     */
    protected abstract Script resolveDefinition(JellyContext context) throws JellyTagException;

    @Override
    public void run(final JellyContext context, XMLOutput output) throws JellyTagException {
        // evaluated values of the attributes
        Map args = new HashMap(attributes.size());

        for (Map.Entry<String, ExpressionAttribute> e : attributes.entrySet()) {
            Expression expression = e.getValue().exp;
            args.put(e.getKey(), expression.evaluate(context));
        }

        // create new context based on current attributes
        JellyContext newJellyContext = context.newJellyContext(args);
        newJellyContext.setExportLibraries(false);
        newJellyContext.setVariable("attrs", args);

        // <d:invokeBody> uses this to discover what to invoke
        newJellyContext.setVariable(
                "org.apache.commons.jelly.body", new Body(getTagBody(), context.getVariable("attrs")));
        newJellyContext.setVariable("org.apache.commons.jelly.body.scope", context);
        final Script def = resolveDefinition(newJellyContext);

        if (JellyFacet.TRACE) {
            try {
                String source = getSource();
                String msg = "<" + source + ">";
                output.comment(msg.toCharArray(), 0, msg.length());
                def.run(newJellyContext, output);
                msg = "</" + source + ">";
                output.comment(msg.toCharArray(), 0, msg.length());
            } catch (SAXException e) {
                throw new JellyTagException(e);
            }
        } else {
            def.run(newJellyContext, output);
        }
    }

    /**
     * The body of a tag invocation, as {@code <d:invokeBody/>} runs it.
     *
     * <p>
     * When &lt;d:invokeBody/> is used to call back into the calling script,
     * the Jelly name resolution rule is in such that the body is evaluated with
     * the variable scope of the &lt;d:invokeBody/> caller. This is very different
     * from a typical closure name resolution mechanism, where the body is evaluated
     * with the variable scope of where the body was created.
     *
     * <p>
     * More concretely, in Jelly, this often shows up as a problem as inability to
     * access the "attrs" variable from inside a body, because every {@link DynamicTag}
     * invocation sets this variable in a new scope.
     *
     * <p>
     * To counter this effect, this class temporarily restores the original "attrs"
     * when the body is evaluated. This makes the name resolution of 'attrs' work
     * like what programmers normally expect.
     *
     * <p>
     * The same problem also shows up as a lack of local variables &mdash; when a tag
     * calls into the body via &lt;d:invokeBody/>, the invoked body will see all the
     * variables that are defined in the caller, which is again not what a normal programming language
     * does. But unfortunately, changing this is too pervasive.
     *
     * <p>
     * It keeps the caller's "attrs", not the caller's context: a body kept for later
     * (as one rendered on demand is) then does not keep every context of the page alive.
     */
    private static final class Body implements Script {
        private final Script tagBody;
        private final Object attrs;

        Body(Script tagBody, Object attrs) {
            this.tagBody = tagBody;
            this.attrs = attrs;
        }

        Body detached() {
            if (attrs instanceof Map<?, ?> m && m.containsKey("ownerTag")) { // set by jelly:define's DynamicTag
                Map<Object, Object> copy = new HashMap<>(m);
                copy.remove("ownerTag");
                return new Body(tagBody, copy);
            }
            return this;
        }

        @Override
        public Script compile() throws JellyException {
            return this;
        }

        @Override
        public void run(JellyContext nestedContext, XMLOutput output) throws JellyTagException {
            Map m = nestedContext.getVariables();
            Object oldAttrs = m.put("attrs", attrs);
            try {
                tagBody.run(nestedContext, output);
            } finally {
                m.put("attrs", oldAttrs);
            }
        }
    }

    /**
     * A tag body (the value of {@code org.apache.commons.jelly.body} in a tag's context) that can be kept after the page
     * is rendered, to be run later (as a body rendered on demand is). It keeps no tag, and so no context, of the page:
     * the {@code ownerTag} of {@code attrs} from a {@code jelly:define} tag is left out.
     *
     * @param body a tag body
     * @return a body to keep, or the given one if it keeps nothing of the page
     */
    public static Script detach(Script body) {
        return body instanceof Body b ? b.detached() : body;
    }

    protected String getSource() {
        return "{jelly:" + getNsUri() + "}:" + getLocalName();
    }
}
