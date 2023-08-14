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

import java.util.HashSet;
import java.util.Set;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.MetaClass;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.WebApp;

import java.lang.reflect.Method;
import java.util.Arrays;

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
     * Returns the URL to the proxy script for this {@link org.kohsuke.stapler.bind.Bound}
     * @param variableName the variable to assign to the bound object
     * @param bound the bound object, or {@code null} if none.
     * @return
     */
    public static String getProxyScriptUrl(String variableName, Bound bound) {
        if (bound == null) {
            // TODO Should this hardocde a non-UUID to ensure null bind?
            return Stapler.getCurrentRequest().getContextPath() + BoundObjectTable.PREFIX + variableName + "/null";
        } else {
            return bound.getProxyScriptURL(variableName);
        }
    }

    /**
     * Returns the URL for the standalone proxy script.
     * @param variableName the name of the JS variable to assign
     * @return
     */
    public final String getProxyScriptURL(String variableName) {
        // TODO Probably wrong with WithWellKnownURL?
        return getURL().replace(BoundObjectTable.PREFIX, BoundObjectTable.SCRIPT_PREFIX + variableName + "/");
    }

    private static Set<String> getBoundMethods(Class<?> clazz) {
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
     * Returns a list of all JS bound methods of the target's type.
     * @return a list of all JS bound methods of the target's type
     */
    public final Set<String> getBoundMethods() {
        return getBoundMethods(getTarget().getClass());
    }

    public static String getProxyScript(String url, Class<?> clazz) {
        StringBuilder buf = new StringBuilder("makeStaplerProxy('").append(url).append("','").append(
                WebApp.getCurrent().getCrumbIssuer().issueCrumb()
        ).append("',[").append(
                StringUtils.join(getBoundMethods(clazz).stream().map(n -> "'" + n + "'").toArray(String[]::new), ",")).append("])");
        return buf.toString();
    }

    private static String camelize(String name) {
        return Character.toLowerCase(name.charAt(0))+name.substring(1);
    }
}
