package org.kohsuke.stapler.compression;

import javax.servlet.ServletOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * {@link ServletOutputStream} that writes to the specified output stream.
 *
 * @author Kohsuke Kawaguchi
 */
public class FilterServletOutputStream extends ServletOutputStream {
    private final OutputStream out;

    public FilterServletOutputStream(OutputStream out) {
        this.out = out;
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
        out.close();
    }

    @Override
    public void flush() throws IOException {
        out.flush();
    }
}
