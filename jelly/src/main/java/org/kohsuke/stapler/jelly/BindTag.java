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

import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.XMLOutput;
import org.jvnet.maven.jellydoc.annotation.NoContent;
import org.jvnet.maven.jellydoc.annotation.Required;
import org.kohsuke.stapler.WebApp;
import org.kohsuke.stapler.bind.Bound;
import org.kohsuke.stapler.bind.BoundObjectTable;
import org.kohsuke.stapler.framework.adjunct.AdjunctsInPage;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * Binds a server-side object to client side so that JavaScript can call into server.
 * This tag evaluates to a {@code <script>} tag.
 * 
 * @author Kohsuke Kawaguchi
 */
@NoContent
public class BindTag extends AbstractStaplerTag {
    private String varName;
    private Object javaObject;

    /**
     * JavaScript variable name to set the proxy to.
     * <p>
     * This name can be arbitrary left hand side expression,
     * such as "a[0]" or "a.b.c".
     *
     * If this value is unspecified, the tag generates a JavaScript expression to create a proxy.
     */
    public void setVar(String varName) {
        this.varName = varName;
    }

    @Required
    public void setValue(Object o) {
        this.javaObject = o;
    }

    @Override
    public void doTag(XMLOutput out) throws JellyTagException {
        // make sure we get the supporting script in place
        ensureDependencies(out);
        AdjunctTag a = new AdjunctTag();
        a.setContext(getContext());
        a.setIncludes("org.kohsuke.stapler.bind");
        a.doTag(out);

        try {
            if (javaObject==null) {
                if (varName == null) {
                    out.write("null");
                } else {
                    // TODO un-inline?
                    out.startElement("script");
                    out.write(varName + "=null;");
                    out.endElement("script");
                }
            } else {
                Bound h = WebApp.getCurrent().boundObjectTable.bind(javaObject);

                if (varName==null) {
                    // this mode (of writing just the expression) needs to be used with caution because
                    // the adjunct tag above might produce <script> tag.
                    // Doing this is deprecated as it cannot be done with CSP enabled
                    out.write(h.getProxyScript());
                } else {
                    // TODO Find a better solution for CSP compliant bind scripts than binding another object
                    Bound script = WebApp.getCurrent().boundObjectTable.bind(new BoundObjectTable.BindScript(h, varName));

                    final AttributesImpl attributes = new AttributesImpl();
                    attributes.addAttribute("", "src", "src", "", script.getURL());
                    attributes.addAttribute("", "type", "type", "", "application/javascript");
                    out.startElement("script", attributes);
                    out.endElement("script");
                }
            }
        } catch (SAXException e) {
            throw new JellyTagException(e);
        }
    }

    /**
     * Ensures that we have the dependencies properly available to run bind.js.
     *
     * bind.js requires either prototype or jQuery. If we use jQuery, we use
     * {@code JSON.stringify()}, which isn't present in older browser, so we
     * will add it.
     */
    private void ensureDependencies(XMLOutput out) throws JellyTagException {
        AdjunctsInPage adjuncts = AdjunctsInPage.get();

        if (adjuncts.isIncluded("org.kohsuke.stapler.framework.prototype.prototype"))
            return; // all the dependencies met

        if (adjuncts.isIncluded("org.kohsuke.stapler.jquery")) {
            // Old browsers (like htmlunit or <=IE7) doesn't support JSON.stringify needed to do the bind call via jQuery
            // So to be on the safe side we include json2.js. See https://github.com/douglascrockford/JSON-js
            include(out, "org.kohsuke.stapler.json2");
        } else {
            // supply a missing dependency (if we can)
            include(out, "org.kohsuke.stapler.framework.prototype.prototype");
        }
    }

    private void include(XMLOutput out, String adjunct) throws JellyTagException {
        if (varName==null) {
            // since this will insert a <script> tag, we can only do this when <st:bind> is used in the block mode
            // this can result in a missing dependency, but that's something the caller can fix by manually inserting
            // it to the page
            return;
        }
        AdjunctTag a = new AdjunctTag();
        a.setContext(getContext());
        a.setIncludes(adjunct);
        a.doTag(out);
    }
}
