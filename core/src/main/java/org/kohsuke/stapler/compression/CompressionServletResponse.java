package org.kohsuke.stapler.compression;

import com.jcraft.jzlib.GZIPOutputStream;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

/**
 * {@link HttpServletResponse} that recognizes Content-Encoding: gzip in the response header
 * and acts accordingly.
 *
 * @author Kohsuke Kawaguchi
 */
public class CompressionServletResponse extends HttpServletResponseWrapper {
    /**
     * If not null, we are compressing the stream.
     */
    private ServletOutputStream stream;
    private PrintWriter writer;

    public CompressionServletResponse(HttpServletResponse response) {
        super(response);
    }

    /**
     * If we've already inserted gzip compression filter, then we can't let the content length set from the app
     * because that refers to the size of the uncompressed content, where we actually need the size of the compressed
     * content.
     *
     * For the same reason, if the content length is set before we can decide whether to
     */
    @Override
    public void setContentLength(int len) {
        if (stream!=null)       return;
        super.setContentLength(len);
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        if (writer!=null)   return writer;
        if (stream!=null) {
            writer = new PrintWriter(new OutputStreamWriter(stream,getCharacterEncoding()));
            return writer;
        }
        return super.getWriter();
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        if (stream!=null)   return stream;
        return super.getOutputStream();
    }

    public void activate() throws IOException {
        if (stream==null) {
            super.setHeader("Content-Encoding", "gzip");
            stream = new FilterServletOutputStream(new GZIPOutputStream(super.getOutputStream()));
        }
    }

    public void close() throws IOException {
        if (stream!=null)
            stream.close();
    }
}
