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

package org.kohsuke.stapler.framework.io;

import java.io.Writer;
import java.io.IOException;
import java.util.List;
import java.util.LinkedList;

/**
 * {@link Writer} that spools the output and writes to another {@link Writer} later.
 *
 * @author Kohsuke Kawaguchi
 */
public /*for now, until Hudson migration completes*/ final class CharSpool extends Writer {
    private List<char[]> buf;

    private char[] last = new char[1024];
    private int pos;

    public void write(char cbuf[], int off, int len) {
        while(len>0) {
            int sz = Math.min(last.length-pos,len);
            System.arraycopy(cbuf,off,last,pos,sz);
            len -= sz;
            off += sz;
            pos += sz;
            renew();
        }
    }

    private void renew() {
        if(pos<last.length)
            return;

        if(buf==null)
            buf = new LinkedList<char[]>();
        buf.add(last);
        last = new char[1024];
        pos = 0;
    }

    public void write(int c) {
        renew();
        last[pos++] = (char)c;
    }

    public void flush() {
        // noop
    }

    public void close() {
        // noop
    }

    public void writeTo(Writer w) throws IOException {
        if(buf!=null) {
            for (char[] cb : buf) {
                w.write(cb);
            }
        }
        w.write(last,0,pos);
    }
}
