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

import org.apache.commons.io.output.CountingOutputStream;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;

/**
 * Represents a large text data.
 *
 * <p>
 * This class defines methods for handling progressive text update.
 *
 * <h2>Usage</h2>
 * <p>
 *
 * @author Kohsuke Kawaguchi
 */
public class LargeText {
    /**
     * Represents the data source of this text.
     */
    private interface Source {
        Session open() throws IOException;
        long length();
        boolean exists();
    }
    private final Source source;

    protected final Charset charset;

    private volatile boolean completed;

    public LargeText(File file, boolean completed) {
        this(file,Charset.defaultCharset(),completed);
    }

    public LargeText(final File file, Charset charset, boolean completed) {
        this.charset = charset;
        this.source = new Source() {
            public Session open() throws IOException {
                return new FileSession(file);
            }

            public long length() {
                return file.length();
            }

            public boolean exists() {
                return file.exists();
            }
        };
        this.completed = completed;
    }

    public LargeText(ByteBuffer memory, boolean completed) {
        this(memory,Charset.defaultCharset(),completed);
    }

    public LargeText(final ByteBuffer memory, Charset charset, boolean completed) {
        this.charset = charset;
        this.source = new Source() {
            public Session open() throws IOException {
                return new BufferSession(memory);
            }

            public long length() {
                return memory.length();
            }

            public boolean exists() {
                return true;
            }
        };
        this.completed = completed;
    }

    public void markAsComplete() {
        completed = true;
    }

    public boolean isComplete() {
        return completed;
    }

    public long length() {
        return source.length();
    }

    /**
     * Returns {@link Reader} for reading the raw bytes.
     */
    public Reader readAll() throws IOException {
        return new InputStreamReader(new InputStream() {
            final Session session = source.open();
            public int read() throws IOException {
                byte[] buf = new byte[1];
                int n = session.read(buf);
                if(n==1)    return buf[0];
                else        return -1; // EOF
            }

            public int read(byte[] buf, int off, int len) throws IOException {
                return session.read(buf,off,len);
            }

            public void close() throws IOException {
                session.close();
            }
        },charset);
    }

    public long writeLogTo(long start, Writer w) throws IOException {
        return writeLogTo(start, new WriterOutputStream(w, charset));
    }

    /**
     * Writes the tail portion of the file to the {@link OutputStream}.
     *
     * @param start
     *      The byte offset in the input file where the write operation starts.
     *
     * @return
     *      if the file is still being written, this method writes the file
     *      until the last newline character and returns the offset to start
     *      the next write operation.
     */
    public long writeLogTo(long start, OutputStream out) throws IOException {
        CountingOutputStream os = new CountingOutputStream(out);

        Session f = source.open();
        f.skip(start);

        if(completed) {
            // write everything till EOF
            byte[] buf = new byte[1024];
            int sz;
            while((sz=f.read(buf))>=0)
                os.write(buf,0,sz);
        } else {
            ByteBuf buf = new ByteBuf(null,f);
            HeadMark head = new HeadMark(buf);
            TailMark tail = new TailMark(buf);

            while(tail.moveToNextLine(f)) {
                head.moveTo(tail,os);
            }
            head.finish(os);
        }

        f.close();
        os.flush();

        return os.getCount()+start;
    }

    /**
     * Implements the progressive text handling.
     * This method is used as a "web method" with progressiveText.jelly.
     */
    public void doProgressText(StaplerRequest req, StaplerResponse rsp) throws IOException {
        setContentType(rsp);
        rsp.setStatus(HttpServletResponse.SC_OK);

        if(!source.exists()) {
            // file doesn't exist yet
            rsp.addHeader("X-Text-Size","0");
            rsp.addHeader("X-More-Data","true");
            return;
        }

        long start = 0;
        String s = req.getParameter("start");
        if(s!=null)
            start = Long.parseLong(s);

        if(source.length() < start )
            start = 0;  // text rolled over

        CharSpool spool = new CharSpool();
        long r = writeLogTo(start,spool);

        rsp.addHeader("X-Text-Size",String.valueOf(r));
        if(!completed)
            rsp.addHeader("X-More-Data","true");

        Writer w = createWriter(req, rsp, r - start);
        spool.writeTo(new LineEndNormalizingWriter(w));
        w.close();
    }

