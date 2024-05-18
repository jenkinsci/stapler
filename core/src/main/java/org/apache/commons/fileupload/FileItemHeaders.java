/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.fileupload;

import java.util.Iterator;

/**
 * <p> This class provides support for accessing the headers for a file or form
 * item that was received within a <code>multipart/form-data</code> POST
 * request.</p>
 *
 * @since 1.2.1
 */
public interface FileItemHeaders {

    /**
     * Returns the value of the specified part header as a <code>String</code>.
     * <p>
     * If the part did not include a header of the specified name, this method
     * return <code>null</code>.  If there are multiple headers with the same
     * name, this method returns the first header in the item.  The header
     * name is case insensitive.
     *
     * @param name a <code>String</code> specifying the header name
     * @return a <code>String</code> containing the value of the requested
     *         header, or <code>null</code> if the item does not have a header
     *         of that name
     */
    String getHeader(String name);

    /**
     * <p>
     * Returns all the values of the specified item header as an
     * <code>Iterator</code> of <code>String</code> objects.
     * </p>
     * <p>
     * If the item did not include any headers of the specified name, this
     * method returns an empty <code>Iterator</code>. The header name is
     * case insensitive.
     * </p>
     *
     * @param name a <code>String</code> specifying the header name
     * @return an <code>Iterator</code> containing the values of the
     *         requested header. If the item does not have any headers of
     *         that name, return an empty <code>Iterator</code>
     */
    Iterator<String> getHeaders(String name);

    /**
     * <p>
     * Returns an <code>Iterator</code> of all the header names.
     * </p>
     *
     * @return an <code>Iterator</code> containing all of the names of
     * headers provided with this file item. If the item does not have
     * any headers return an empty <code>Iterator</code>
     */
    Iterator<String> getHeaderNames();

    default org.apache.commons.fileupload2.core.FileItemHeaders toFileUpload2FileItemHeaders() {
        return new org.apache.commons.fileupload2.core.FileItemHeaders() {
            @Override
            public void addHeader(String name, String value) {
                throw new UnsupportedOperationException();
            }

            @Override
            public String getHeader(String name) {
                return FileItemHeaders.this.getHeader(name);
            }

            @Override
            public Iterator<String> getHeaderNames() {
                return FileItemHeaders.this.getHeaderNames();
            }

            @Override
            public Iterator<String> getHeaders(String name) {
                return FileItemHeaders.this.getHeaders(name);
            }
        };
    }

    static FileItemHeaders fromFileUpload2FileItemHeaders(org.apache.commons.fileupload2.core.FileItemHeaders from) {
        return new FileItemHeaders() {
            @Override
            public String getHeader(String name) {
                return from.getHeader(name);
            }

            @Override
            public Iterator<String> getHeaders(String name) {
                return from.getHeaders(name);
            }

            @Override
            public Iterator<String> getHeaderNames() {
                return from.getHeaderNames();
            }

            @Override
            public org.apache.commons.fileupload2.core.FileItemHeaders toFileUpload2FileItemHeaders() {
                return from;
            }
        };
    }
}
