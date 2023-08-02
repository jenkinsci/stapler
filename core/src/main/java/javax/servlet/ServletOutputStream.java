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
import jakarta.servlet.WriteListener;
import java.io.IOException;

@SuppressFBWarnings(value = "NM_SAME_SIMPLE_NAME_AS_SUPERCLASS", justification = "for compatibility")
public abstract class ServletOutputStream extends jakarta.servlet.ServletOutputStream {
    public static ServletOutputStream fromJakartaServletOutputStream(jakarta.servlet.ServletOutputStream jakartaServletOutputStream) {
        return new ServletOutputStream() {
            @Override
            public void print(String s) throws IOException {
                jakartaServletOutputStream.print(s);
            }

            @Override
            public void print(boolean b) throws IOException {
                jakartaServletOutputStream.print(b);
            }

            @Override
            public void print(char c) throws IOException {
                jakartaServletOutputStream.print(c);
            }

            @Override
            public void print(int i) throws IOException {
                jakartaServletOutputStream.print(i);
            }

            @Override
            public void print(long l) throws IOException {
                jakartaServletOutputStream.print(l);
            }

            @Override
            public void print(float f) throws IOException {
                jakartaServletOutputStream.print(f);
            }

            @Override
            public void print(double d) throws IOException {
                jakartaServletOutputStream.print(d);
            }

            @Override
            public void println() throws IOException {
                jakartaServletOutputStream.println();
            }

            @Override
            public void println(String s) throws IOException {
                jakartaServletOutputStream.println(s);
            }

            @Override
            public void println(boolean b) throws IOException {
                jakartaServletOutputStream.println(b);
            }

            @Override
            public void println(char c) throws IOException {
                jakartaServletOutputStream.println(c);
            }

            @Override
            public void println(int i) throws IOException {
                jakartaServletOutputStream.println(i);
            }

            @Override
            public void println(long l) throws IOException {
                jakartaServletOutputStream.println(l);
            }

            @Override
            public void println(float f) throws IOException {
                jakartaServletOutputStream.println(f);
            }

            @Override
            public void println(double d) throws IOException {
                jakartaServletOutputStream.println(d);
            }

            @Override
            public void write(byte[] b) throws IOException {
                jakartaServletOutputStream.write(b);
            }

            @Override
            public void write(byte[] b, int off, int len) throws IOException {
                jakartaServletOutputStream.write(b, off, len);
            }

            @Override
            public void flush() throws IOException {
                jakartaServletOutputStream.flush();
            }

            @Override
            public void close() throws IOException {
                jakartaServletOutputStream.close();
            }

            @Override
            public void write(int b) throws IOException {
                jakartaServletOutputStream.write(b);
            }

            @Override
            public boolean isReady() {
                return jakartaServletOutputStream.isReady();
            }

            @Override
            public void setWriteListener(WriteListener writeListener) {
                jakartaServletOutputStream.setWriteListener(writeListener);
            }
        };
    }
}
