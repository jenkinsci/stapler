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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import org.apache.commons.fileupload2.core.FileItemHeadersProvider;

/**
 * <p> This class represents a file or form item that was received within a
 * <code>multipart/form-data</code> POST request.
 *
 * <p> After retrieving an instance of this class from a {@code
 * org.apache.commons.fileupload.FileUpload FileUpload} instance (see
 * {@code org.apache.commons.fileupload.servlet.ServletFileUpload
 * #parseRequest(javax.servlet.http.HttpServletRequest)}), you may
 * either request all contents of the file at once using {@link #get()} or
 * request an {@link InputStream} with
 * {@link #getInputStream()} and process the file without attempting to load
 * it into memory, which may come handy with large files.
 *
 * <p> While this interface does not extend
 * <code>javax.activation.DataSource</code> per se (to avoid a seldom used
 * dependency), several of the defined methods are specifically defined with
 * the same signatures as methods in that interface. This allows an
 * implementation of this interface to also implement
 * <code>javax.activation.DataSource</code> with minimal additional work.
 */
public interface FileItem {

    // ------------------------------- Methods from javax.activation.DataSource

    /**
     * Returns an {@link InputStream} that can be
     * used to retrieve the contents of the file.
     *
     * @return An {@link InputStream} that can be
     *         used to retrieve the contents of the file.
     *
     * @throws IOException if an error occurs.
     */
    InputStream getInputStream() throws IOException;

    /**
     * Returns the content type passed by the browser or <code>null</code> if
     * not defined.
     *
     * @return The content type passed by the browser or <code>null</code> if
     *         not defined.
     */
    String getContentType();

    /**
     * Returns the original filename in the client's filesystem, as provided by
     * the browser (or other client software). In most cases, this will be the
     * base file name, without path information. However, some clients, such as
     * the Opera browser, do include path information.
     *
     * @return The original filename in the client's filesystem.
     * @throws InvalidFileNameException The file name contains a NUL character,
     *   which might be an indicator of a security attack. If you intend to
     *   use the file name anyways, catch the exception and use
     *   InvalidFileNameException#getName().
     */
    String getName();

    // ------------------------------------------------------- FileItem methods

    /**
     * Provides a hint as to whether or not the file contents will be read
     * from memory.
     *
     * @return <code>true</code> if the file contents will be read from memory;
     *         <code>false</code> otherwise.
     */
    boolean isInMemory();

    /**
     * Returns the size of the file item.
     *
     * @return The size of the file item, in bytes.
     */
    long getSize();

    /**
     * Returns the contents of the file item as an array of bytes.
     *
     * @return The contents of the file item as an array of bytes.
     */
    byte[] get();

    /**
     * Returns the contents of the file item as a String, using the specified
     * encoding.  This method uses {@link #get()} to retrieve the
     * contents of the item.
     *
     * @param encoding The character encoding to use.
     *
     * @return The contents of the item, as a string.
     *
     * @throws UnsupportedEncodingException if the requested character
     *                                      encoding is not available.
     */
    String getString(String encoding) throws UnsupportedEncodingException;

    /**
     * Returns the contents of the file item as a String, using the default
     * character encoding.  This method uses {@link #get()} to retrieve the
     * contents of the item.
     *
     * @return The contents of the item, as a string.
     */
    String getString();

    /**
     * A convenience method to write an uploaded item to disk. The client code
     * is not concerned with whether or not the item is stored in memory, or on
     * disk in a temporary location. They just want to write the uploaded item
     * to a file.
     * <p>
     * This method is not guaranteed to succeed if called more than once for
     * the same item. This allows a particular implementation to use, for
     * example, file renaming, where possible, rather than copying all of the
     * underlying data, thus gaining a significant performance benefit.
     *
     * @param file The <code>File</code> into which the uploaded item should
     *             be stored.
     *
     * @throws Exception if an error occurs.
     */
    void write(File file) throws Exception;

    /**
     * Deletes the underlying storage for a file item, including deleting any
     * associated temporary disk file. Although this storage will be deleted
     * automatically when the <code>FileItem</code> instance is garbage
     * collected, this method can be used to ensure that this is done at an
     * earlier time, thus preserving system resources.
     */
    void delete();

    /**
     * Returns the name of the field in the multipart form corresponding to
     * this file item.
     *
     * @return The name of the form field.
     */
    String getFieldName();

    /**
     * Sets the field name used to reference this file item.
     *
     * @param name The name of the form field.
     */
    void setFieldName(String name);

    /**
     * Determines whether or not a <code>FileItem</code> instance represents
     * a simple form field.
     *
     * @return <code>true</code> if the instance represents a simple form
     *         field; <code>false</code> if it represents an uploaded file.
     */
    boolean isFormField();

    /**
     * Specifies whether or not a <code>FileItem</code> instance represents
     * a simple form field.
     *
     * @param state <code>true</code> if the instance represents a simple form
     *              field; <code>false</code> if it represents an uploaded file.
     */
    void setFormField(boolean state);

