package org.kohsuke.stapler;

import java.io.FilterInputStream;
import java.io.InputStream;
import java.io.IOException;

/**
 * {@link InputStream} decorator that chops off the underlying stream at the specified length
 *
 * @author Kohsuke Kawaguchi
 */
final class TruncatedInputStream extends FilterInputStream {
    private long len;

    public TruncatedInputStream(InputStream in, long len) {
        super(in);
        this.len = len;
    }

    @Override
    public int read() throws IOException {
        if(len<=0)
            return -1;
        len--;
        return super.read();
    }

    @Override
    public int read(byte[] b, int off, int l) throws IOException {
        if(l<=0)  return -1;

        int r = super.read(b, off, (int)Math.min(l,len));
        if(r>0)
            len -= r;
        return r;
    }

    @Override
    public int available() throws IOException {
        return (int)Math.min(super.available(),len);
    }

    @Override
    public long skip(long n) throws IOException {
        long r = super.skip(Math.min(len, n));
        len -= r;
        return r;
    }
}
