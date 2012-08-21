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

package org.kohsuke.stapler.export;

import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerResponse;

import java.util.Stack;
import java.io.Writer;
import java.io.IOException;
import java.beans.Introspector;

/**
 * Writes XML.
 *
 * @author Kohsuke Kawaguchi
 */
final class XMLDataWriter implements DataWriter {

    private String name;
    private final Stack<String> objectNames = new Stack<String>();
    private final Stack<Boolean> arrayState = new Stack<Boolean>();
    private final Writer out;
    public boolean isArray;

    XMLDataWriter(Object bean, Writer out) throws IOException {
        Class c=bean.getClass();
        while (c.isAnonymousClass())
            c = c.getSuperclass();
        name = Introspector.decapitalize(c.getSimpleName());
        this.out = out;
    }

    XMLDataWriter(Object bean, StaplerResponse rsp) throws IOException {
        this(bean,rsp.getWriter());
    }

    public void name(String name) {
        this.name = name;
    }

    public void valuePrimitive(Object v) throws IOException {
        value(v.toString());
    }

    public void value(String v) throws IOException {
        String n = adjustName();
        out.write('<'+n+'>');
        out.write(Stapler.escape(v));
        out.write("</"+n+'>');
    }

    public void valueNull() {
        // use absence to indicate null.
    }

    public void startArray() {
        // use repeated element to display array
        // this means nested arrays are not supported
        isArray = true;
    }

    public void endArray() {
        isArray = false;
    }

    public void startObject() throws IOException {
        objectNames.push(name);
        out.write('<'+adjustName()+'>');
        arrayState.push(isArray);
        isArray = false;
    }

    public void endObject() throws IOException {
        name = objectNames.pop();
        isArray = arrayState.pop();
        out.write("</"+adjustName()+'>');
    }

    /**
     * Returns the name to be used as an element name
     * by considering {@link #isArray}
     */
    private String adjustName() {
        String escaped = makeXmlName(name);
        if(isArray) return toSingular(escaped);
        return escaped;
    }

    /*package*/ static String toSingular(String name) {
        return name.replaceFirst("ies$", "y").replaceFirst("s$", "");
    }

    /*package*/ static String makeXmlName(String name) {
        if (name.length()==0)   name="_";

        if (!XmlChars.isNameStart(name.charAt(0))) {
            if (name.length()>1 && XmlChars.isNameStart(name.charAt(1)))
                name = name.substring(1);
            else
                name = '_'+name;
        }

        int i=1;
        while (i<name.length()) {
            if (XmlChars.isNameChar(name.charAt(i)))
                i++;
            else
                name = name.substring(0,i)+name.substring(i+1);
        }

        return name;
    }
}