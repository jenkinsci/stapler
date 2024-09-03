package io.jenkins.servlet;

import java.io.IOException;
import java.util.Objects;
import javax.servlet.ReadListener;

public class ReadListenerWrapper {
    public static jakarta.servlet.ReadListener toJakartaReadListener(ReadListener from) {
        Objects.requireNonNull(from);
        return new jakarta.servlet.ReadListener() {
            @Override
            public void onDataAvailable() throws IOException {
                from.onDataAvailable();
            }

            @Override
            public void onAllDataRead() throws IOException {
                from.onAllDataRead();
            }

            @Override
            public void onError(Throwable throwable) {
                from.onError(throwable);
            }
        };
    }

    public static ReadListener fromJakartaReadListener(jakarta.servlet.ReadListener from) {
        Objects.requireNonNull(from);
        return new ReadListener() {
            @Override
            public void onDataAvailable() throws IOException {
                from.onDataAvailable();
            }

            @Override
            public void onAllDataRead() throws IOException {
                from.onAllDataRead();
            }

            @Override
            public void onError(Throwable t) {
                from.onError(t);
            }
        };
    }
}
