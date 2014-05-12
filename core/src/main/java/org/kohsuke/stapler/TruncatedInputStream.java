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

package org.kohsuke.stapler;

import java.io.FilterInputStream;
import java.io.InputStream;
import java.io.IOException;

/**
 * {@link InputStream} decorator that chops off the underlying stream at the specified length
 *
 * @author Kohsuke Kawaguchi
 */
final class TruncatedInputStream extends FilterInputStream {
    private long len;

    public TruncatedInputStream(InputStream in, long len) {
        super(in);
        this.len = len;
    }

    @Override
    public int read() throws IOException {
        if(len<=0)
            return -1;
        len--;
        return super.read();
    }

    @Override
    public int read(byte[] b, int off, int l) throws IOException {
        int toRead = (int) Math.min(l, len);
        if (toRead <= 0) {
            return -1;
        }

        int r = super.read(b, off, toRead);
        if(r>0)
            len -= r;
        return r;
    }

    @Override
    public int available() throws IOException {
        return (int)Math.min(super.available(),len);
    }

    @Override
    public long skip(long n) throws IOException {
        long r = super.skip(Math.min(len, n));
        len -= r;
        return r;
    }
}
