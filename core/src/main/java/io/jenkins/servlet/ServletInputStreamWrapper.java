package io.jenkins.servlet;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;
import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;

public class ServletInputStreamWrapper {
    public static jakarta.servlet.ServletInputStream toJakartaServletInputStream(ServletInputStream from) {
        Objects.requireNonNull(from);
        return new jakarta.servlet.ServletInputStream() {
            @Override
            public int read() throws IOException {
                return from.read();
            }

            @Override
            public int readLine(byte[] b, int off, int len) throws IOException {
                return from.readLine(b, off, len);
            }

            @Override
            public int read(byte[] b) throws IOException {
                return from.read(b);
            }

            @Override
            public int read(byte[] b, int off, int len) throws IOException {
                return from.read(b, off, len);
            }

            @Override
            public byte[] readAllBytes() throws IOException {
                return from.readAllBytes();
            }

            @Override
            public byte[] readNBytes(int len) throws IOException {
                return from.readNBytes(len);
            }

            @Override
            public int readNBytes(byte[] b, int off, int len) throws IOException {
                return from.readNBytes(b, off, len);
            }

            @Override
            public long skip(long n) throws IOException {
                return from.skip(n);
            }

            @Override
            public int available() throws IOException {
                return from.available();
            }

            @Override
            public void close() throws IOException {
                from.close();
            }

            @Override
            public synchronized void mark(int readlimit) {
                from.mark(readlimit);
            }

            @Override
            public synchronized void reset() throws IOException {
                from.reset();
            }

            @Override
            public boolean markSupported() {
                return from.markSupported();
            }

            @Override
            public long transferTo(OutputStream out) throws IOException {
                return from.transferTo(out);
            }

            @Override
            public boolean isFinished() {
                return from.isFinished();
            }

            @Override
            public boolean isReady() {
                return from.isReady();
            }

            @Override
            public void setReadListener(jakarta.servlet.ReadListener readListener) {
                from.setReadListener(ReadListenerWrapper.fromJakartaReadListener(readListener));
            }
        };
    }

    public static ServletInputStream fromJakartaServletInputStream(jakarta.servlet.ServletInputStream from) {
        Objects.requireNonNull(from);
        return new ServletInputStream() {
            @Override
            public int readLine(byte[] b, int off, int len) throws IOException {
                return from.readLine(b, off, len);
            }

            @Override
            public int read(byte[] b) throws IOException {
                return from.read(b);
            }

            @Override
            public int read(byte[] b, int off, int len) throws IOException {
                return from.read(b, off, len);
            }

            @Override
            public byte[] readAllBytes() throws IOException {
                return from.readAllBytes();
            }

            @Override
            public byte[] readNBytes(int len) throws IOException {
                return from.readNBytes(len);
            }

            @Override
            public int readNBytes(byte[] b, int off, int len) throws IOException {
                return from.readNBytes(b, off, len);
            }

            @Override
            public long skip(long n) throws IOException {
                return from.skip(n);
            }

            @Override
            public int available() throws IOException {
                return from.available();
            }

            @Override
            public void close() throws IOException {
                from.close();
            }

            @Override
            public synchronized void mark(int readlimit) {
                from.mark(readlimit);
            }

            @Override
            public synchronized void reset() throws IOException {
                from.reset();
            }

            @Override
            public boolean markSupported() {
                return from.markSupported();
            }

            @Override
            public long transferTo(OutputStream out) throws IOException {
                return from.transferTo(out);
            }

            @Override
            public boolean isFinished() {
                return from.isFinished();
            }

            @Override
            public boolean isReady() {
                return from.isReady();
            }

            @Override
            public void setReadListener(ReadListener readListener) {
                from.setReadListener(ReadListenerWrapper.toJakartaReadListener(readListener));
            }

            @Override
            public int read() throws IOException {
                return from.read();
            }
        };
    }
}
