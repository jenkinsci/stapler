package io.jenkins.servlet;

import java.io.IOException;
import java.util.Objects;
import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;

public class ServletOutputStreamWrapper {
    public static jakarta.servlet.ServletOutputStream toJakartaServletOutputStream(ServletOutputStream from) {
        Objects.requireNonNull(from);
        return new jakarta.servlet.ServletOutputStream() {
            @Override
            public void print(String s) throws IOException {
                from.print(s);
            }

            @Override
            public void print(boolean b) throws IOException {
                from.print(b);
            }

            @Override
            public void print(char c) throws IOException {
                from.print(c);
            }

            @Override
            public void print(int i) throws IOException {
                from.print(i);
            }

            @Override
            public void print(long l) throws IOException {
                from.print(l);
            }

            @Override
            public void print(float f) throws IOException {
                from.print(f);
            }

            @Override
            public void print(double d) throws IOException {
                from.print(d);
            }

            @Override
            public void println() throws IOException {
                from.println();
            }

            @Override
            public void println(String s) throws IOException {
                from.println(s);
            }

            @Override
            public void println(boolean b) throws IOException {
                from.println(b);
            }

            @Override
            public void println(char c) throws IOException {
                from.println(c);
            }

            @Override
            public void println(int i) throws IOException {
                from.println(i);
            }

            @Override
            public void println(long l) throws IOException {
                from.println(l);
            }

            @Override
            public void println(float f) throws IOException {
                from.println(f);
            }

            @Override
            public void println(double d) throws IOException {
                from.println(d);
            }

            @Override
            public void write(byte[] b) throws IOException {
                from.write(b);
            }

            @Override
            public void write(byte[] b, int off, int len) throws IOException {
                from.write(b, off, len);
            }

            @Override
            public void flush() throws IOException {
                from.flush();
            }

            @Override
            public void close() throws IOException {
                from.close();
            }

            @Override
            public boolean isReady() {
                return from.isReady();
            }

            @Override
            public void setWriteListener(jakarta.servlet.WriteListener writeListener) {
                from.setWriteListener(WriteListenerWrapper.fromJakartaWriteListener(writeListener));
            }

            @Override
            public void write(int b) throws IOException {
                from.write(b);
            }
        };
    }

    public static ServletOutputStream fromJakartaServletOutputStream(jakarta.servlet.ServletOutputStream from) {
        Objects.requireNonNull(from);
        return new ServletOutputStream() {
            @Override
            public void print(String s) throws IOException {
                from.print(s);
            }

            @Override
            public void print(boolean b) throws IOException {
                from.print(b);
            }

            @Override
            public void print(char c) throws IOException {
                from.print(c);
            }

            @Override
            public void print(int i) throws IOException {
                from.print(i);
            }

            @Override
            public void print(long l) throws IOException {
                from.print(l);
            }

            @Override
            public void print(float f) throws IOException {
                from.print(f);
            }

            @Override
            public void print(double d) throws IOException {
                from.print(d);
            }

            @Override
            public void println() throws IOException {
                from.println();
            }

            @Override
            public void println(String s) throws IOException {
                from.println(s);
            }

            @Override
            public void println(boolean b) throws IOException {
                from.println(b);
            }

            @Override
            public void println(char c) throws IOException {
                from.println(c);
            }

            @Override
            public void println(int i) throws IOException {
                from.println(i);
            }

            @Override
            public void println(long l) throws IOException {
                from.println(l);
            }

            @Override
            public void println(float f) throws IOException {
                from.println(f);
            }

            @Override
            public void println(double d) throws IOException {
                from.println(d);
            }

            @Override
            public void write(byte[] b) throws IOException {
                from.write(b);
            }

            @Override
            public void write(byte[] b, int off, int len) throws IOException {
                from.write(b, off, len);
            }

            @Override
            public void flush() throws IOException {
                from.flush();
            }

            @Override
            public void close() throws IOException {
                from.close();
            }

            @Override
            public boolean isReady() {
                return from.isReady();
            }

            @Override
            public void setWriteListener(WriteListener writeListener) {
                from.setWriteListener(WriteListenerWrapper.toJakartaWriteListener(writeListener));
            }

            @Override
            public void write(int b) throws IOException {
                from.write(b);
            }
        };
    }
}
