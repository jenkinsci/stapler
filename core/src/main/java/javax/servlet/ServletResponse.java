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
import java.io.PrintWriter;
import java.util.Locale;

public interface ServletResponse {
    String getCharacterEncoding();

    String getContentType();

    ServletOutputStream getOutputStream() throws IOException;

    PrintWriter getWriter() throws IOException;

    void setCharacterEncoding(String charset);

    void setContentLength(int len);

    void setContentLengthLong(long len);

    void setContentType(String type);

    void setBufferSize(int size);

    int getBufferSize();

    void flushBuffer() throws IOException;

    void resetBuffer();

    boolean isCommitted();

    void reset();

    void setLocale(Locale loc);

    Locale getLocale();

    default jakarta.servlet.ServletResponse toJakartaServletResponse() {
        return new jakarta.servlet.ServletResponse() {
            @Override
            public String getCharacterEncoding() {
                return ServletResponse.this.getCharacterEncoding();
            }

            @Override
            public String getContentType() {
                return ServletResponse.this.getContentType();
            }

            @Override
            public jakarta.servlet.ServletOutputStream getOutputStream() throws IOException {
                return ServletResponse.this.getOutputStream();
            }

            @Override
            public PrintWriter getWriter() throws IOException {
                return ServletResponse.this.getWriter();
            }

            @Override
            public void setCharacterEncoding(String s) {
                ServletResponse.this.setCharacterEncoding(s);
            }

            @Override
            public void setContentLength(int i) {
                ServletResponse.this.setContentLength(i);
            }

            @Override
            public void setContentLengthLong(long l) {
                ServletResponse.this.setContentLengthLong(l);
            }

            @Override
            public void setContentType(String s) {
                ServletResponse.this.setContentType(s);
            }

            @Override
            public void setBufferSize(int i) {
                ServletResponse.this.setBufferSize(i);
            }

            @Override
            public int getBufferSize() {
                return ServletResponse.this.getBufferSize();
            }

            @Override
            public void flushBuffer() throws IOException {
                ServletResponse.this.flushBuffer();
            }

            @Override
            public void resetBuffer() {
                ServletResponse.this.resetBuffer();
            }

            @Override
            public boolean isCommitted() {
                return ServletResponse.this.isCommitted();
            }

            @Override
            public void reset() {
                ServletResponse.this.reset();
            }

            @Override
            public void setLocale(Locale locale) {
                ServletResponse.this.setLocale(locale);
            }

            @Override
            public Locale getLocale() {
                return ServletResponse.this.getLocale();
            }
        };
    }

    static ServletResponse fromJakartaServletResponse(jakarta.servlet.ServletResponse from) {
        return new ServletResponse() {
            @Override
            public String getCharacterEncoding() {
                return from.getCharacterEncoding();
            }

            @Override
            public String getContentType() {
                return from.getContentType();
            }

            @Override
            public ServletOutputStream getOutputStream() throws IOException {
                // TODO
                throw new UnsupportedOperationException();
            }

            @Override
            public PrintWriter getWriter() throws IOException {
                return from.getWriter();
            }

            @Override
            public void setCharacterEncoding(String charset) {
                from.setCharacterEncoding(charset);
            }

            @Override
            public void setContentLength(int len) {
                from.setContentLength(len);
            }

            @Override
            public void setContentLengthLong(long len) {
                from.setContentLengthLong(len);
            }

            @Override
            public void setContentType(String type) {
                from.setContentType(type);
            }

            @Override
            public void setBufferSize(int size) {
                from.setBufferSize(size);
            }

            @Override
            public int getBufferSize() {
                return from.getBufferSize();
            }

            @Override
            public void flushBuffer() throws IOException {
                from.flushBuffer();
            }

            @Override
            public void resetBuffer() {
                from.resetBuffer();
            }

            @Override
            public boolean isCommitted() {
                return from.isCommitted();
            }

            @Override
            public void reset() {
                from.reset();
            }

            @Override
            public void setLocale(Locale loc) {
                from.setLocale(loc);
            }

            @Override
            public Locale getLocale() {
                return from.getLocale();
            }

            @Override
            public jakarta.servlet.ServletResponse toJakartaServletResponse() {
                return from;
            }
        };
    }
}
