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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.zip.GZIPOutputStream;
import org.junit.Ignore;
import org.junit.Test;
import org.jvnet.hudson.test.Issue;
import org.kohsuke.stapler.test.AbstractStaplerTestV4;

public class LargeTextTest extends AbstractStaplerTestV4 {
    ByteArrayOutputStream responseBAOS = new ByteArrayOutputStream();

    @Override
    public void setUp() throws Exception {
        super.setUp();
        responseBAOS.reset();
        when(rawResponse.getWriter()).thenReturn(new PrintWriter(responseBAOS));
    }

    @Issue("JENKINS-37664")
    @Test
    public void writeLogToFromByteBuffer() throws Exception {
        writeLogToWith(byteBuffer());
    }

    @Test
    public void writeLogToFromInterface() throws Exception {
        writeLogToWith(interfaceBased());
    }

    @Test
    public void writeLogToFromFile() throws Exception {
        writeLogToWith(file());
    }

    @Test
    public void writeLogToFromFileWithGzipDetection() throws Exception {
        writeLogToWith(fileWithGzipDetection());
    }

    @Test
    public void writeLogToFromGzFile() throws Exception {
        writeLogToWith(gzFile());
    }

    private void writeLogToWith(BuildLargeText t) throws Exception {
        assertEquals("", tail(t, "", 0));
        assertEquals("abcde", tail(t, "abcde", 0));
        assertEquals("de", tail(t, "abcde", 3));
        assertEquals("e", tail(t, "abcde", 4));
        assertEquals("", tail(t, "abcde", 5));
        try {
            fail(tail(t, "abcde", 6));
        } catch (EOFException x) {
            // right
        }
        try {
            fail(tail(t, "abcde", 99));
        } catch (EOFException x) {
            // right
        }
        // Large string with rest after reading multiples of 1024 bytes.
        String large = "Hello World! ".repeat(1025).trim();
        assertEquals(large, tail(t, large, 0));
        assertEquals(large.substring(1337), tail(t, large, 1337));
    }

    BuildLargeText byteBuffer() {
        return new FromByteBuffer();
    }

    BuildLargeText file() {
        return new FromFile(false);
    }

    BuildLargeText fileWithGzipDetection() {
        return new FromFile(true);
    }

    BuildLargeText gzFile() {
        return new FromGzFile();
    }

    BuildLargeText interfaceBased() {
        return new FromInterface();
    }

    static class FromByteBuffer implements BuildLargeText {
        public LargeText build(String text) throws IOException {
            ByteBuffer bb = new ByteBuffer();
            bb.write(text.getBytes(), 0, text.length());
            return new LargeText(bb, true);
        }

        public void close() {}
    }

    static class FromInterface implements BuildLargeText {
        public LargeText build(String text) throws IOException {
            LargeText.Source src = new LargeText.Source() {
                private final byte[] textBytes = text.getBytes(StandardCharsets.UTF_8);

                class BytesSession extends ByteArrayInputStream implements LargeText.Session {
                    BytesSession() {
                        super(textBytes);
                    }
                }

                public LargeText.Session open() {
                    return new BytesSession();
                }

                public long length() {
                    return textBytes.length;
                }

                public boolean exists() {
                    return true;
                }
            };
            return new LargeText(src, StandardCharsets.UTF_8, true);
        }

        public void close() {}
    }

    static class FromFile implements BuildLargeText {
        private Path path;
        final boolean detectGzip;

        FromFile(boolean detectGzip) {
            this.detectGzip = detectGzip;
        }

        public LargeText build(String text) throws IOException {
            path = Files.createTempFile("stapler-test", ".log");
            Files.write(path, text.getBytes());
            return new LargeText(path.toFile(), true, detectGzip);
        }

        public void close() throws IOException {
            if (path != null) {
                Files.delete(path);
            }
        }
    }

    static class FromGzFile implements BuildLargeText {
        private Path path;

