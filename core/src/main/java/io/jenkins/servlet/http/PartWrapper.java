package io.jenkins.servlet.http;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Objects;
import javax.servlet.http.Part;

public class PartWrapper {
    public static jakarta.servlet.http.Part toJakartaPart(Part from) {
        Objects.requireNonNull(from);
        return new jakarta.servlet.http.Part() {
            @Override
            public InputStream getInputStream() throws IOException {
                return from.getInputStream();
            }

            @Override
            public String getContentType() {
                return from.getContentType();
            }

            @Override
            public String getName() {
                return from.getName();
            }

            @Override
            public String getSubmittedFileName() {
                return from.getSubmittedFileName();
            }

            @Override
            public long getSize() {
                return from.getSize();
            }

            @Override
            public void write(String fileName) throws IOException {
                from.write(fileName);
            }

            @Override
            public void delete() throws IOException {
                from.delete();
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
        };
    }

    public static Part fromJakartaPart(jakarta.servlet.http.Part from) {
        Objects.requireNonNull(from);
        return new Part() {
            @Override
            public InputStream getInputStream() throws IOException {
                return from.getInputStream();
            }

            @Override
            public String getContentType() {
                return from.getContentType();
            }

            @Override
            public String getName() {
                return from.getName();
            }

            @Override
            public String getSubmittedFileName() {
                return from.getSubmittedFileName();
            }

            @Override
            public long getSize() {
                return from.getSize();
            }

            @Override
            public void write(String fileName) throws IOException {
                from.write(fileName);
            }

            @Override
            public void delete() throws IOException {
                from.delete();
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
        };
    }
}
