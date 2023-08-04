/*
 * Copyright (c) 1997-2018 Oracle and/or its affiliates and others.
 * All rights reserved.
 * Copyright 2004 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package javax.servlet.http;

import java.util.Enumeration;
import javax.servlet.ServletContext;

public interface HttpSession {
    long getCreationTime();

    String getId();

    long getLastAccessedTime();

    ServletContext getServletContext();

    void setMaxInactiveInterval(int interval);

    int getMaxInactiveInterval();

    @Deprecated
    HttpSessionContext getSessionContext();

    Object getAttribute(String name);

    @Deprecated
    Object getValue(String name);

    Enumeration<String> getAttributeNames();

    @Deprecated
    String[] getValueNames();

    void setAttribute(String name, Object value);

    @Deprecated
    void putValue(String name, Object value);

    void removeAttribute(String name);

    @Deprecated
    void removeValue(String name);

    void invalidate();

    boolean isNew();

    default jakarta.servlet.http.HttpSession toJakartaHttpSession() {
        return new jakarta.servlet.http.HttpSession() {
            @Override
            public long getCreationTime() {
                return HttpSession.this.getCreationTime();
            }

            @Override
            public String getId() {
                return HttpSession.this.getId();
            }

            @Override
            public long getLastAccessedTime() {
                return HttpSession.this.getLastAccessedTime();
            }

            @Override
            public jakarta.servlet.ServletContext getServletContext() {
                return HttpSession.this.getServletContext().toJakartaServletContext();
            }

            @Override
            public void setMaxInactiveInterval(int interval) {
                HttpSession.this.setMaxInactiveInterval(interval);
            }

            @Override
            public int getMaxInactiveInterval() {
                return HttpSession.this.getMaxInactiveInterval();
            }

            @Override
            public jakarta.servlet.http.HttpSessionContext getSessionContext() {
                return HttpSession.this.getSessionContext().toJakartaHttpSessionContext();
            }

            @Override
            public Object getAttribute(String name) {
                return HttpSession.this.getAttribute(name);
            }

            @Override
            public Object getValue(String name) {
                return HttpSession.this.getValue(name);
            }

            @Override
            public Enumeration<String> getAttributeNames() {
                return HttpSession.this.getAttributeNames();
            }

            @Override
            public String[] getValueNames() {
                return HttpSession.this.getValueNames();
            }

            @Override
            public void setAttribute(String name, Object value) {
                HttpSession.this.setAttribute(name, value);
            }

            @Override
            public void putValue(String name, Object value) {
                HttpSession.this.putValue(name, value);
            }

            @Override
            public void removeAttribute(String name) {
                HttpSession.this.removeAttribute(name);
            }

            @Override
            public void removeValue(String name) {
                HttpSession.this.removeValue(name);
            }

            @Override
            public void invalidate() {
                HttpSession.this.invalidate();
            }

            @Override
            public boolean isNew() {
                return HttpSession.this.isNew();
            }
        };
    }

    static HttpSession fromJakartaHttpSession(jakarta.servlet.http.HttpSession from) {
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
                return ServletContext.fromJakartServletContext(from.getServletContext());
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
                return HttpSessionContext.fromJakartaHttpSessionContext(from.getSessionContext());
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

            @Override
            public jakarta.servlet.http.HttpSession toJakartaHttpSession() {
                return from;
            }
        };
    }
}
