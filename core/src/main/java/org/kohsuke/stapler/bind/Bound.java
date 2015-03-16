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

import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.MetaClass;
import org.kohsuke.stapler.WebApp;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

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
        StringBuilder buf = new StringBuilder("makeStaplerProxy('").append(getURL()).append("','").append(
                WebApp.getCurrent().getCrumbIssuer().issueCrumb()
        ).append("',[");

        boolean first=true;
        for (Method m : getTarget().getClass().getMethods()) {
            Collection<String> names;
            if (m.getName().startsWith("js")) {
                names = Collections.singleton(camelize(m.getName().substring(2)));
            } else {
                JavaScriptMethod a = m.getAnnotation(JavaScriptMethod.class);
                if (a!=null) {
                    names = Arrays.asList(a.name());
                    if (names.isEmpty())
                        names = Collections.singleton(m.getName());
                } else
                    continue;
            }

            for (String n : names) {
                if (first)  first = false;
                else        buf.append(',');
                buf.append('\'').append(n).append('\'');
            }
        }
        buf.append("])");
        
        return buf.toString();
    }

    private static String camelize(String name) {
        return Character.toLowerCase(name.charAt(0))+name.substring(1);
    }
}
