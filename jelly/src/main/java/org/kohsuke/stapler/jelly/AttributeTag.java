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

import org.apache.commons.jelly.TagSupport;
import org.apache.commons.jelly.XMLOutput;
import org.jvnet.maven.jellydoc.annotation.Required;

/**
 * Documentation for an attribute of a Jelly tag file.
 *
 * <p>
 * This tag should be placed right inside {@code <st:documentation>}
 * to describe attributes of a tag. The body would describe
 * the meaning of an attribute in a natural language.
 * The description text can also use
 * <a href="http://textile.thresholdstate.com/">Textile markup</a>
 *
 * @author Kohsuke Kawaguchi
 */
public class AttributeTag extends TagSupport {
    public void doTag(XMLOutput output) {
        // noop
    }

    /**
     * Name of the attribute.
     */
    @Required
    public void setName(String v) {}

    /**
     * If the attribute is required, specify use="required".
     * (This is modeled after XML Schema attribute declaration.)
     *
     * <p>
     * By default, use="optional" is assumed.
     */
    public void setUse(String v) {}

    /**
     * If it makes sense, describe the Java type that the attribute
     * expects as values.
     */
    public void setType(String v) {}

    /**
     * If the attribute is deprecated, set to true.
     * Use of the deprecated attribute will cause a warning.
     */
    public void setDeprecated(boolean v) {}
}

