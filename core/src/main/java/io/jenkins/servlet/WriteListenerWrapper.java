package io.jenkins.servlet;

import java.io.IOException;
import java.util.Objects;
import javax.servlet.WriteListener;

public class WriteListenerWrapper {
    public static jakarta.servlet.WriteListener toJakartaWriteListener(WriteListener from) {
        Objects.requireNonNull(from);
        return new jakarta.servlet.WriteListener() {
            @Override
            public void onWritePossible() throws IOException {
                from.onWritePossible();
            }

            @Override
            public void onError(Throwable t) {
                from.onError(t);
            }
        };
    }

    public static WriteListener fromJakartaWriteListener(jakarta.servlet.WriteListener from) {
        Objects.requireNonNull(from);
        return new WriteListener() {
            @Override
            public void onWritePossible() throws IOException {
                from.onWritePossible();
            }

            @Override
            public void onError(Throwable t) {
                from.onError(t);
            }
        };
    }
}
