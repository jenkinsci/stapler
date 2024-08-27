package io.jenkins.servlet;

import io.jenkins.servlet.http.HttpServletRequestWrapper;
import io.jenkins.servlet.http.HttpServletResponseWrapper;
import java.util.Objects;
import javax.servlet.AsyncContext;
import javax.servlet.AsyncListener;
import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class AsyncContextWrapper {
    public static jakarta.servlet.AsyncContext toJakartaAsyncContext(AsyncContext from) {
        Objects.requireNonNull(from);
        return new jakarta.servlet.AsyncContext() {
            @Override
            public jakarta.servlet.ServletRequest getRequest() {
                ServletRequest request = from.getRequest();
                return request instanceof HttpServletRequest
                        ? HttpServletRequestWrapper.toJakartaHttpServletRequest(((HttpServletRequest) request))
                        : io.jenkins.servlet.ServletRequestWrapper.toJakartaServletRequest(request);
            }

            @Override
            public jakarta.servlet.ServletResponse getResponse() {
                ServletResponse response = from.getResponse();
                return response instanceof HttpServletResponse
                        ? HttpServletResponseWrapper.toJakartaHttpServletResponse(((HttpServletResponse) response))
                        : ServletResponseWrapper.toJakartaServletResponse(response);
            }

            @Override
            public boolean hasOriginalRequestAndResponse() {
                return from.hasOriginalRequestAndResponse();
            }

            @Override
            public void dispatch() {
                from.dispatch();
            }

            @Override
            public void dispatch(String path) {
                from.dispatch(path);
            }

            @Override
            public void dispatch(jakarta.servlet.ServletContext context, String path) {
                from.dispatch(ServletContextWrapper.fromJakartServletContext(context), path);
            }

            @Override
            public void complete() {
                from.complete();
            }

            @Override
            public void start(Runnable run) {
                from.start(run);
            }

            @Override
            public void addListener(jakarta.servlet.AsyncListener listener) {
                from.addListener(AsyncListenerWrapper.fromJakartaAsyncListener(listener));
            }

            @Override
            public void addListener(
                    jakarta.servlet.AsyncListener listener,
                    jakarta.servlet.ServletRequest servletRequest,
                    jakarta.servlet.ServletResponse servletResponse) {
                from.addListener(
                        AsyncListenerWrapper.fromJakartaAsyncListener(listener),
                        servletRequest instanceof jakarta.servlet.http.HttpServletRequest
                                ? HttpServletRequestWrapper.fromJakartaHttpServletRequest(
                                        (jakarta.servlet.http.HttpServletRequest) servletRequest)
                                : io.jenkins.servlet.ServletRequestWrapper.fromJakartaServletRequest(servletRequest),
                        servletResponse instanceof jakarta.servlet.http.HttpServletResponse
                                ? HttpServletResponseWrapper.fromJakartaHttpServletResponse(
                                        (jakarta.servlet.http.HttpServletResponse) servletResponse)
                                : ServletResponseWrapper.fromJakartaServletResponse(servletResponse));
            }

            @Override
            public <T extends jakarta.servlet.AsyncListener> T createListener(Class<T> clazz) {
                // TODO implement this
                throw new UnsupportedOperationException();
            }

            @Override
            public void setTimeout(long timeout) {
                from.setTimeout(timeout);
            }

            @Override
            public long getTimeout() {
                return from.getTimeout();
            }
        };
    }

    public static AsyncContext fromJakartaAsyncContext(jakarta.servlet.AsyncContext from) {
        Objects.requireNonNull(from);
        return new AsyncContext() {
            @Override
            public ServletRequest getRequest() {
                jakarta.servlet.ServletRequest request = from.getRequest();
                return request instanceof jakarta.servlet.http.HttpServletRequest
                        ? HttpServletRequestWrapper.fromJakartaHttpServletRequest(
                                (jakarta.servlet.http.HttpServletRequest) request)
                        : ServletRequestWrapper.fromJakartaServletRequest(request);
            }

            @Override
            public ServletResponse getResponse() {
                jakarta.servlet.ServletResponse response = from.getResponse();
                return response instanceof jakarta.servlet.http.HttpServletResponse
                        ? HttpServletResponseWrapper.fromJakartaHttpServletResponse(
                                (jakarta.servlet.http.HttpServletResponse) response)
                        : ServletResponseWrapper.fromJakartaServletResponse(response);
            }

            @Override
            public boolean hasOriginalRequestAndResponse() {
                return from.hasOriginalRequestAndResponse();
            }

            @Override
            public void dispatch() {
                from.dispatch();
            }

            @Override
            public void dispatch(String path) {
                from.dispatch(path);
            }

            @Override
            public void dispatch(ServletContext context, String path) {
                from.dispatch(ServletContextWrapper.toJakartaServletContext(context), path);
            }

            @Override
            public void complete() {
                from.complete();
            }

            @Override
            public void start(Runnable run) {
                from.start(run);
            }

            @Override
            public void addListener(AsyncListener listener) {
                from.addListener(AsyncListenerWrapper.toJakartaAsyncListener(listener));
            }

            @Override
            public void addListener(
                    AsyncListener listener, ServletRequest servletRequest, ServletResponse servletResponse) {
                from.addListener(
                        AsyncListenerWrapper.toJakartaAsyncListener(listener),
                        servletRequest instanceof HttpServletRequest
                                ? HttpServletRequestWrapper.toJakartaHttpServletRequest(
                                        ((HttpServletRequest) servletRequest))
                                : io.jenkins.servlet.ServletRequestWrapper.toJakartaServletRequest(servletRequest),
                        servletResponse instanceof HttpServletResponse
                                ? HttpServletResponseWrapper.toJakartaHttpServletResponse(
                                        ((HttpServletResponse) servletResponse))
                                : ServletResponseWrapper.toJakartaServletResponse(servletResponse));
            }

            @Override
            public <T extends AsyncListener> T createListener(Class<T> clazz) {
                // TODO implement this
                throw new UnsupportedOperationException();
            }

            @Override
            public void setTimeout(long timeout) {
                from.setTimeout(timeout);
            }

            @Override
            public long getTimeout() {
                return from.getTimeout();
            }
        };
    }
}
