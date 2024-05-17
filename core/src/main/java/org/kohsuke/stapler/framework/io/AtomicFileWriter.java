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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * Buffered {@link FileWriter} that uses UTF-8.
 *
 * <p>
 * The write operation is atomic when used for overwriting;
 * it either leaves the original file intact, or it completely rewrites it with new contents.
 *
 * @author Kohsuke Kawaguchi
 */
public class AtomicFileWriter extends Writer {

    private final Writer core;
    private final File tmpFile;
    private final File destFile;

    @SuppressFBWarnings(value = "PATH_TRAVERSAL_IN", justification = "Protected by checks at other layers.")
    public AtomicFileWriter(File f) throws IOException {
        tmpFile = File.createTempFile("atomic", null, f.getParentFile());
        destFile = f;
        core = new BufferedWriter(new OutputStreamWriter(
                Files.newOutputStream(
                        tmpFile.toPath(),
                        StandardOpenOption.CREATE,
                        StandardOpenOption.TRUNCATE_EXISTING, // empty file already created by createTempFile
                        StandardOpenOption.WRITE),
                StandardCharsets.UTF_8));
    }

    @Override
    public void write(int c) throws IOException {
        core.write(c);
    }

    @Override
    public void write(String str, int off, int len) throws IOException {
        core.write(str, off, len);
    }

    @Override
    public void write(char[] cbuf, int off, int len) throws IOException {
        core.write(cbuf, off, len);
    }

    @Override
    public void flush() throws IOException {
        core.flush();
    }

    @Override
    public void close() throws IOException {
        core.close();
    }

    public void commit() throws IOException {
        close();
        Path destFilePath = destFile.toPath();
        if (Files.exists(destFilePath)) {
            Files.delete(destFilePath);
        }
        Files.move(tmpFile.toPath(), destFilePath);
    }

    /**
     * Until the data is committed, this file captures
     * the written content.
     */
    public File getTemporaryFile() {
        return tmpFile;
    }
}
