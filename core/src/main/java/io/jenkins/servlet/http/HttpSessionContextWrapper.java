package io.jenkins.servlet.http;

import java.util.Enumeration;
import java.util.Objects;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;

public class HttpSessionContextWrapper {
    public static jakarta.servlet.http.HttpSessionContext toJakartaHttpSessionContext(HttpSessionContext from) {
        Objects.requireNonNull(from);
        return new jakarta.servlet.http.HttpSessionContext() {
            @Override
            public jakarta.servlet.http.HttpSession getSession(String sessionId) {
                return HttpSessionWrapper.toJakartaHttpSession(from.getSession(sessionId));
            }

            @Override
            public Enumeration<String> getIds() {
                return from.getIds();
            }
        };
    }

    public static HttpSessionContext fromJakartaHttpSessionContext(jakarta.servlet.http.HttpSessionContext from) {
        Objects.requireNonNull(from);
        return new HttpSessionContext() {
            @Override
            public HttpSession getSession(String sessionId) {
                return HttpSessionWrapper.fromJakartaHttpSession(from.getSession(sessionId));
            }

            @Override
            public Enumeration<String> getIds() {
                return from.getIds();
            }
        };
    }
}