        public LargeText build(String text) throws IOException {
            path = Files.createTempFile("stapler-test", ".log.gz");
            try (var out = new FileOutputStream(path.toFile());
                    var gz = new GZIPOutputStream(out)) {
                gz.write(text.getBytes());
            }
            return new LargeText(path.toFile(), true, true);
        }

        public void close() throws IOException {
            if (path != null) {
                Files.delete(path);
            }
        }
    }

    interface BuildLargeText extends AutoCloseable {
        LargeText build(String text) throws IOException;
    }

    String tail(BuildLargeText buildLargeText, String text, long start) throws Exception {
        try (buildLargeText) {
            LargeText t = buildLargeText.build(text);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            assertEquals(text.length(), t.writeLogTo(start, baos));
            return baos.toString();
        }
    }

    @Issue("#141")
    @Test
    @Ignore
    public void writeLogToWithLargeFile() throws Exception {
        Path path = Files.createTempFile("stapler-test", ".log");
        try {
            long size = Integer.MAX_VALUE + 256L;
            String suffix = "End";
            byte[] suffixBytes = suffix.getBytes(StandardCharsets.US_ASCII);
            try (var raf = new RandomAccessFile(path.toFile(), "rw")) {
                raf.seek(size - suffixBytes.length);
                raf.write(suffixBytes);
            }
            assertEquals(size, path.toFile().length());

            LargeText t = new LargeText(path.toFile(), StandardCharsets.US_ASCII, true);

            try (OutputStream os = OutputStream.nullOutputStream()) {
                long writenCount = t.writeLogTo(0, os);
                assertEquals(size, writenCount);

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                assertEquals(size, t.writeLogTo(size - suffixBytes.length, baos));
                assertEquals(suffix, baos.toString());
            }
        } finally {
            Files.delete(path);
        }
    }

    @Test
    public void doProgressTextLimited() throws Exception {
        String text = "Hello World!";
        final int stop = text.length() - 1;
        ByteBuffer bb = new ByteBuffer() {
            @Override
            public synchronized long length() {
                return stop;
            }
        };
        bb.write(text.getBytes(), 0, text.length());
        LargeText t = new LargeText(bb, true);

        t.doProgressText(request, response);
        assertEquals(text.substring(0, stop), responseBAOS.toString());
    }

    @Test
    public void doProgressTextStreaming() throws Exception {
        String text = "Hello World!";
        ByteBuffer bb = new ByteBuffer();
        bb.write(text.getBytes(), 0, text.length());
        LargeText t = new LargeText(bb, true);
        when(request.getHeader("X-Streaming")).thenReturn("true");

        t.doProgressText(request, response);
        verify(rawResponse).setHeader("X-Streaming", "true");
        assertEquals(text + "\n{\"completed\":true,\"start\":0,\"end\":12}", responseBAOS.toString());
    }

    @Test
    public void doProgressTextStreamingNext() throws Exception {
        String text = "Hello World!";
        ByteBuffer bb = new ByteBuffer();
        bb.write(text.getBytes(), 0, text.length());
        LargeText t = new LargeText(bb, true);
        when(request.getHeader("X-Streaming")).thenReturn("true");
        when(request.getParameter("start")).thenReturn("6");

        t.doProgressText(request, response);
        assertEquals("World!" + "\n{\"completed\":true,\"start\":6,\"end\":12}", responseBAOS.toString());
    }

    @Test
    public void doProgressTextStreamingEnd() throws Exception {
        String text = "Hello World!";
        ByteBuffer bb = new ByteBuffer();
        bb.write(text.getBytes(), 0, text.length());
        LargeText t = new LargeText(bb, true);
        when(request.getHeader("X-Streaming")).thenReturn("true");
        when(request.getParameter("start")).thenReturn("12");

        t.doProgressText(request, response);
        assertEquals("" + "\n{\"completed\":true,\"start\":12,\"end\":12}", responseBAOS.toString());
    }

