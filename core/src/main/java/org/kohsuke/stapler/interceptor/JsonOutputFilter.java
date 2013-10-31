/*
 * Copyright (c) 2013, Robert Sandell
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
package org.kohsuke.stapler.interceptor;

import net.sf.json.JsonConfig;
import net.sf.json.util.PropertyFilter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.bind.JavaScriptMethod;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationTargetException;
import java.util.Set;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.util.Arrays;
import java.util.HashSet;

/**
 * Annotation for filtering the JSON data returned from a {@link JavaScriptMethod} annotated method.
 * Put this annotation on your js proxied method and provide the properties you want filtered.
 *
 * @author Robert Sandell &lt;sandell.robert@gmail.com&gt;
 */
@Retention(RUNTIME)
@Target({METHOD, FIELD})
@InterceptorAnnotation(JsonOutputFilter.Processor.class)
public @interface JsonOutputFilter {

    /**
     * White-list of properties to include in the output.
     */
    String[] includes() default {};

    /**
     * Black-list of properties to exclude from the output.
     */
    String[] excludes() default {};

    /**
     * If transient fields should be ignored. Default true.
     *
     * @see JsonConfig#isIgnoreTransientFields()
     */
    boolean ignoreTransient() default true;

    /**
     * If {@link JsonConfig#DEFAULT_EXCLUDES} should be ignored. Default false
     *
     * @see JsonConfig#isIgnoreDefaultExcludes()
     */
    boolean ignoreDefaultExcludes() default false;

    public static class Processor extends Interceptor {
        @Override
        public Object invoke(StaplerRequest request, StaplerResponse response, Object instance, Object[] arguments)
                throws IllegalAccessException, InvocationTargetException {
            JsonOutputFilter annotation = target.getAnnotation((JsonOutputFilter.class));
            if (annotation != null) {
                JsonConfig config = new JsonConfig();
                config.setJsonPropertyFilter(new FilterPropertyFilter(annotation.includes(), annotation.excludes()));
                config.setIgnoreTransientFields(annotation.ignoreTransient());
                config.setIgnoreDefaultExcludes(annotation.ignoreDefaultExcludes());
                response.setJsonConfig(config);
            }
            return target.invoke(request, response, instance, arguments);
        }
    }

    /**
     * Json Property filter for handling the include and exclude.
     */
    static class FilterPropertyFilter implements PropertyFilter {

        private Set<String> includes;
        private Set<String> excludes;

        public FilterPropertyFilter(String[] includes, String[] excludes) {
            this(new HashSet<String>(Arrays.asList(includes)), new HashSet<String>(Arrays.asList(excludes)));
        }

        public FilterPropertyFilter(Set<String> includes, Set<String> excludes) {
            this.includes = includes;
            this.excludes = excludes;
        }

        public boolean apply(Object source, String name, Object value) {
            if (excludes.contains(name)) {
                return true;
            } else if (!includes.isEmpty()) {
                return !includes.contains(name);
            } else {
                return false;
            }
        }
    }
}
