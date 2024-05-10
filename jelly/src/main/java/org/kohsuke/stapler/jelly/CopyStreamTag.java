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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.XMLOutput;
import org.jvnet.maven.jellydoc.annotation.NoContent;
import org.xml.sax.SAXException;

/**
 * Copies a stream as text.
 * @author Kohsuke Kawaguchi
 */
@NoContent
public class CopyStreamTag extends AbstractStaplerTag {
    private Reader in;

    public void setReader(Reader in) {
        this.in = in;
    }

    public void setInputStream(InputStream in) {
        this.in = new InputStreamReader(in, StandardCharsets.UTF_8);
    }

    public void setFile(File f) throws FileNotFoundException {
        this.in = new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8);
    }

    @SuppressFBWarnings(value = "URLCONNECTION_SSRF_FD", justification = "Not relevant in this situation.")
    public void setUrl(URL url) throws IOException {
        setInputStream(url.openStream());
    }


    @Override
    public void doTag(XMLOutput xmlOutput) throws JellyTagException {
        if(in==null)
            // In JEXL, failures evaluate to null, so if the input is meant to be
            // set from expression, we don't want that evaluation failure to cause
            // the entire page rendering to fail.
            return;

        char[] buf = new char[8192];
        int len;

        try {
            try {
                while((len=in.read(buf,0,buf.length))>=0) {
                    int last = 0;
                    for (int i=0; i<len; i++ ) {
                        char ch = buf[i];
                        if (ch == '<') {
                            xmlOutput.characters(buf, last, i - last); // flush
                            xmlOutput.characters(CHARS_LE, 0, CHARS_LE.length);
                            last = i + 1;
                        } else if (ch == '&') {
                            xmlOutput.characters(buf,last,i-last); // flush
                            xmlOutput.characters(CHARS_AMP,0,CHARS_AMP.length);
                            last = i+1;
                        }
                    }
                    xmlOutput.characters(buf,last,len-last);
                }
            } finally {
                in.close();
            }
        } catch (IOException | SAXException e) {
            throw new JellyTagException(e);
        }
    }

    private static final char[] CHARS_LE = "&lt;".toCharArray();
    private static final char[] CHARS_AMP = "&amp;".toCharArray();
}
