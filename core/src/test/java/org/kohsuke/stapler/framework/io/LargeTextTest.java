/*
 * Copyright (c) 2016, CloudBees, Inc
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package org.kohsuke.stapler.framework.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.common.base.Strings;
import org.apache.commons.io.output.NullOutputStream;
import org.junit.Test;
import org.jvnet.hudson.test.Issue;

public class LargeTextTest {

    @Issue("JENKINS-37664")
    @Test
    public void writeLogTo() throws Exception {
        assertEquals("", tail("", 0));
        assertEquals("abcde", tail("abcde", 0));
        assertEquals("de", tail("abcde", 3));
        assertEquals("", tail("abcde", 5));
        try {
            fail(tail("abcde", 6));
        } catch (EOFException x) {
            // right
        }
        try {
            fail(tail("abcde", 99));
        } catch (EOFException x) {
            // right
        }
    }

    String tail(String text, long start) throws IOException {
        LargeText t;
        try (ByteBuffer bb = new ByteBuffer()) {
            bb.write(text.getBytes(), 0, text.length());

            t = new LargeText(bb, true);
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        assertEquals(text.length(), t.writeLogTo(start, baos));
        return baos.toString();
    }

    @Issue("#141")
    @Test
    public void writeLogToWithLargeFile() throws Exception {
        Path path = Files.createTempFile("stapler-test", ".log.gz");
        long size = Integer.MAX_VALUE + 256L;
        writeLargeFile(path, size);

        LargeText t = new LargeText(path.toFile(), StandardCharsets.US_ASCII, true);

        try (OutputStream os = new NullOutputStream()) {
            long writenCount = t.writeLogTo(0, os);

            assertEquals(size, writenCount);
        }

        Files.delete(path);
    }

    private void writeLargeFile(Path path, long size) {
        // Write the same data over and over again, so the bytes written is high, but the file is
        // actually very small
        int chunkSize = 1024;
        byte[] bytesChunk = Strings.repeat("0", chunkSize).getBytes(StandardCharsets.US_ASCII);
        try (OutputStream stream = new FileOutputStream(path.toFile())) {
            long remaining = size;
            while (remaining > chunkSize) {
                stream.write(bytesChunk);
                remaining -= chunkSize;
            }
            stream.write(bytesChunk, 0, (int) remaining);
            stream.flush();
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

}
