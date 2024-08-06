package io.jenkins.servlet;

import java.util.Objects;
import javax.servlet.SessionTrackingMode;

public class SessionTrackingModeWrapper {
    public static jakarta.servlet.SessionTrackingMode toJakartaSessionTrackingMode(SessionTrackingMode from) {
        Objects.requireNonNull(from);
        switch (from) {
            case COOKIE:
                return jakarta.servlet.SessionTrackingMode.COOKIE;
            case URL:
                return jakarta.servlet.SessionTrackingMode.URL;
            case SSL:
                return jakarta.servlet.SessionTrackingMode.SSL;
            default:
                throw new IllegalArgumentException("Unknown SessionTrackingMode: " + from);
        }
    }

    public static SessionTrackingMode fromJakartaSessionTrackingMode(jakarta.servlet.SessionTrackingMode from) {
        Objects.requireNonNull(from);
        switch (from) {
            case COOKIE:
                return SessionTrackingMode.COOKIE;
            case URL:
                return SessionTrackingMode.URL;
            case SSL:
                return SessionTrackingMode.SSL;
            default:
                throw new IllegalArgumentException("Unknown SessionTrackingMode: " + from);
        }
    }
}
