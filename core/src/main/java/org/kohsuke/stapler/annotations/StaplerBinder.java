/*
 * Copyright (c) 2017, Stephen Connolly, CloudBees, Inc.
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

package org.kohsuke.stapler.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotates a static public factory method that will be used to instantiate objects of the defining class when that
 * class is used as a parameter type on a {@link StaplerMethod} (or on another {@link StaplerBinder}).
 * A class may only have one {@link StaplerBinder}
 * <p>
 * Example:
 * <pre>
 * {@code @StaplerObject}
 * public class Widget {
 *    {@code @StaplerBinder}
 *     public static Widget create(StaplerRequest req) {
 *         return ...;
 *     }
 * }</pre>
 * Then if there is an action method that needs the required type the factory method will be invoked prior to the
 * action method being invoked.
 * <pre>
 * {@code @StaplerObject}
 * public class Root {
 *    {@code @StaplerGET}
 *     public HttpResponse doSomething(Widget widget) {
 *         ...
 *     }
 * }
 * </pre>
 * <strong>NOTE: there are no protections against recursive {@link StaplerBinder} call trees.</strong>
 */
@Target(METHOD)
@Retention(RUNTIME)
@Documented
public @interface StaplerBinder {
}
