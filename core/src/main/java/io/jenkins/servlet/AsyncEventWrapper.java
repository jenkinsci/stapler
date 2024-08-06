package io.jenkins.servlet;

import java.util.Objects;
import javax.servlet.AsyncEvent;

public class AsyncEventWrapper {
    public static jakarta.servlet.AsyncEvent toJakartaServletHttpAsyncEvent(AsyncEvent from) {
        Objects.requireNonNull(from);
        return new jakarta.servlet.AsyncEvent(
                AsyncContextWrapper.toJakartaAsyncContext(from.getAsyncContext()),
                io.jenkins.servlet.ServletRequestWrapper.toJakartaServletRequest(from.getSuppliedRequest()),
                ServletResponseWrapper.toJakartaServletResponse(from.getSuppliedResponse()),
                from.getThrowable());
    }

    public static AsyncEvent fromJakartaServletHttpAsyncEvent(jakarta.servlet.AsyncEvent from) {
        Objects.requireNonNull(from);
        return new AsyncEvent(
                AsyncContextWrapper.fromJakartaAsyncContext(from.getAsyncContext()),
                ServletRequestWrapper.fromJakartaServletRequest(from.getSuppliedRequest()),
                ServletResponseWrapper.fromJakartaServletResponse(from.getSuppliedResponse()),
                from.getThrowable());
    }
}
