package io.jenkins.servlet;

import java.util.Objects;
import javax.servlet.ServletException;

public class ServletExceptionWrapper {
    public static jakarta.servlet.ServletException toJakartaServletException(ServletException e) {
        Objects.requireNonNull(e);
        return new jakarta.servlet.ServletException(e.toString(), e);
    }

    public static ServletException fromJakartaServletException(jakarta.servlet.ServletException e) {
        Objects.requireNonNull(e);
        return new ServletException(e.toString(), e);
    }
}
