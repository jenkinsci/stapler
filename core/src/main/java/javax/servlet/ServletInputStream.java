/*
 * Copyright (c) 1997-2018 Oracle and/or its affiliates and others.
 * All rights reserved.
 * Copyright 2004 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package javax.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public abstract class ServletInputStream extends InputStream {
    protected ServletInputStream() {}

    public int readLine(byte[] b, int off, int len) throws IOException {

        if (len <= 0) {
            return 0;
        }
        int count = 0, c;

        while ((c = read()) != -1) {
            b[off++] = (byte) c;
            count++;
            if (c == '\n' || count == len) {
                break;
            }
        }
        return count > 0 ? count : -1;
    }

    public abstract boolean isFinished();

    public abstract boolean isReady();

    public abstract void setReadListener(ReadListener readListener);

    public jakarta.servlet.ServletInputStream toJakartaServletInputStream() {
        return new jakarta.servlet.ServletInputStream() {
            @Override
            public int read() throws IOException {
                return ServletInputStream.this.read();
            }

            @Override
            public int readLine(byte[] b, int off, int len) throws IOException {
                return ServletInputStream.this.readLine(b, off, len);
            }

            @Override
            public int read(byte[] b) throws IOException {
                return ServletInputStream.this.read(b);
            }

            @Override
            public int read(byte[] b, int off, int len) throws IOException {
                return ServletInputStream.this.read(b, off, len);
            }

            @Override
            public byte[] readAllBytes() throws IOException {
                return ServletInputStream.this.readAllBytes();
            }

            @Override
            public byte[] readNBytes(int len) throws IOException {
                return ServletInputStream.this.readNBytes(len);
            }

            @Override
            public int readNBytes(byte[] b, int off, int len) throws IOException {
                return ServletInputStream.this.readNBytes(b, off, len);
            }

            @Override
            public long skip(long n) throws IOException {
                return ServletInputStream.this.skip(n);
            }

            @Override
            public int available() throws IOException {
                return ServletInputStream.this.available();
            }

            @Override
            public void close() throws IOException {
                ServletInputStream.this.close();
            }

            @Override
            public synchronized void mark(int readlimit) {
                ServletInputStream.this.mark(readlimit);
            }

            @Override
            public synchronized void reset() throws IOException {
                ServletInputStream.this.reset();
            }

            @Override
            public boolean markSupported() {
                return ServletInputStream.this.markSupported();
            }

            @Override
            public long transferTo(OutputStream out) throws IOException {
                return ServletInputStream.this.transferTo(out);
            }

            @Override
            public boolean isFinished() {
                return ServletInputStream.this.isFinished();
            }

            @Override
            public boolean isReady() {
                return ServletInputStream.this.isReady();
            }

            @Override
            public void setReadListener(jakarta.servlet.ReadListener readListener) {
                ServletInputStream.this.setReadListener(ReadListener.fromJakartaReadListener(readListener));
            }
        };
    }

    public static ServletInputStream fromJakartaServletInputStream(jakarta.servlet.ServletInputStream from) {
        return new ServletInputStream() {
            @Override
            public int readLine(byte[] b, int off, int len) throws IOException {
                return from.readLine(b, off, len);
            }

            @Override
            public int read(byte[] b) throws IOException {
                return from.read(b);
            }

            @Override
            public int read(byte[] b, int off, int len) throws IOException {
                return from.read(b, off, len);
            }

            @Override
            public byte[] readAllBytes() throws IOException {
                return from.readAllBytes();
            }

            @Override
            public byte[] readNBytes(int len) throws IOException {
                return from.readNBytes(len);
            }

            @Override
            public int readNBytes(byte[] b, int off, int len) throws IOException {
                return from.readNBytes(b, off, len);
            }

            @Override
            public long skip(long n) throws IOException {
                return from.skip(n);
            }

            @Override
            public int available() throws IOException {
                return from.available();
            }

            @Override
            public void close() throws IOException {
                from.close();
            }

            @Override
            public synchronized void mark(int readlimit) {
                from.mark(readlimit);
            }

            @Override
            public synchronized void reset() throws IOException {
                from.reset();
            }

            @Override
            public boolean markSupported() {
                return from.markSupported();
            }

            @Override
            public long transferTo(OutputStream out) throws IOException {
                return from.transferTo(out);
            }

            @Override
            public boolean isFinished() {
                return from.isFinished();
            }

            @Override
            public boolean isReady() {
                return from.isReady();
            }

            @Override
            public void setReadListener(ReadListener readListener) {
                from.setReadListener(readListener.toJakartaReadListener());
            }

            @Override
            public int read() throws IOException {
                return from.read();
            }

            @Override
            public jakarta.servlet.ServletInputStream toJakartaServletInputStream() {
                return from;
            }
        };
    }
}
