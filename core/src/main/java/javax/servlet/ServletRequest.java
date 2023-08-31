/*
 * Copyright (c) 1997, 2019 Oracle and/or its affiliates and others.
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

package javax.servlet;

import com.infradna.tool.bridge_method_injector.WithBridgeMethods;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;

public interface ServletRequest {
    Object getAttribute(String name);

    Enumeration<String> getAttributeNames();

    String getCharacterEncoding();

    void setCharacterEncoding(String env) throws UnsupportedEncodingException;

    int getContentLength();

    long getContentLengthLong();

    String getContentType();

    @WithBridgeMethods(ServletInputStream.class)
    jakarta.servlet.ServletInputStream getInputStream() throws IOException;

    String getParameter(String name);

    Enumeration<String> getParameterNames();

    String[] getParameterValues(String name);

    Map<String, String[]> getParameterMap();

    String getProtocol();

    String getScheme();

    String getServerName();

    int getServerPort();

    BufferedReader getReader() throws IOException;

    String getRemoteAddr();

    String getRemoteHost();

    void setAttribute(String name, Object o);

    void removeAttribute(String name);

    Locale getLocale();

    Enumeration<Locale> getLocales();

    boolean isSecure();

    @WithBridgeMethods(RequestDispatcher.class)
    jakarta.servlet.RequestDispatcher getRequestDispatcher(String path);

    @Deprecated
    String getRealPath(String path);

    int getRemotePort();

    String getLocalName();

    String getLocalAddr();

    int getLocalPort();

    @WithBridgeMethods(ServletContext.class)
    jakarta.servlet.ServletContext getServletContext();

    // AsyncContext startAsync() throws IllegalStateException;

    // AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse) throws
    // IllegalStateException;

    // boolean isAsyncStarted();

    // boolean isAsyncSupported();

    // AsyncContext getAsyncContext();

    @WithBridgeMethods(DispatcherType.class)
    jakarta.servlet.DispatcherType getDispatcherType();

    default jakarta.servlet.ServletRequest toJakartaServletRequest() {
        return new jakarta.servlet.ServletRequest() {
            @Override
            public Object getAttribute(String name) {
                return ServletRequest.this.getAttribute(name);
            }

            @Override
            public Enumeration<String> getAttributeNames() {
                return ServletRequest.this.getAttributeNames();
            }

            @Override
            public String getCharacterEncoding() {
                return ServletRequest.this.getCharacterEncoding();
            }

            @Override
            public void setCharacterEncoding(String env) throws UnsupportedEncodingException {
                ServletRequest.this.setCharacterEncoding(env);
            }

            @Override
            public int getContentLength() {
                return ServletRequest.this.getContentLength();
            }

            @Override
            public long getContentLengthLong() {
                return ServletRequest.this.getContentLengthLong();
            }

            @Override
            public String getContentType() {
                return ServletRequest.this.getContentType();
            }

            @Override
            public jakarta.servlet.ServletInputStream getInputStream() throws IOException {
                return ServletRequest.this.getInputStream();
            }

            @Override
            public String getParameter(String name) {
                return ServletRequest.this.getParameter(name);
            }

            @Override
            public Enumeration<String> getParameterNames() {
                return ServletRequest.this.getParameterNames();
            }

            @Override
            public String[] getParameterValues(String name) {
                return ServletRequest.this.getParameterValues(name);
            }

            @Override
            public Map<String, String[]> getParameterMap() {
                return ServletRequest.this.getParameterMap();
            }

            @Override
            public String getProtocol() {
                return ServletRequest.this.getProtocol();
            }

            @Override
            public String getScheme() {
                return ServletRequest.this.getScheme();
            }

            @Override
            public String getServerName() {
                return ServletRequest.this.getServerName();
            }

            @Override
            public int getServerPort() {
                return ServletRequest.this.getServerPort();
            }

            @Override
            public BufferedReader getReader() throws IOException {
                return ServletRequest.this.getReader();
            }

            @Override
            public String getRemoteAddr() {
                return ServletRequest.this.getRemoteAddr();
            }

            @Override
            public String getRemoteHost() {
                return ServletRequest.this.getRemoteHost();
            }

            @Override
            public void setAttribute(String name, Object o) {
                ServletRequest.this.setAttribute(name, o);
            }

            @Override
            public void removeAttribute(String name) {
                ServletRequest.this.removeAttribute(name);
            }

            @Override
            public Locale getLocale() {
                return ServletRequest.this.getLocale();
            }

            @Override
            public Enumeration<Locale> getLocales() {
                return ServletRequest.this.getLocales();
            }

            @Override
            public boolean isSecure() {
                return ServletRequest.this.isSecure();
            }

            @Override
            public jakarta.servlet.RequestDispatcher getRequestDispatcher(String path) {
                return ServletRequest.this.getRequestDispatcher(path);
            }

            @Override
            public String getRealPath(String path) {
                return ServletRequest.this.getRealPath(path);
            }

            @Override
            public int getRemotePort() {
                return ServletRequest.this.getRemotePort();
            }

            @Override
            public String getLocalName() {
                return ServletRequest.this.getLocalName();
            }

            @Override
            public String getLocalAddr() {
                return ServletRequest.this.getLocalAddr();
            }

            @Override
            public int getLocalPort() {
                return ServletRequest.this.getLocalPort();
            }

            @Override
            public jakarta.servlet.ServletContext getServletContext() {
                return ServletRequest.this.getServletContext();
            }

            @Override
            public jakarta.servlet.AsyncContext startAsync() throws IllegalStateException {
                // TODO
                throw new UnsupportedOperationException();
            }

            @Override
            public jakarta.servlet.AsyncContext startAsync(
                    jakarta.servlet.ServletRequest servletRequest, jakarta.servlet.ServletResponse servletResponse)
                    throws IllegalStateException {
                // TODO
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean isAsyncStarted() {
                // TODO
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean isAsyncSupported() {
                // TODO
                throw new UnsupportedOperationException();
            }

            @Override
            public jakarta.servlet.AsyncContext getAsyncContext() {
                // TODO
                throw new UnsupportedOperationException();
            }

            @Override
            public jakarta.servlet.DispatcherType getDispatcherType() {
                return ServletRequest.this.getDispatcherType();
            }
        };
    }

    static ServletRequest fromJakartaServletRequest(jakarta.servlet.ServletRequest from) {
        return new ServletRequest() {
            @Override
            public Object getAttribute(String name) {
                return from.getAttribute(name);
            }

            @Override
            public Enumeration<String> getAttributeNames() {
                return from.getAttributeNames();
            }

            @Override
            public String getCharacterEncoding() {
                return from.getCharacterEncoding();
            }

            @Override
            public void setCharacterEncoding(String env) throws UnsupportedEncodingException {
                from.setCharacterEncoding(env);
            }

            @Override
            public int getContentLength() {
                return from.getContentLength();
            }

            @Override
            public long getContentLengthLong() {
                return from.getContentLengthLong();
            }

            @Override
            public String getContentType() {
                return from.getContentType();
            }

            @Override
            @WithBridgeMethods(value = ServletInputStream.class, adapterMethod = "fromJakartaServletInputStream")
            public jakarta.servlet.ServletInputStream getInputStream() throws IOException {
                return from.getInputStream();
            }

            private Object fromJakartaServletInputStream(
                    jakarta.servlet.ServletInputStream inputStream, Class<?> type) {
                return ServletInputStream.fromJakartaServletInputStream(inputStream);
            }

            @Override
            public String getParameter(String name) {
                return from.getParameter(name);
            }

            @Override
            public Enumeration<String> getParameterNames() {
                return from.getParameterNames();
            }

            @Override
            public String[] getParameterValues(String name) {
                return from.getParameterValues(name);
            }

            @Override
            public Map<String, String[]> getParameterMap() {
                return from.getParameterMap();
            }

            @Override
            public String getProtocol() {
                return from.getProtocol();
            }

            @Override
            public String getScheme() {
                return from.getScheme();
            }

            @Override
            public String getServerName() {
                return from.getServerName();
            }

            @Override
            public int getServerPort() {
                return from.getServerPort();
            }

            @Override
            public BufferedReader getReader() throws IOException {
                return from.getReader();
            }

            @Override
            public String getRemoteAddr() {
                return from.getRemoteAddr();
            }

            @Override
            public String getRemoteHost() {
                return from.getRemoteHost();
            }

            @Override
            public void setAttribute(String name, Object o) {
                from.setAttribute(name, o);
            }

            @Override
            public void removeAttribute(String name) {
                from.removeAttribute(name);
            }

            @Override
            public Locale getLocale() {
                return from.getLocale();
            }

            @Override
            public Enumeration<Locale> getLocales() {
                return from.getLocales();
            }

            @Override
            public boolean isSecure() {
                return from.isSecure();
            }

            @WithBridgeMethods(value = RequestDispatcher.class, adapterMethod = "fromJakartaRequestDispatcher")
            public jakarta.servlet.RequestDispatcher getRequestDispatcher(String path) {
                return from.getRequestDispatcher(path);
            }

            private Object fromJakartaRequestDispatcher(
                    jakarta.servlet.RequestDispatcher requestDispatcher, Class<?> type) {
                return RequestDispatcher.fromJakartaRequestDispatcher(requestDispatcher);
            }

            @Override
            public String getRealPath(String path) {
                return from.getRealPath(path);
            }

            @Override
            public int getRemotePort() {
                return from.getRemotePort();
            }

            @Override
            public String getLocalName() {
                return from.getLocalName();
            }

            @Override
            public String getLocalAddr() {
                return from.getLocalAddr();
            }

            @Override
            public int getLocalPort() {
                return from.getLocalPort();
            }

            @Override
            @WithBridgeMethods(value = ServletContext.class, adapterMethod = "fromJakartaServletContext")
            public jakarta.servlet.ServletContext getServletContext() {
                return from.getServletContext();
            }

            private Object fromJakartaServletContext(jakarta.servlet.ServletContext servletContext, Class<?> type) {
                return ServletContext.fromJakartServletContext(servletContext);
            }

            // @Override
            // public AsyncContext startAsync() throws IllegalStateException {
            //    throw new UnsupportedOperationException();
            // }

            // @Override
            // public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse)
            //        throws IllegalStateException {
            //    throw new UnsupportedOperationException();
            // }

            // @Override
            // public boolean isAsyncStarted() {
            //    return from.isAsyncStarted();
            // }

            // @Override
            // public boolean isAsyncSupported() {
            //    return from.isAsyncSupported();
            // }

            // @Override
            // public AsyncContext getAsyncContext() {
            //    throw new UnsupportedOperationException();
            // }

            @Override
            @WithBridgeMethods(value = DispatcherType.class, adapterMethod = "fromJakartaDispatcherType")
            public jakarta.servlet.DispatcherType getDispatcherType() {
                return from.getDispatcherType();
            }

            private Object fromJakartaDispatcherType(jakarta.servlet.DispatcherType dispatcherType, Class<?> type) {
                return DispatcherType.fromJakartaDispatcherType(dispatcherType);
            }

            @Override
            public jakarta.servlet.ServletRequest toJakartaServletRequest() {
                return from;
            }
        };
    }
}
