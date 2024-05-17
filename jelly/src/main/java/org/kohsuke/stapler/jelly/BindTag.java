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

import edu.umd.cs.findbugs.annotations.CheckForNull;
import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.XMLOutput;
import org.jvnet.maven.jellydoc.annotation.NoContent;
import org.jvnet.maven.jellydoc.annotation.Required;
import org.kohsuke.stapler.WebApp;
import org.kohsuke.stapler.bind.Bound;
import org.kohsuke.stapler.bind.BoundObjectTable;
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
        AdjunctTag a = new AdjunctTag();
        a.setContext(getContext());
        a.setIncludes("org.kohsuke.stapler.bind");
        a.doTag(out);

        try {
            if (javaObject == null) {
                if (varName == null) {
                    // Legacy mode if no 'var' is specified and we bound 'null': Write 'null' expression inline, without
                    // <script> wrapper.
                    out.write("null");
                } else {
                    // Modern: Write a script tag whose 'src' points to DataBoundTable#doScript, with a special suffix
                    // indicating the null bound object.
                    writeScriptTag(out, null);
                }
            } else {
                Bound h = WebApp.getCurrent().boundObjectTable.bind(javaObject);

                if (varName == null) {
                    // Legacy mode if no 'var' is specified: Write the expression inline, without <script> wrapper.
                    // Doing this is deprecated as it cannot be done with Content-Security-Policy unless 'unsafe-inline'
                    // is allowed.
                    // Additionally, this mode needs to be used with caution because the adjunct tag above might produce
                    // a <script> tag.
                    out.write(h.getProxyScript());
                } else if (!BoundObjectTable.isValidJavaScriptIdentifier(varName)) {
                    // Legacy mode if 'var' is not a safe variable name: Write the expression inline within <script>
                    // wrapper.
                    // Doing this is deprecated as it cannot be done with Content-Security-Policy unless 'unsafe-inline'
                    // is allowed.
                    out.startElement("script");
                    out.write(varName + "=" + h.getProxyScript() + ";");
                    out.endElement("script");
                } else {
                    // Modern: Write a script tag whose 'src' points to DataBoundTable#doScript
                    writeScriptTag(out, h);
                }
            }
        } catch (SAXException e) {
            throw new JellyTagException(e);
        }
    }

    /**
     * Writes a {@code <script>} tag whose {@code src} attribute points to
     * {@link BoundObjectTable#doScript(org.kohsuke.stapler.StaplerRequest, org.kohsuke.stapler.StaplerResponse, String, String)}.
     * @param out XML output
     * @param bound Wrapper for the bound object
     * @throws SAXException if something goes wrong writing XML
     */
    private void writeScriptTag(XMLOutput out, @CheckForNull Bound bound) throws SAXException {
        final AttributesImpl attributes = new AttributesImpl();
        if (bound == null) {
            attributes.addAttribute("", "src", "src", "", Bound.getProxyScriptURL(varName, null));
        } else {
            attributes.addAttribute("", "src", "src", "", bound.getProxyScriptURL(varName));
        }
        attributes.addAttribute("", "type", "type", "", "text/javascript");
        out.startElement("script", attributes);
        out.endElement("script");
    }
}
