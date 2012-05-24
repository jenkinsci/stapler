package org.kohsuke.stapler.compression;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.zip.GZIPOutputStream;

/**
 * {@link HttpServletResponse} that recognizes Content-Encoding: gzip in the response header
 * and acts accoringly.
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

    @Override
    public void setHeader(String name, String value) {
        super.setHeader(name, value);
        activateCompressionIfNecessary(name,value);
    }

    @Override
    public void addHeader(String name, String value) {
        super.addHeader(name, value);
        activateCompressionIfNecessary(name, value);
    }

    private void activateCompressionIfNecessary(String name, String value) {
        try {
            if (name.equals("Content-Encoding") && value.equals("gzip")) {
                if (stream==null)
                    stream = new FilterServletOutputStream(new GZIPOutputStream(super.getOutputStream()));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
}
