package io.jenkins.servlet.http;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Objects;
import javax.servlet.http.Cookie;

public class CookieWrapper {
    public static jakarta.servlet.http.Cookie toJakartaServletHttpCookie(Cookie from) {
        Objects.requireNonNull(from);
        jakarta.servlet.http.Cookie result = new jakarta.servlet.http.Cookie(from.getName(), from.getValue());
        if (from.getComment() != null) {
            result.setComment(from.getComment());
        }
        if (from.getDomain() != null) {
            result.setDomain(from.getDomain());
        }
        result.setMaxAge(from.getMaxAge());
        if (from.getPath() != null) {
            result.setPath(from.getPath());
        }
        result.setSecure(from.getSecure());
        result.setVersion(from.getVersion());
        result.setHttpOnly(from.isHttpOnly());
        return result;
    }

    @SuppressFBWarnings(
            value = {"HTTPONLY_COOKIE", "INSECURE_COOKIE"},
            justification = "for compatibility")
    public static Cookie fromJakartaServletHttpCookie(jakarta.servlet.http.Cookie from) {
        Objects.requireNonNull(from);
        Cookie result = new Cookie(from.getName(), from.getValue());
        if (from.getComment() != null) {
            result.setComment(from.getComment());
        }
        if (from.getDomain() != null) {
            result.setDomain(from.getDomain());
        }
        result.setMaxAge(from.getMaxAge());
        if (from.getPath() != null) {
            result.setPath(from.getPath());
        }
        result.setSecure(from.getSecure());
        result.setVersion(from.getVersion());
        result.setHttpOnly(from.isHttpOnly());
        return result;
    }
}