    /**
     * Returns an {@link OutputStream} that can
     * be used for storing the contents of the file.
     *
     * @return An {@link OutputStream} that can be used
     *         for storing the contensts of the file.
     *
     * @throws IOException if an error occurs.
     */
    OutputStream getOutputStream() throws IOException;

    /**
     * Returns the collection of headers defined locally within this item.
     *
     * @return the {@link FileItemHeaders} present for this item.
     */
    FileItemHeaders getHeaders();

    /**
     * Sets the headers read from within an item.  Implementations of
     * {@link FileItem} or {@code FileItemStream} should implement this
     * interface to be able to get the raw headers found within the item
     * header block.
     *
     * @param headers the instance that holds onto the headers
     *         for this instance.
     */
    void setHeaders(FileItemHeaders headers);

    default org.apache.commons.fileupload2.core.FileItem toFileUpload2FileItem() {
        return new org.apache.commons.fileupload2.core.FileItem() {
            @Override
            public org.apache.commons.fileupload2.core.FileItemHeaders getHeaders() {
                return FileItem.this.getHeaders().toFileUpload2FileItemHeaders();
            }

            @Override
            public FileItemHeadersProvider setHeaders(org.apache.commons.fileupload2.core.FileItemHeaders headers) {
                FileItem.this.setHeaders(FileItemHeaders.fromFileUpload2FileItemHeaders(headers));
                return this;
            }

            @Override
            public org.apache.commons.fileupload2.core.FileItem delete() throws IOException {
                try {
                    FileItem.this.delete();
                } catch (UncheckedIOException e) {
                    throw e.getCause();
                }
                return this;
            }

            @Override
            public byte[] get() {
                return FileItem.this.get();
            }

            @Override
            public String getContentType() {
                return FileItem.this.getContentType();
            }

            @Override
            public String getFieldName() {
                return FileItem.this.getFieldName();
            }

            @Override
            public InputStream getInputStream() throws IOException {
                return FileItem.this.getInputStream();
            }

            @Override
            @SuppressFBWarnings(value = "FILE_UPLOAD_FILENAME", justification = "for compatibility")
            public String getName() {
                return FileItem.this.getName();
            }

            @Override
            public OutputStream getOutputStream() throws IOException {
                return FileItem.this.getOutputStream();
            }

            @Override
            public long getSize() {
                return FileItem.this.getSize();
            }

            @Override
            public String getString() {
                return FileItem.this.getString();
            }

            @Override
            public String getString(Charset toCharset) throws IOException {
                return FileItem.this.getString(toCharset.name());
            }

            @Override
            public boolean isFormField() {
                return FileItem.this.isFormField();
            }

            @Override
            public boolean isInMemory() {
                return FileItem.this.isInMemory();
            }

            @Override
            public org.apache.commons.fileupload2.core.FileItem setFieldName(String name) {
                FileItem.this.setFieldName(name);
                return this;
            }

            @Override
            public org.apache.commons.fileupload2.core.FileItem setFormField(boolean state) {
                FileItem.this.setFormField(state);
                return this;
            }

            @Override
            public org.apache.commons.fileupload2.core.FileItem write(Path file) throws IOException {
                try {
                    FileItem.this.write(file.toFile());
                } catch (UncheckedIOException e) {
                    throw e.getCause();
                } catch (IOException e) {
                    throw e;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                return this;
            }
        };
    }

    static FileItem fromFileUpload2FileItem(org.apache.commons.fileupload2.core.FileItem from) {
        return new FileItem() {

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
            public boolean isInMemory() {
                return from.isInMemory();
            }

            @Override
            public long getSize() {
                return from.getSize();
            }

            @Override
            public byte[] get() {
                return from.get();
            }

            @Override
            public String getString(String encoding) throws UnsupportedEncodingException {
                try {
                    return from.getString(Charset.forName(encoding));
                } catch (UnsupportedEncodingException e) {
                    throw e;
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }

            @Override
            public String getString() {
                return from.getString();
            }

            @Override
            public void write(File file) throws Exception {
                from.write(file.toPath());
            }

            @Override
            public void delete() {
                try {
                    from.delete();
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }

            @Override
            public String getFieldName() {
                return from.getFieldName();
            }

            @Override
            @SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT", justification = "for compatibility")
            public void setFieldName(String name) {
                from.setFieldName(name);
            }

            @Override
            public boolean isFormField() {
                return from.isFormField();
            }

            @Override
            @SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT", justification = "for compatibility")
            public void setFormField(boolean state) {
                from.setFormField(state);
            }

            @Override
            public OutputStream getOutputStream() throws IOException {
                return from.getOutputStream();
            }

            @Override
            public FileItemHeaders getHeaders() {
                return FileItemHeaders.fromFileUpload2FileItemHeaders(from.getHeaders());
            }

            @Override
            public void setHeaders(FileItemHeaders headers) {
                from.setHeaders(headers.toFileUpload2FileItemHeaders());
            }

            @Override
            public org.apache.commons.fileupload2.core.FileItem toFileUpload2FileItem() {
                return from;
            }
        };
    }
}
