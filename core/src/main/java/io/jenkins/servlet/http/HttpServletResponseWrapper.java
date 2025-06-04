package io.jenkins.servlet.http;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.jenkins.servlet.ServletOutputStreamWrapper;
import io.jenkins.servlet.ServletResponseWrapper;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

public class HttpServletResponseWrapper {
    public static jakarta.servlet.http.HttpServletResponse toJakartaHttpServletResponse(HttpServletResponse from) {
        if (from instanceof JavaxHttpServletResponseWrapper javax) {
            return javax.toJakartaHttpServletResponse();
        }
        return new JakartaHttpServletResponseWrapperImpl(from);
    }

    public static HttpServletResponse fromJakartaHttpServletResponse(jakarta.servlet.http.HttpServletResponse from) {
        if (from instanceof JakartaHttpServletResponseWrapper jakarta) {
            return jakarta.toJavaxHttpServletResponse();
        }
        return new JavaxHttpServletResponseWrapperImpl(from);
    }

    public interface JakartaHttpServletResponseWrapper {
        HttpServletResponse toJavaxHttpServletResponse();
    }

    @SuppressFBWarnings(
            value = {"UNVALIDATED_REDIRECT", "URL_REWRITING", "XSS_SERVLET"},
            justification = "for compatibility")
    private static class JakartaHttpServletResponseWrapperImpl
            implements jakarta.servlet.http.HttpServletResponse,
                    ServletResponseWrapper.JakartaServletResponseWrapper,
                    JakartaHttpServletResponseWrapper {
        private final HttpServletResponse from;

        JakartaHttpServletResponseWrapperImpl(HttpServletResponse from) {
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
        public void addCookie(jakarta.servlet.http.Cookie cookie) {
            from.addCookie(CookieWrapper.fromJakartaServletHttpCookie(cookie));
        }

        @Override
        public boolean containsHeader(String name) {
            return from.containsHeader(name);
        }

        @Override
        public String encodeURL(String url) {
            return from.encodeURL(url);
        }

        @Override
        public String encodeRedirectURL(String url) {
            return from.encodeRedirectURL(url);
        }

        @Override
        public String encodeUrl(String url) {
            return from.encodeUrl(url);
        }

        @Override
        public String encodeRedirectUrl(String url) {
            return from.encodeRedirectUrl(url);
        }

        @Override
        public void sendError(int sc, String msg) throws IOException {
            from.sendError(sc, msg);
        }

        @Override
        public void sendError(int sc) throws IOException {
            from.sendError(sc);
        }

        @Override
        public void sendRedirect(String location) throws IOException {
            from.sendRedirect(location);
        }

        @Override
        public void setDateHeader(String name, long date) {
            from.setDateHeader(name, date);
        }

        @Override
        public void addDateHeader(String name, long date) {
            from.addDateHeader(name, date);
        }

        @Override
        public void setHeader(String name, String value) {
            from.setHeader(name, value);
        }

        @Override
        public void addHeader(String name, String value) {
            from.addHeader(name, value);
        }

        @Override
        public void setIntHeader(String name, int value) {
            from.setIntHeader(name, value);
        }

        @Override
        public void addIntHeader(String name, int value) {
            from.addIntHeader(name, value);
        }

        @Override
        public void setStatus(int sc) {
            from.setStatus(sc);
        }

        @Override
        public void setStatus(int sc, String sm) {
            from.setStatus(sc, sm);
        }

        @Override
        public int getStatus() {
            return from.getStatus();
        }

        @Override
        public String getHeader(String name) {
            return from.getHeader(name);
        }

        @Override
        public Collection<String> getHeaders(String name) {
            return from.getHeaders(name);
        }

        @Override
        public Collection<String> getHeaderNames() {
            return from.getHeaderNames();
        }

        @Override
        public void setTrailerFields(Supplier<Map<String, String>> supplier) {
            from.setTrailerFields(supplier);
        }

        @Override
        public Supplier<Map<String, String>> getTrailerFields() {
            return from.getTrailerFields();
        }

        @Override
        public ServletResponse toJavaxServletResponse() {
            return from;
        }

        @Override
        public HttpServletResponse toJavaxHttpServletResponse() {
            return from;
        }
    }

    public interface JavaxHttpServletResponseWrapper {
        jakarta.servlet.http.HttpServletResponse toJakartaHttpServletResponse();
    }

    private static class JavaxHttpServletResponseWrapperImpl
            implements HttpServletResponse,
                    ServletResponseWrapper.JavaxServletResponseWrapper,
                    JavaxHttpServletResponseWrapper {
        private final jakarta.servlet.http.HttpServletResponse from;

        JavaxHttpServletResponseWrapperImpl(jakarta.servlet.http.HttpServletResponse from) {
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
        public void addCookie(Cookie cookie) {
            from.addCookie(CookieWrapper.toJakartaServletHttpCookie(cookie));
        }

        @Override
        public boolean containsHeader(String name) {
            return from.containsHeader(name);
        }

        @Override
        @SuppressFBWarnings(value = "URL_REWRITING", justification = "for compatibility")
        public String encodeURL(String url) {
            return from.encodeURL(url);
        }

        @Override
        @SuppressFBWarnings(value = "URL_REWRITING", justification = "for compatibility")
        public String encodeRedirectURL(String url) {
            return from.encodeRedirectURL(url);
        }

        @Override
        @SuppressFBWarnings(value = "URL_REWRITING", justification = "for compatibility")
        public String encodeUrl(String url) {
            return from.encodeUrl(url);
        }

        @Override
        @SuppressFBWarnings(value = "URL_REWRITING", justification = "for compatibility")
        public String encodeRedirectUrl(String url) {
            return from.encodeRedirectUrl(url);
        }

        @Override
        public void sendError(int sc, String msg) throws IOException {
            from.sendError(sc, msg);
        }

        @Override
        public void sendError(int sc) throws IOException {
            from.sendError(sc);
        }

        @Override
        @SuppressFBWarnings(value = "UNVALIDATED_REDIRECT", justification = "for compatibility")
        public void sendRedirect(String location) throws IOException {
            from.sendRedirect(location);
        }

        @Override
        public void setDateHeader(String name, long date) {
            from.setDateHeader(name, date);
        }

        @Override
        public void addDateHeader(String name, long date) {
            from.addDateHeader(name, date);
        }

        @Override
        public void setHeader(String name, String value) {
            from.setHeader(name, value);
        }

        @Override
        public void addHeader(String name, String value) {
            from.addHeader(name, value);
        }

        @Override
        public void setIntHeader(String name, int value) {
            from.setIntHeader(name, value);
        }

        @Override
        public void addIntHeader(String name, int value) {
            from.addIntHeader(name, value);
        }

        @Override
        public void setStatus(int sc) {
            from.setStatus(sc);
        }

        @Override
        public void setStatus(int sc, String sm) {
            from.setStatus(sc, sm);
        }

        @Override
        public int getStatus() {
            return from.getStatus();
        }

        @Override
        public String getHeader(String name) {
            return from.getHeader(name);
        }

        @Override
        public Collection<String> getHeaders(String name) {
            return from.getHeaders(name);
        }

        @Override
        public Collection<String> getHeaderNames() {
            return from.getHeaderNames();
        }

        @Override
        public void setTrailerFields(Supplier<Map<String, String>> supplier) {
            from.setTrailerFields(supplier);
        }

        @Override
        public Supplier<Map<String, String>> getTrailerFields() {
            return from.getTrailerFields();
        }

        @Override
        public jakarta.servlet.ServletResponse toJakartaServletResponse() {
            return from;
        }

        @Override
        public jakarta.servlet.http.HttpServletResponse toJakartaHttpServletResponse() {
            return from;
        }
    }
}