    @Test
    public void doProgressTextStreamingExtraMetadata() throws Exception {
        String text = "Hello World!";
        ByteBuffer bb = new ByteBuffer();
        bb.write(text.getBytes(), 0, text.length());
        LargeText t = new LargeText(bb, true) {
            @Override
            public long writeLogTo(long start, OutputStream out) throws IOException {
                long r = super.writeLogTo(start, out);
                putStreamingMeta("foo", "42");
                putStreamingMeta("bar", "1337");
                return r;
            }
        };
        when(request.getHeader("X-Streaming")).thenReturn("true");

        t.doProgressText(request, response);
        verify(rawResponse).setHeader("X-Streaming", "true");
        assertEquals(
                text + "\n{\"completed\":true,\"start\":0,\"foo\":\"42\",\"bar\":\"1337\",\"end\":12}",
                responseBAOS.toString());
    }

    @Test
    public void doProgressTextStreamingRollOver() throws Exception {
        String text = "Hello World!";
        ByteBuffer bb = new ByteBuffer();
        bb.write(text.getBytes(), 0, text.length());
        LargeText t = new LargeText(bb, true);
        when(request.getHeader("X-Streaming")).thenReturn("true");
        when(request.getParameter("start")).thenReturn("100");

        t.doProgressText(request, response);
        assertEquals(text + "\n{\"completed\":true,\"start\":0,\"end\":12}", responseBAOS.toString());
    }

    @Test
    public void doProgressTextStreamingTailSmall() throws Exception {
        String text = "Hello\nWorld!";
        ByteBuffer bb = new ByteBuffer();
        bb.write(text.getBytes(), 0, text.length());
        LargeText t = new LargeText(bb, true);
        when(request.getHeader("X-Streaming")).thenReturn("true");
        when(request.getParameter("start")).thenReturn("-100");

        t.doProgressText(request, response);
        assertEquals(text + "\n{\"completed\":true,\"start\":0,\"end\":12}", responseBAOS.toString());
    }

    @Test
    public void doProgressTextStreamingTailFindLF() throws Exception {
        String text = "Hello\nWorld!";
        ByteBuffer bb = new ByteBuffer();
        bb.write(text.getBytes(), 0, text.length());
        LargeText t = new LargeText(bb, true);
        when(request.getHeader("X-Streaming")).thenReturn("true");
        when(request.getParameter("start")).thenReturn("-8");

        t.doProgressText(request, response);
        assertEquals(
                "World!" + "\n{\"completed\":true,\"startFromNewLine\":true,\"start\":6,\"end\":12}",
                responseBAOS.toString());
    }

    @Test
    public void doProgressTextStreamingTailNoLF() throws Exception {
        String text = "Hello World!";
        ByteBuffer bb = new ByteBuffer();
        bb.write(text.getBytes(), 0, text.length());
        LargeText t = new LargeText(bb, true);
        when(request.getHeader("X-Streaming")).thenReturn("true");
        when(request.getParameter("start")).thenReturn("-8");

        t.doProgressText(request, response);
        assertEquals("o World!" + "\n{\"completed\":true,\"start\":4,\"end\":12}", responseBAOS.toString());
    }

    @Test
    public void doProgressTextStreamingTailLarge() throws Exception {
        String text = "x".repeat(9999) + "\nHello World!";
        ByteBuffer bb = new ByteBuffer();
        bb.write(text.getBytes(), 0, text.length());
        LargeText t = new LargeText(bb, true);
        when(request.getHeader("X-Streaming")).thenReturn("true");
        when(request.getParameter("start")).thenReturn("-10000");

        t.doProgressText(request, response);
        assertEquals(
                "Hello World!" + "\n{\"completed\":true,\"startFromNewLine\":true,\"start\":10000,\"end\":10012}",
                responseBAOS.toString());
    }

