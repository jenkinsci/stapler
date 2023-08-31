/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package javax.servlet.http;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

public interface Part {
    InputStream getInputStream() throws IOException;

    String getContentType();

    String getName();

    String getSubmittedFileName();

    long getSize();

    void write(String fileName) throws IOException;

    void delete() throws IOException;

    String getHeader(String name);

    Collection<String> getHeaders(String name);

    Collection<String> getHeaderNames();

    default jakarta.servlet.http.Part toJakartaPart() {
        return new jakarta.servlet.http.Part() {
            @Override
            public InputStream getInputStream() throws IOException {
                return Part.this.getInputStream();
            }

            @Override
            public String getContentType() {
                return Part.this.getContentType();
            }

            @Override
            public String getName() {
                return Part.this.getName();
            }

            @Override
            public String getSubmittedFileName() {
                return Part.this.getSubmittedFileName();
            }

            @Override
            public long getSize() {
                return Part.this.getSize();
            }

            @Override
            public void write(String fileName) throws IOException {
                Part.this.write(fileName);
            }

            @Override
            public void delete() throws IOException {
                Part.this.delete();
            }

            @Override
            public String getHeader(String name) {
                return Part.this.getHeader(name);
            }

            @Override
            public Collection<String> getHeaders(String name) {
                return Part.this.getHeaders(name);
            }

            @Override
            public Collection<String> getHeaderNames() {
                return Part.this.getHeaderNames();
            }
        };
    }

    static Part fromJakartaPart(jakarta.servlet.http.Part from) {
        return new Part() {
            @Override
            public InputStream getInputStream() throws IOException {
                return from.getInputStream();
            }

            @Override
            public String getContentType() {
                return from.getContentType();
            }

            @Override
            public String getName() {
                return from.getName();
            }

            @Override
            public String getSubmittedFileName() {
                return from.getSubmittedFileName();
            }

            @Override
            public long getSize() {
                return from.getSize();
            }

            @Override
            public void write(String fileName) throws IOException {
                from.write(fileName);
            }

            @Override
            public void delete() throws IOException {
                from.delete();
            }

            @Override
            public String getHeader(String name) {
                return from.getHeader(name);
            }

            @Override
            public Collection<String> getHeaders(String name) {
                return from.getHeaders(name);
            }

            @Override
            public Collection<String> getHeaderNames() {
                return from.getHeaderNames();
            }

            @Override
            public jakarta.servlet.http.Part toJakartaPart() {
                return from;
            }
        };
    }
}
