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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.servlet.ReadListener;
import java.io.IOException;
import java.io.OutputStream;

@SuppressFBWarnings(value = "NM_SAME_SIMPLE_NAME_AS_SUPERCLASS", justification = "for compatibility")
public abstract class ServletInputStream extends jakarta.servlet.ServletInputStream {
    public static ServletInputStream fromJakartaServletInputStream(jakarta.servlet.ServletInputStream jakartaServletInputStream) {
        return new ServletInputStream() {
            @Override
            public boolean isFinished() {
                return jakartaServletInputStream.isFinished();
            }

            @Override
            public boolean isReady() {
                return jakartaServletInputStream.isReady();
            }

            @Override
            public int readLine(byte[] b, int off, int len) throws IOException {
                return jakartaServletInputStream.readLine(b, off, len);
            }

            @Override
            public int read(byte[] b) throws IOException {
                return jakartaServletInputStream.read(b);
            }

            @Override
            public int read(byte[] b, int off, int len) throws IOException {
                return jakartaServletInputStream.read(b, off, len);
            }

            @Override
            public byte[] readAllBytes() throws IOException {
                return jakartaServletInputStream.readAllBytes();
            }

            @Override
            public byte[] readNBytes(int len) throws IOException {
                return jakartaServletInputStream.readNBytes(len);
            }

            @Override
            public int readNBytes(byte[] b, int off, int len) throws IOException {
                return jakartaServletInputStream.readNBytes(b, off, len);
            }

            @Override
            public long skip(long n) throws IOException {
                return jakartaServletInputStream.skip(n);
            }

            @Override
            public int available() throws IOException {
                return jakartaServletInputStream.available();
            }

            @Override
            public void close() throws IOException {
                jakartaServletInputStream.close();
            }

            @Override
            public synchronized void mark(int readlimit) {
                jakartaServletInputStream.mark(readlimit);
            }

            @Override
            public synchronized void reset() throws IOException {
                jakartaServletInputStream.reset();
            }

            @Override
            public boolean markSupported() {
                return jakartaServletInputStream.markSupported();
            }

            @Override
            public long transferTo(OutputStream out) throws IOException {
                return jakartaServletInputStream.transferTo(out);
            }

            @Override
            public void setReadListener(ReadListener readListener) {
                jakartaServletInputStream.setReadListener(readListener);
            }

            @Override
            public int read() throws IOException {
                return jakartaServletInputStream.read();
            }
        };
    }
}
