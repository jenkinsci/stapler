package io.jenkins.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;
import java.util.Objects;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletResponse;

public class ServletResponseWrapper {
    public static jakarta.servlet.ServletResponse toJakartaServletResponse(ServletResponse from) {
        if (from instanceof JavaxServletResponseWrapper javax) {
            return javax.toJakartaServletResponse();
        }
        return new JakartaServletResponseWrapperImpl(from);
    }

    public static ServletResponse fromJakartaServletResponse(jakarta.servlet.ServletResponse from) {
        if (from instanceof JakartaServletResponseWrapper jakarta) {
            return jakarta.toJavaxServletResponse();
        }
        return new JavaxServletResponseWrapperImpl(from);
    }

    public interface JakartaServletResponseWrapper {
        ServletResponse toJavaxServletResponse();
    }

    private static class JakartaServletResponseWrapperImpl
            implements jakarta.servlet.ServletResponse, JakartaServletResponseWrapper {
        private final ServletResponse from;

        JakartaServletResponseWrapperImpl(ServletResponse from) {
            this.from = Objects.requireNonNull(from);
        }

        @Override
        public String getCharacterEncoding() {
            return from.getCharacterEncoding();
        }

        @Override
        public String getContentType() {
            return from.getContentType();
        }

        @Override
        public jakarta.servlet.ServletOutputStream getOutputStream() throws IOException {
            return ServletOutputStreamWrapper.toJakartaServletOutputStream(from.getOutputStream());
        }

        @Override
        public PrintWriter getWriter() throws IOException {
            return from.getWriter();
        }

        @Override
        public void setCharacterEncoding(String charset) {
            from.setCharacterEncoding(charset);
        }

        @Override
        public void setContentLength(int len) {
            from.setContentLength(len);
        }

        @Override
        public void setContentLengthLong(long len) {
            from.setContentLengthLong(len);
        }

        @Override
        public void setContentType(String type) {
            from.setContentType(type);
        }

        @Override
        public void setBufferSize(int size) {
            from.setBufferSize(size);
        }

        @Override
        public int getBufferSize() {
            return from.getBufferSize();
        }

        @Override
        public void flushBuffer() throws IOException {
            from.flushBuffer();
        }

        @Override
        public void resetBuffer() {
            from.resetBuffer();
        }

        @Override
        public boolean isCommitted() {
            return from.isCommitted();
        }

        @Override
        public void reset() {
            from.reset();
        }

        @Override
        public void setLocale(Locale loc) {
            from.setLocale(loc);
        }

        @Override
        public Locale getLocale() {
            return from.getLocale();
        }

        @Override
        public ServletResponse toJavaxServletResponse() {
            return from;
        }
    }

    public interface JavaxServletResponseWrapper {
        jakarta.servlet.ServletResponse toJakartaServletResponse();
    }

    private static class JavaxServletResponseWrapperImpl implements ServletResponse, JavaxServletResponseWrapper {
        private final jakarta.servlet.ServletResponse from;

        JavaxServletResponseWrapperImpl(jakarta.servlet.ServletResponse from) {
            this.from = Objects.requireNonNull(from);
        }

        @Override
        public String getCharacterEncoding() {
            return from.getCharacterEncoding();
        }

        @Override
        public String getContentType() {
            return from.getContentType();
        }

        @Override
        public ServletOutputStream getOutputStream() throws IOException {
            return ServletOutputStreamWrapper.fromJakartaServletOutputStream(from.getOutputStream());
        }

        @Override
        public PrintWriter getWriter() throws IOException {
            return from.getWriter();
        }

        @Override
        public void setCharacterEncoding(String charset) {
            from.setCharacterEncoding(charset);
        }

        @Override
        public void setContentLength(int len) {
            from.setContentLength(len);
        }

        @Override
        public void setContentLengthLong(long len) {
            from.setContentLengthLong(len);
        }

        @Override
        public void setContentType(String type) {
            from.setContentType(type);
        }

        @Override
        public void setBufferSize(int size) {
            from.setBufferSize(size);
        }

        @Override
        public int getBufferSize() {
            return from.getBufferSize();
        }

        @Override
        public void flushBuffer() throws IOException {
            from.flushBuffer();
        }

        @Override
        public void resetBuffer() {
            from.resetBuffer();
        }

        @Override
        public boolean isCommitted() {
            return from.isCommitted();
        }

        @Override
        public void reset() {
            from.reset();
        }

        @Override
        public void setLocale(Locale loc) {
            from.setLocale(loc);
        }

        @Override
        public Locale getLocale() {
            return from.getLocale();
        }

        @Override
        public jakarta.servlet.ServletResponse toJakartaServletResponse() {
            return from;
        }
    }
}
