package org.kohsuke.stapler.compression;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;

/**
 * {@link ServletOutputStream} that writes to the specified output stream.
 *
 * @author Kohsuke Kawaguchi
 */
public class FilterServletOutputStream extends ServletOutputStream {
    private final OutputStream out;
    private final ServletOutputStream realSream;

    /**
     * Whether the stream is closed; implicitly initialized to false.
     */
    private volatile boolean closed;

    /**
     * Object used to prevent a race on the 'closed' instance variable.
     */
    private final Object closeLock = new Object();

    /**
     * Constructs a new {@link FilterOutputStream}.
     * @param out the stream that sits above the realStream, performing some filtering.  This must be eventually delegating eventual writes to {@code realStream}.
     * @param realStream the actual underlying ServletOutputStream from the container.  Used to check the {@link #isReady()} state and to add {@link WriteListener}s.
     */
    public FilterServletOutputStream(OutputStream out, ServletOutputStream realStream) {
        this.out = out;
        this.realSream = realStream;
    }

    @Override
    public void write(int b) throws IOException {
        out.write(b);
    }

    @Override
    public void write(byte[] b) throws IOException {
        out.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        out.write(b, off, len);
    }

    @Override
    public void close() throws IOException {
        if (closed) {
            return;
        }
        synchronized (closeLock) {
            if (closed) {
                return;
            }
            closed = true;
        }

        Throwable flushException = null;
        try {
            flush();
        } catch (Throwable e) {
            flushException = e;
            throw e;
        } finally {
            if (flushException == null) {
                out.close();
            } else {
                try {
                    out.close();
                } catch (Throwable closeException) {
                    if (flushException != closeException) {
                        closeException.addSuppressed(flushException);
                    }
                    throw closeException;
                }
            }
        }
    }

    @Override
    public void flush() throws IOException {
        out.flush();
    }

    @Override
    public boolean isReady() {
        return realSream.isReady();
    }

    @Override
    public void setWriteListener(WriteListener writeListener) {
        realSream.setWriteListener(writeListener);
    }
}
