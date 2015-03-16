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

import org.apache.commons.jelly.XMLOutput;
import org.dom4j.io.HTMLWriter;
import org.dom4j.io.OutputFormat;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

/**
 * Wrapper for XMLOutput using HTMLWriter that can turn off its HTML handling
 * (if the Content-Type gets set to something other than text/html).
 * 
 * @author Alan.Harder@Sun.Com
 */
public class HTMLWriterOutput extends XMLOutput {
    private HTMLWriter htmlWriter;
    private OutputFormat format;

    public static HTMLWriterOutput create(OutputStream out) throws UnsupportedEncodingException {
        OutputFormat format = createFormat();
        return new HTMLWriterOutput(new HTMLWriter(out, format), format, false);
    }

    public static HTMLWriterOutput create(Writer out, boolean escapeText) {
        OutputFormat format = createFormat();
        return new HTMLWriterOutput(new HTMLWriter(out, format), format, escapeText);
    }

    private static OutputFormat createFormat() {
        OutputFormat format = new OutputFormat();
        format.setXHTML(true);
        // Only use short close for tags identified by HTMLWriter:
        format.setExpandEmptyElements(true);
        return format;
    }

    private HTMLWriterOutput(HTMLWriter hw, OutputFormat fmt, boolean escapeText) {
        super(hw);
        hw.setEscapeText(escapeText);
        this.htmlWriter = hw;
        this.format = fmt;
    }

    @Override public void close() throws IOException {
        htmlWriter.close();
    }

    /**
     * False to turn off HTML handling and reenable {@code />} for any empty XML element.
     * True to switch back to default mode with HTML handling.
     */
    public void useHTML(boolean enabled) {
        htmlWriter.setEnabled(enabled);
        format.setExpandEmptyElements(enabled);
    }
}
