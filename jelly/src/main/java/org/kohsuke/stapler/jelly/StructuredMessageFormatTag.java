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
import org.apache.commons.jelly.LocationAware;
import org.jvnet.localizer.LocaleProvider;
import org.jvnet.maven.jellydoc.annotation.Required;
import org.xml.sax.SAXException;

import java.util.ArrayList;
import java.util.List;

/**
 * Format message from a resource, but by using a nested children as arguments, instead of just using expressions.
 * 
 * @author Kohsuke Kawaguchi
 */
public class StructuredMessageFormatTag extends AbstractStaplerTag implements LocationAware {
    private final List<Object> arguments = new ArrayList<Object>();

    private String key;
    private ResourceBundle rb;

    @Required
    public void setKey(String resourceKey) {
        this.key = resourceKey;
    }

    public void addArgument(Object o) {
        this.arguments.add(o);
    }

    public void doTag(XMLOutput output) throws JellyTagException {
        try {
            arguments.clear();
            invokeBody(output);

            output.write(rb.format(LocaleProvider.getLocale(), key,arguments.toArray()));
        } catch (SAXException e) {
            throw new JellyTagException("could not write the XMLOutput",e);
        } finally {
            arguments.clear(); // don't keep heavy objects in memory for too long
        }
    }

    public int getLineNumber() {
        return -1;
    }

    public void setLineNumber(int lineNumber) {
    }

    public int getColumnNumber() {
        return -1;
    }

    public void setColumnNumber(int columnNumber) {
    }

    public String getFileName() {
        return null;
    }

    public void setFileName(String fileName) {
        rb = ResourceBundle.load(fileName);
    }

    public String getElementName() {
        return null;
    }

    public void setElementName(String elementName) {
    }
}
