package io.jenkins.servlet;

import java.io.IOException;
import java.util.Objects;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;

public class AsyncListenerWrapper {
    public static jakarta.servlet.AsyncListener toJakartaAsyncListener(AsyncListener from) {
        Objects.requireNonNull(from);
        return new jakarta.servlet.AsyncListener() {

            @Override
            public void onComplete(jakarta.servlet.AsyncEvent event) throws IOException {
                from.onComplete(AsyncEventWrapper.fromJakartaServletHttpAsyncEvent(event));
            }

            @Override
            public void onTimeout(jakarta.servlet.AsyncEvent event) throws IOException {
                from.onTimeout(AsyncEventWrapper.fromJakartaServletHttpAsyncEvent(event));
            }

            @Override
            public void onError(jakarta.servlet.AsyncEvent event) throws IOException {
                from.onError(AsyncEventWrapper.fromJakartaServletHttpAsyncEvent(event));
            }

            @Override
            public void onStartAsync(jakarta.servlet.AsyncEvent event) throws IOException {
                from.onStartAsync(AsyncEventWrapper.fromJakartaServletHttpAsyncEvent(event));
            }
        };
    }

    public static AsyncListener fromJakartaAsyncListener(jakarta.servlet.AsyncListener from) {
        Objects.requireNonNull(from);
        return new AsyncListener() {

            @Override
            public void onComplete(AsyncEvent event) throws IOException {
                from.onComplete(AsyncEventWrapper.toJakartaServletHttpAsyncEvent(event));
            }

            @Override
            public void onTimeout(AsyncEvent event) throws IOException {
                from.onTimeout(AsyncEventWrapper.toJakartaServletHttpAsyncEvent(event));
            }

            @Override
            public void onError(AsyncEvent event) throws IOException {
                from.onError(AsyncEventWrapper.toJakartaServletHttpAsyncEvent(event));
            }

            @Override
            public void onStartAsync(AsyncEvent event) throws IOException {
                from.onStartAsync(AsyncEventWrapper.toJakartaServletHttpAsyncEvent(event));
            }
        };
    }
}
