/*
 * Copyright (c) 2004-2010, Kohsuke Kawaguchi
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided
 * that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice, this list of
 *       conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright notice, this list of
 *       conditions and the following disclaimer in the documentation and/or other materials
 *       provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS
 * OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
 * AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER
 * IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF
 * THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.kohsuke.stapler.bind;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.MetaClass;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.WebApp;

/**
 * Handles to the object bound via {@link BoundObjectTable}.
 *
 * As {@link HttpResponse}, this object generates a redirect to the URL that it points to.
 *
 * @author Kohsuke Kawaguchi
 * @see MetaClass#buildDispatchers
 */
public abstract class Bound implements HttpResponse {
    /**
     * Explicitly unbind this object. The referenced object
     * won't be bound to URL anymore.
     */
    public abstract void release();

    /**
     * The URL where the object is bound to. This method
     * starts with '/' and thus always absolute within the current web server.
     */
    public abstract String getURL();

    /**
     * The URL where the object can be released, or {@code null} if not applicable.
     */
    public String getReleaseURL() {
        return null;
    }

    /**
     * Gets the bound object.
     */
    public abstract Object getTarget();

    /**
     * Returns a JavaScript expression which evaluates to a JavaScript proxy that
     * talks back to the bound object that this handle represents.
     */
    public final String getProxyScript() {
        return getProxyScript(getURL(), getTarget().getClass());
    }

    /**
     * Returns the URL to the proxy script for the specified {@link org.kohsuke.stapler.bind.Bound}.
     *
     * @param variableName the variable to assign to the bound object
     * @param bound the bound object, or {@code null} if none.
     * @return the URL to the proxy script for the specified {@link org.kohsuke.stapler.bind.Bound}, starting with the context path
     */
    public static String getProxyScriptURL(String variableName, Bound bound) {
        if (bound == null) {
            return Stapler.getCurrentRequest2().getContextPath() + BoundObjectTable.SCRIPT_PREFIX + "/null?var="
                    + variableName;
        } else {
            return bound.getProxyScriptURL(variableName);
        }
    }

    /**
     * Returns the URL for the standalone proxy script of this {@link org.kohsuke.stapler.bind.Bound}.
     *
     * @param variableName the name of the JS variable to assign
     * @return the URL for the standalone proxy script of this {@link org.kohsuke.stapler.bind.Bound}, starting with the context path
     */
    public final String getProxyScriptURL(String variableName) {
        final String methodsList =
                String.join(",", getBoundJavaScriptUrlNames(getTarget().getClass()));
        // The URL looks like it has some redundant elements, but only if it's not a WithWellKnownURL
        return Stapler.getCurrentRequest2().getContextPath() + BoundObjectTable.SCRIPT_PREFIX + getURL() + "?var="
                + variableName + "&methods=" + methodsList;
    }

    private static Set<String> getBoundJavaScriptUrlNames(Class<?> clazz) {
        Set<String> names = new HashSet<>();
        for (Method m : clazz.getMethods()) {
            if (m.getName().startsWith("js")) {
                names.add(camelize(m.getName().substring(2)));
            } else {
                JavaScriptMethod a = m.getAnnotation(JavaScriptMethod.class);
                if (a != null) {
                    if (a.name().length == 0) {
                        names.add(m.getName());
                    } else {
                        names.addAll(Arrays.asList(a.name()));
                    }
                }
            }
        }
        return names;
    }

    /**
     * Returns a collection of all JS bound methods of the target's type.
     * @return a collection of all JS bound methods of the target's type
     */
    public final Set<String> getBoundJavaScriptUrlNames() {
        return getBoundJavaScriptUrlNames(getTarget().getClass());
    }

    public static String getProxyScript(String url, Class<?> clazz) {
        return getProxyScript(url, getBoundJavaScriptUrlNames(clazz).toArray(String[]::new));
    }

    /**
     * Returns the Stapler proxy script for the specified URL and method names
     *
     * @param url URL to proxied object
     * @param methods list of method names
     * @return the Stapler proxy script for the specified URL and method names
     */
    public static String getProxyScript(String url, String[] methods) {
        final String crumb = WebApp.getCurrent().getCrumbIssuer().issueCrumb();
        final String methodNamesList = Arrays.stream(methods)
                .sorted()
                .map(it -> "'" + escapeQuotedString(it) + "'")
                .collect(Collectors.joining(","));
        return "makeStaplerProxy('" + escapeQuotedString(url) + "','" + crumb + "',[" + methodNamesList + "])";
    }

    private static String escapeQuotedString(String singleQuotedJsValue) {
        return singleQuotedJsValue.replace("\\", "\\\\").replace("'", "\\'");
    }

    private static String camelize(String name) {
        return Character.toLowerCase(name.charAt(0)) + name.substring(1);
    }
}
