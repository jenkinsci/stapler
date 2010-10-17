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
import org.apache.commons.jelly.MissingAttributeException;
import org.apache.commons.jelly.TagSupport;
import org.apache.commons.jelly.XMLOutput;
import org.apache.commons.jelly.expression.Expression;
import org.xml.sax.SAXException;
import org.jvnet.maven.jellydoc.annotation.NoContent;
import org.jvnet.maven.jellydoc.annotation.Required;

/**
 * Tag that outputs the specified value but with escaping,
 * so that you can escape a portion even if the
 * {@link XMLOutput} is not escaping.
 *
 * @author Kohsuke Kawaguchi
 */
@NoContent
public class OutTag extends TagSupport {
    private Expression value;

    @Required
    public void setValue(Expression value) {
        this.value = value;
    }

    public void doTag(XMLOutput output) throws MissingAttributeException, JellyTagException {
        final String text = value.evaluateAsString(context);
        if (text != null) {
            StringBuilder buf = new StringBuilder(text.length());
            for (int i=0; i<text.length(); i++ ) {
                char ch = text.charAt(i);
                switch(ch) {
                case '<':       buf.append("&lt;");     break;
                case '&':       buf.append("&amp;");    break;
                default:        buf.append(ch);
                }
            }

            try {
                output.write(buf.toString());
            }
            catch (SAXException e) {
                throw new JellyTagException("could not write the XMLOutput",e);
            }
        }
    }
}
