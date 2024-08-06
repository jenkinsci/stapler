package io.jenkins.servlet;

import java.util.Objects;
import javax.servlet.ServletContextEvent;

public class ServletContextEventWrapper {
    public static jakarta.servlet.ServletContextEvent toJakartaServletContextEvent(ServletContextEvent from) {
        Objects.requireNonNull(from);
        return new jakarta.servlet.ServletContextEvent(
                ServletContextWrapper.toJakartaServletContext(from.getServletContext()));
    }

    public static ServletContextEvent fromJakartaServletContextEvent(jakarta.servlet.ServletContextEvent from) {
        Objects.requireNonNull(from);
        return new ServletContextEvent(ServletContextWrapper.fromJakartServletContext(from.getServletContext()));
    }
}