    protected void setContentType(StaplerResponse rsp) {
        rsp.setContentType("text/plain;charset=UTF-8");
    }

    protected Writer createWriter(StaplerRequest req, StaplerResponse rsp, long size) throws IOException {
        // when sending big text, try compression. don't bother if it's small
        if(size >4096)
            return rsp.getCompressedWriter(req);
        else
            return rsp.getWriter();
    }

    /**
     * Points to a byte in the buffer.
     */
    private static class Mark {
        protected ByteBuf buf;
        protected int pos;

        public Mark(ByteBuf buf) {
            this.buf = buf;
        }
    }

    /**
     * Points to the start of the region that's not committed
     * to the output yet.
     */
    private static final class HeadMark extends Mark {
        public HeadMark(ByteBuf buf) {
            super(buf);
        }

        /**
         * Moves this mark to 'that' mark, and writes the data
         * to {@link OutputStream} if necessary.
         */
        void moveTo(Mark that, OutputStream os) throws IOException {
        	ByteBuf previous = this.buf;
            while(this.buf!=that.buf) {
                os.write(buf.buf,0,buf.size);
                previous = buf;
                buf = buf.next;
                pos = 0;
            }
            // don't want to keep already writen bits, can lead to OOM
            previous.next = null;
            this.pos = that.pos;
        }

        void finish(OutputStream os) throws IOException {
            os.write(buf.buf,0,pos);
        }
    }

    /**
     * Points to the end of the region.
     */
    private static final class TailMark extends Mark {
        public TailMark(ByteBuf buf) {
            super(buf);
        }

        boolean moveToNextLine(Session f) throws IOException {
            while(true) {
                while(pos==buf.size) {
                    if(!buf.isFull()) {
                        // read until EOF
                        return false;
                    } else {
                        // read into the next buffer
                        buf = new ByteBuf(buf,f);
                        pos = 0;
                    }
                }
                byte b = buf.buf[pos++];
                if(b=='\r' || b=='\n')
                    return true;
            }
        }
    }

    /**
     * Variable length byte buffer implemented as a linked list of fixed length buffer.
     */
    private static final class ByteBuf {
        private final byte[] buf = new byte[1024];
        private int size = 0;
        private ByteBuf next;

        public ByteBuf(ByteBuf previous, Session f) throws IOException {
            if(previous!=null) {
                assert previous.next==null;
                previous.next = this;
            }

            while(!this.isFull()) {
                int chunk = f.read(buf, size, buf.length - size);
                if(chunk==-1)
                    return;
                size+= chunk;
            }
        }

        public boolean isFull() {
            return buf.length==size;
        }
    }

    /**
     * Represents the read session of the {@link Source}.
     * Methods generally follow the contracts of {@link InputStream}.
     */
    private interface Session {
        void close() throws IOException;
        void skip(long start) throws IOException;
        int read(byte[] buf) throws IOException;
        int read(byte[] buf, int offset, int length) throws IOException;
    }

    /**
     * {@link Session} implementation over {@link RandomAccessFile}.
     */
    private static final class FileSession implements Session {
        private final RandomAccessFile file;

        public FileSession(File file) throws IOException {
            this.file = new RandomAccessFile(file,"r");
        }

        public void close() throws IOException {
            file.close();
        }

        public void skip(long start) throws IOException {
            file.seek(file.getFilePointer()+start);
        }

        public int read(byte[] buf) throws IOException {
            return file.read(buf);
        }

        public int read(byte[] buf, int offset, int length) throws IOException {
            return file.read(buf,offset,length);
        }
    }

    /**
     * {@link Session} implementation over {@link ByteBuffer}.
     */
    private static final class BufferSession implements Session {
        private final InputStream in;

        public BufferSession(ByteBuffer buf) {
            this.in = buf.newInputStream();
        }


        public void close() throws IOException {
            in.close();
        }

        public void skip(long start) throws IOException {
            while (start>0)
                start -= in.skip(start);
        }

        public int read(byte[] buf) throws IOException {
            return in.read(buf);
        }

        public int read(byte[] buf, int offset, int length) throws IOException {
            return in.read(buf,offset,length);
        }
    }
}
