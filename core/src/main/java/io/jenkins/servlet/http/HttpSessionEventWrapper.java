package io.jenkins.servlet.http;

import java.util.Objects;
import javax.servlet.http.HttpSessionEvent;

public class HttpSessionEventWrapper {
    public static jakarta.servlet.http.HttpSessionEvent toJakartaHttpSessionEvent(HttpSessionEvent from) {
        Objects.requireNonNull(from);
        return new jakarta.servlet.http.HttpSessionEvent(HttpSessionWrapper.toJakartaHttpSession(from.getSession()));
    }

    public static HttpSessionEvent fromJakartaHttpSessionEvent(jakarta.servlet.http.HttpSessionEvent from) {
        Objects.requireNonNull(from);
        return new HttpSessionEvent(HttpSessionWrapper.fromJakartaHttpSession(from.getSession()));
    }
}
