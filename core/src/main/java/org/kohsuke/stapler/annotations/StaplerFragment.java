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
import java.lang.annotation.Inherited;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import org.kohsuke.stapler.AbstractTearOff;
import org.kohsuke.stapler.Facet;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotation to document that the class expects there to be a {@link AbstractTearOff} with the specified name available.
 * If the annotation is not {@link #optional()} and the annotated type is either an {@code interface} or an
 * {@code abstract class} then the {@link AbstractTearOff} is not required available to the annotated class but must be
 * present on any concrete implementation subclasses.
 *
 * @since TODO
 */
@Target(TYPE)
@Retention(RUNTIME)
@Documented
@Repeatable(StaplerFragments.class)
@Inherited
public @interface StaplerFragment {
    /**
     * The name of the {@link AbstractTearOff} excluding the {@link AbstractTearOff#getDefaultScriptExtension()}, for
     * example {@code config} not {@code config.jelly} or {@code config.groovy}.
     *
     * @return the name of the {@link AbstractTearOff}.
     */
    String value();

    /**
     * Marks the {@link AbstractTearOff} as optional.
     * @return {@code true} if the {@link AbstractTearOff} is optional.
     */
    boolean optional() default false;
}
