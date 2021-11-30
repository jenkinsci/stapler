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

import edu.umd.cs.findbugs.annotations.NonNull;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.Arrays;

/**
 * JSON writer.
 *
 * @author Kohsuke Kawaguchi
 */
class JSONDataWriter implements DataWriter {
    protected boolean needComma;
    protected final Writer out;
    protected final ExportConfig config;

    private int indent;
    private String classAttr;

    JSONDataWriter(Writer out, ExportConfig config) throws IOException {
        this.out = out;
        this.config = config;
        indent = config.isPrettyPrint() ? 0 : -1;
    }

    @Override
    public @NonNull ExportConfig getExportConfig() {
        return config;
    }

    @Override
    public void name(String name) throws IOException {
        comma();
        if (indent<0)   out.write('"'+name+"\":");
        else            out.write('"'+name+"\" : ");
        needComma = false;
    }

    protected void data(String v) throws IOException {
        comma();
        out.write(v);
    }

    protected void comma() throws IOException {
        if(needComma) {
            out.write(',');
            indent();
        }
        needComma = true;
    }

    /**
     * Prints indentation.
     */
    private void indent() throws IOException {
        if (indent>=0) {
            out.write('\n');
            for (int i=indent*2; i>0; ) {
                int len = Math.min(i,INDENT.length);
                out.write(INDENT,0,len);
                i-=len;
            }
        }
    }

    private void inc() {
        if (indent<0)   return; // no indentation
        indent++;
    }

    private void dec() {
        if (indent<0)   return;
        indent--;
    }

    @Override
    public void valuePrimitive(Object v) throws IOException {
        data(v.toString());
    }

    @Override
    public void value(String v) throws IOException {
        StringBuilder buf = new StringBuilder(v.length());
        buf.append('\"');
        for( int i=0; i<v.length(); i++ ) {
            char c = v.charAt(i);
            if (Character.isISOControl(c) || Character.isHighSurrogate(c) || Character.isLowSurrogate(c)) {
                // Control chars: strictly speaking, JSON spec expects only U+0000 through U+001F, but any char _may_ be escaped, so just do that for U+007F through U+009F too.
                // Surrogate pair characters: https://docs.oracle.com/javase/6/docs/api/java/lang/Character.html#unicode
                // JSON spec: https://tools.ietf.org/html/rfc8259#section-7
                buf.append("\\u");
                buf.append(HEX[(c >> 12) & 0xf]);
                buf.append(HEX[(c >> 8) & 0xf]);
                buf.append(HEX[(c >> 4) & 0xf]);
                buf.append(HEX[c & 0xf]);
            } else {
                switch (c) {
                    case '"':
                        buf.append("\\\"");
                        break;
                    case '\\':
                        buf.append("\\\\");
                        break;
                    case '\n':
                        buf.append("\\n");
                        break;
                    case '\r':
                        buf.append("\\r");
                        break;
                    case '\t':
                        buf.append("\\t");
                        break;
                    default:
                        buf.append(c);
                        break;
                }
            }
        }
        buf.append('\"');
        data(buf.toString());
    }

    @Override
    public void valueNull() throws IOException {
        data("null");
    }

    private void open(char symbol) throws IOException {
        comma();
        out.write(symbol);
        needComma = false;
        inc();
        indent();
    }

    private void close(char symbol) throws IOException {
        dec();
        indent();
        needComma = true;
        out.write(symbol);
    }

    @Override
    public void startArray() throws IOException {
        open('[');
    }

    @Override
    public void endArray() throws IOException {
        close(']');
    }

    @Override
    public void type(Type expected, Class actual) throws IOException {
        classAttr = config.getClassAttribute().print(expected, actual);
    }

    @Override
    public void startObject() throws IOException {
        _startObject();

        if (classAttr!=null) {
            name(CLASS_PROPERTY_NAME);
            value(classAttr);
            classAttr = null;
        }
    }

    protected void _startObject() throws IOException {
        open('{');
    }

    @Override
    public void endObject() throws IOException {
        close('}');
    }

    private static final char[] HEX = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    private static final char[] INDENT = new char[32];
    static {
        Arrays.fill(INDENT, ' ');
    }
}