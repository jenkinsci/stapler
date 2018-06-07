/*
 * Copyright (c) 2018, Nicolas De Loof
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

package org.kohsuke.stapler;

import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;

import javax.annotation.PostConstruct;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Designates a field used to databind JSON values into objects in methods like
 * {@link StaplerRequest#bindJSON(Class, JSONObject)} and
 * {@link StaplerRequest#bindParameters(Class, String)}.
 *
 * <p>
 * Stapler will first invoke {@link DataBoundConstructor}-annotated constructor, and if there's any
 * remaining properties in JSON, it'll try to find a matching {@link DataBound}-annotated fields.
 *
 * <p>
 * To create a method to be called after all the setter injections are complete, annotate a method
 * with {@link PostConstruct}.
 *
 * @author <a href="mailto:nicolas.deloof@gmail.com">Nicolas De Loof</a>
 */
@Retention(RUNTIME)
@Target({FIELD})
@Documented
public @interface DataBound {

    enum Trim {
        NONE {
            @Override
            public Object apply(Object val) {
                return val;
            }
        }, TONULL {
            @Override
            public Object apply(Object val) {
                return StringUtils.trimToNull((String) val);
            }
        }, TOEMPTY {
            @Override
            public Object apply(Object val) {
                return StringUtils.trimToEmpty((String) val);
            }
        };

        public abstract Object apply(Object val);

    }

    /**
     * Define pre-processing of string-based values before they get injected and validated.
     */
    Trim trim() default Trim.NONE;
}
