package io.jenkins.servlet;

import java.util.Objects;
import javax.servlet.SessionCookieConfig;

public class SessionCookieConfigWrapper {
    public static jakarta.servlet.SessionCookieConfig toJakartaSessionCookieConfig(SessionCookieConfig from) {
        Objects.requireNonNull(from);
        return new jakarta.servlet.SessionCookieConfig() {
            @Override
            public void setName(String name) {
                from.setName(name);
            }

            @Override
            public String getName() {
                return from.getName();
            }

            @Override
            public void setDomain(String domain) {
                from.setDomain(domain);
            }

            @Override
            public String getDomain() {
                return from.getDomain();
            }

            @Override
            public void setPath(String path) {
                from.setPath(path);
            }

            @Override
            public String getPath() {
                return from.getPath();
            }

            @Override
            public void setComment(String comment) {
                from.setComment(comment);
            }

            @Override
            public String getComment() {
                return from.getComment();
            }

            @Override
            public void setHttpOnly(boolean httpOnly) {
                from.setHttpOnly(httpOnly);
            }

            @Override
            public boolean isHttpOnly() {
                return from.isHttpOnly();
            }

            @Override
            public void setSecure(boolean secure) {
                from.setSecure(secure);
            }

            @Override
            public boolean isSecure() {
                return from.isSecure();
            }

            @Override
            public void setMaxAge(int maxAge) {
                from.setMaxAge(maxAge);
            }

            @Override
            public int getMaxAge() {
                return from.getMaxAge();
            }
        };
    }

    public static SessionCookieConfig fromJakartaSessionCookieConfig(jakarta.servlet.SessionCookieConfig from) {
        Objects.requireNonNull(from);
        return new SessionCookieConfig() {
            @Override
            public void setName(String name) {
                from.setName(name);
            }

            @Override
            public String getName() {
                return from.getName();
            }

            @Override
            public void setDomain(String domain) {
                from.setDomain(domain);
            }

            @Override
            public String getDomain() {
                return from.getDomain();
            }

            @Override
            public void setPath(String path) {
                from.setPath(path);
            }

            @Override
            public String getPath() {
                return from.getPath();
            }

            @Override
            public void setComment(String comment) {
                from.setComment(comment);
            }

            @Override
            public String getComment() {
                return from.getComment();
            }

            @Override
            public void setHttpOnly(boolean httpOnly) {
                from.setHttpOnly(httpOnly);
            }

            @Override
            public boolean isHttpOnly() {
                return from.isHttpOnly();
            }

            @Override
            public void setSecure(boolean secure) {
                from.setSecure(secure);
            }

            @Override
            public boolean isSecure() {
                return from.isSecure();
            }

            @Override
            public void setMaxAge(int maxAge) {
                from.setMaxAge(maxAge);
            }

            @Override
            public int getMaxAge() {
                return from.getMaxAge();
            }
        };
    }
}