    @Test
    public void doProgressTextStreamingFetchMoreEdge() throws Exception {
        String text = "x".repeat(9999) + "\nHello World!";
        ByteBuffer bb = new ByteBuffer();
        bb.write(text.getBytes(), 0, text.length());
        LargeText t = new LargeText(bb, true);
        when(request.getHeader("X-Streaming")).thenReturn("true");
        when(request.getParameter("start")).thenReturn("100");
        when(request.getParameter("searchNewLineUntil")).thenReturn("10000");

        t.doProgressText(request, response);
        assertEquals(
                text.substring(100) + "\n{\"completed\":true,\"start\":100,\"end\":10012}", responseBAOS.toString());
    }

    @Test
    public void doProgressTextStreamingFetchMoreLF() throws Exception {
        String text = "x".repeat(9999) + "\nHello World!";
        ByteBuffer bb = new ByteBuffer();
        bb.write(text.getBytes(), 0, text.length());
        LargeText t = new LargeText(bb, true);
        when(request.getHeader("X-Streaming")).thenReturn("true");
        when(request.getParameter("start")).thenReturn("100");
        when(request.getParameter("searchNewLineUntil")).thenReturn("10002");

        t.doProgressText(request, response);
        assertEquals(
                "Hello World!" + "\n{\"completed\":true,\"startFromNewLine\":true,\"start\":10000,\"end\":10012}",
                responseBAOS.toString());
    }

    @Test
    public void doProgressTextStreamingFetchMoreNoLF() throws Exception {
        String text = "x".repeat(9999) + "\nHello World!";
        ByteBuffer bb = new ByteBuffer();
        bb.write(text.getBytes(), 0, text.length());
        LargeText t = new LargeText(bb, true);
        when(request.getHeader("X-Streaming")).thenReturn("true");
        when(request.getParameter("start")).thenReturn("100");
        when(request.getParameter("searchNewLineUntil")).thenReturn("200");

        t.doProgressText(request, response);
        assertEquals(
                text.substring(100) + "\n{\"completed\":true,\"start\":100,\"end\":10012}", responseBAOS.toString());
    }

    @Test
    public void doProgressTextStreamingTailRolledOver() throws Exception {
        String text = "Hello\nWorld!";
        ByteBuffer bb = new ByteBuffer() {
            @Override
            public synchronized long length() {
                return super.length() + 100;
            }
        };
        bb.write(text.getBytes(), 0, text.length());
        LargeText t = new LargeText(bb, true);
        when(request.getHeader("X-Streaming")).thenReturn("true");
        when(request.getParameter("start")).thenReturn("-8");

        t.doProgressText(request, response);
        assertEquals(text + "\n{\"completed\":true,\"start\":0,\"end\":12}", responseBAOS.toString());
    }

    @Test
    public void doProgressTextStreamingMissing() throws Exception {
        // Create a temporary file to ensure that the given name does not exist.
        File missing = Files.createTempFile("stapler-test-missing", ".txt").toFile();
        assertTrue(missing.delete());
        assertFalse(missing.exists());

        LargeText t = new LargeText(missing, true);
        when(request.getHeader("X-Streaming")).thenReturn("true");
        when(request.getParameter("start")).thenReturn("-8");

        t.doProgressText(request, response);
        assertEquals("" + "\n{\"completed\":true,\"start\":0,\"end\":0}", responseBAOS.toString());
    }

    @Test
    public void doProgressTextStreamingInfinite() throws Exception {
        ByteBuffer bb = new ByteBuffer() {
            @Override
            public InputStream newInputStream() {
                return new InputStream() {
                    @Override
                    public int read() throws IOException {
                        return 'x';
                    }

                    @Override
                    public int read(@NonNull byte[] b) {
                        Arrays.fill(b, (byte) 'x');
                        return b.length;
                    }
                };
            }
        };
        bb.write(42); // populate the initial byte to trigger streaming.
        LargeText t = new LargeText(bb, false);
        when(request.getHeader("X-Streaming")).thenReturn("true");
        when(request.getParameter("start")).thenReturn("0");

        t.doProgressText(request, response);
        assertEquals(
                "x" + "\n{\"completed\":false,\"start\":0,\"end\":1}", responseBAOS.toString());
    }
}
