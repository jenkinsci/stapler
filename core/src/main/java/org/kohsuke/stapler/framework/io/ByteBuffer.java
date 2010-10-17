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

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * {@link ByteArrayOutputStream} re-implementation.
 *
 * <p>
 * This version allows one to read while writing is in progress.
 *
 * @author Kohsuke Kawaguchi
 */
// TODO: reimplement this without buffer reallocation
public class ByteBuffer extends OutputStream {
    private byte[] buf = new byte[8192];
    /**
     * Size of the data.
     */
    private int size = 0;


    public synchronized void write(byte b[], int off, int len) throws IOException {
        ensureCapacity(len);
        System.arraycopy(b,off,buf,size,len);
        size+=len;
    }

    public synchronized void write(int b) throws IOException {
        ensureCapacity(1);
        buf[size++] = (byte)b;
    }

    public synchronized long length() {
        return size;
    }

    private void ensureCapacity(int len) {
        if(buf.length-size>len)
            return;

        byte[] n = new byte[Math.max(buf.length*2, size+len)];
        System.arraycopy(buf,0,n,0,size);
        this.buf = n;
    }

    public synchronized String toString() {
        return new String(buf,0,size);
    }

    /**
     * Writes the contents of this buffer to another OutputStream.
     */
    public synchronized void writeTo(OutputStream os) throws IOException {
        os.write(buf,0,size);
    }

    /**
     * Creates an {@link InputStream} that reads from the underlying buffer.
     */
    public InputStream newInputStream() {
        return new InputStream() {
            private int pos = 0;
            public int read() throws IOException {
                synchronized(ByteBuffer.this) {
                    if(pos>=size)   return -1;
                    return buf[pos++];
                }
            }

            public int read(byte b[], int off, int len) throws IOException {
                synchronized(ByteBuffer.this) {
                    if(size==pos)
                        return -1;

                    int sz = Math.min(len,size-pos);
                    System.arraycopy(buf,pos,b,off,sz);
                    pos+=sz;
                    return sz;
                }
            }


            public int available() throws IOException {
                synchronized(ByteBuffer.this) {
                    return size-pos;
                }
            }

            public long skip(long n) throws IOException {
                synchronized(ByteBuffer.this) {
                    int diff = (int) Math.min(n,size-pos);
                    pos+=diff;
                    return diff;
                }
            }
        };
    }
}
