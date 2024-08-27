package io.jenkins.servlet.http;

import io.jenkins.servlet.ServletContextWrapper;
import java.util.Enumeration;
import java.util.Objects;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;

public class HttpSessionWrapper {
    public static jakarta.servlet.http.HttpSession toJakartaHttpSession(HttpSession from) {
        Objects.requireNonNull(from);
        return new jakarta.servlet.http.HttpSession() {
            @Override
            public long getCreationTime() {
                return from.getCreationTime();
            }

            @Override
            public String getId() {
                return from.getId();
            }

            @Override
            public long getLastAccessedTime() {
                return from.getLastAccessedTime();
            }

            @Override
            public jakarta.servlet.ServletContext getServletContext() {
                return ServletContextWrapper.toJakartaServletContext(from.getServletContext());
            }

            @Override
            public void setMaxInactiveInterval(int interval) {
                from.setMaxInactiveInterval(interval);
            }

            @Override
            public int getMaxInactiveInterval() {
                return from.getMaxInactiveInterval();
            }

            @Override
            public jakarta.servlet.http.HttpSessionContext getSessionContext() {
                return HttpSessionContextWrapper.toJakartaHttpSessionContext(from.getSessionContext());
            }

            @Override
            public Object getAttribute(String name) {
                return from.getAttribute(name);
            }

            @Override
            public Object getValue(String name) {
                return from.getValue(name);
            }

            @Override
            public Enumeration<String> getAttributeNames() {
                return from.getAttributeNames();
            }

            @Override
            public String[] getValueNames() {
                return from.getValueNames();
            }

            @Override
            public void setAttribute(String name, Object value) {
                from.setAttribute(name, value);
            }

            @Override
            public void putValue(String name, Object value) {
                from.putValue(name, value);
            }

            @Override
            public void removeAttribute(String name) {
                from.removeAttribute(name);
            }

            @Override
            public void removeValue(String name) {
                from.removeValue(name);
            }

            @Override
            public void invalidate() {
                from.invalidate();
            }

            @Override
            public boolean isNew() {
                return from.isNew();
            }
        };
    }

    public static HttpSession fromJakartaHttpSession(jakarta.servlet.http.HttpSession from) {
        Objects.requireNonNull(from);
        return new HttpSession() {
            @Override
            public long getCreationTime() {
                return from.getCreationTime();
            }

            @Override
            public String getId() {
                return from.getId();
            }

            @Override
            public long getLastAccessedTime() {
                return from.getLastAccessedTime();
            }

            @Override
            public ServletContext getServletContext() {
                return ServletContextWrapper.fromJakartServletContext(from.getServletContext());
            }

            @Override
            public void setMaxInactiveInterval(int interval) {
                from.setMaxInactiveInterval(interval);
            }

            @Override
            public int getMaxInactiveInterval() {
                return from.getMaxInactiveInterval();
            }

            @Override
            public HttpSessionContext getSessionContext() {
                return HttpSessionContextWrapper.fromJakartaHttpSessionContext(from.getSessionContext());
            }

            @Override
            public Object getAttribute(String name) {
                return from.getAttribute(name);
            }

            @Override
            public Object getValue(String name) {
                return from.getValue(name);
            }

            @Override
            public Enumeration<String> getAttributeNames() {
                return from.getAttributeNames();
            }

            @Override
            public String[] getValueNames() {
                return from.getValueNames();
            }

            @Override
            public void setAttribute(String name, Object value) {
                from.setAttribute(name, value);
            }

            @Override
            public void putValue(String name, Object value) {
                from.putValue(name, value);
            }

            @Override
            public void removeAttribute(String name) {
                from.removeAttribute(name);
            }

            @Override
            public void removeValue(String name) {
                from.removeValue(name);
            }

            @Override
            public void invalidate() {
                from.invalidate();
            }

            @Override
            public boolean isNew() {
                return from.isNew();
            }
        };
    }
}
