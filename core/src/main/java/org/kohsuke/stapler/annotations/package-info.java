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

/**
 * Stapler routing annotations.
 *
 * This package contains a consolidated set of annotations used to determine how requests get routed to their handlers
 * by {@link org.kohsuke.stapler.Stapler}. The annotations can be grouped into four sections:
 * <ul>
 * <li>URI path binding annotations:
 * <ul><li>{@link org.kohsuke.stapler.annotations.StaplerPath}</li>
 * <li>{@link org.kohsuke.stapler.annotations.StaplerPaths}</li>
 * </ul>
 * These annotations are valid on public methods and public fields. If a method is annotated with any of the
 * {@link org.kohsuke.stapler.annotations} annotations (technically with any annotation marked
 * with the {@link org.kohsuke.stapler.annotations.StaplerPath.Implicit} meta-annotation) it is assumed to have an
 * implicit annotation of {@link org.kohsuke.stapler.annotations.StaplerPath} with
 * {@link org.kohsuke.stapler.annotations.StaplerPath#INFER_FROM_NAME} unless an explicit
 * {@link org.kohsuke.stapler.annotations.StaplerPath} annotation has been provided.
 * <p>
 * For {@link org.kohsuke.stapler.annotations.StaplerObject} annotated types, only public methods and fields with
 * a {@link org.kohsuke.stapler.annotations.StaplerPath} (or
 * {@linkplain org.kohsuke.stapler.annotations.StaplerPath.Implicit implicitly} annotated)
 * will be bound to URIs.
 * </li>
 * <li>{@link javax.servlet.http.HttpServletRequest#getMethod()} filtering annotations:
 * <ul><li>{@link org.kohsuke.stapler.annotations.StaplerMethod}</li>
 * <li>{@link org.kohsuke.stapler.annotations.StaplerMethods}</li>
 * <li>{@link org.kohsuke.stapler.annotations.StaplerCONNECT}</li>
 * <li>{@link org.kohsuke.stapler.annotations.StaplerDELETE}</li>
 * <li>{@link org.kohsuke.stapler.annotations.StaplerGET}</li>
 * <li>{@link org.kohsuke.stapler.annotations.StaplerHEAD}</li>
 * <li>{@link org.kohsuke.stapler.annotations.StaplerPATCH}</li>
 * <li>{@link org.kohsuke.stapler.annotations.StaplerPOST}</li>
 * <li>{@link org.kohsuke.stapler.annotations.StaplerPUT}</li>
 * <li>{@link org.kohsuke.stapler.annotations.StaplerRMI} - this is annotation matches both
 * {@link javax.servlet.http.HttpServletRequest#getMethod()} and
 * {@link javax.servlet.http.HttpServletRequest#getContentType()} and is used to expose RMI to client side JavaScript
 * </li>
 * </ul>
 * These annotations are valid on public methods and have the
 * {@link org.kohsuke.stapler.annotations.StaplerPath.Implicit} meta-annotation which implies a
 * {@link org.kohsuke.stapler.annotations.StaplerPath}
 * with {@link org.kohsuke.stapler.annotations.StaplerPath#INFER_FROM_NAME} unless an explicit
 * {@link org.kohsuke.stapler.annotations.StaplerPath} annotation has been provided.
 * <p>
 * In the event of a method with multiple annotations in this category, one must match for the method to be selected.
 * <p>
 * In the event of multiple methods with different annotations in this category, only the matching method will be
 * selected.
 * <p>
 * In the event of multiple methods with the same equivalent annotations in this category, the most specific method
 * (base on other categories of annotations) will be selected. If there is no clear winner then the exact method to be
 * selected is undefined.
 * </li>
 * <li>{@link javax.servlet.http.HttpServletRequest#getContentType()} filtering annotations:
 * <ul><li>{@link org.kohsuke.stapler.annotations.StaplerContent}</li>
 * <li>{@link org.kohsuke.stapler.annotations.StaplerContents}</li>
 * <li>{@link org.kohsuke.stapler.annotations.StaplerRMI} - this is annotation matches both
 * {@link javax.servlet.http.HttpServletRequest#getMethod()} and
 * {@link javax.servlet.http.HttpServletRequest#getContentType()} and is used to expose RMI to client side JavaScript
 * </ul>
 * These annotations are valid on public methods and have the
 * {@link org.kohsuke.stapler.annotations.StaplerPath.Implicit} meta-annotation which implies a
 * {@link org.kohsuke.stapler.annotations.StaplerPath}
 * with {@link org.kohsuke.stapler.annotations.StaplerPath#INFER_FROM_NAME} unless an explicit
 * {@link org.kohsuke.stapler.annotations.StaplerPath} annotation has been provided.
 * <p>
 * In the event of a method with multiple annotations in this category, one must match for the method to be selected.
 * <p>
 * In the event of multiple methods with different annotations in this category, only the matching method will be
 * selected.
 * <p>
 * In the event of multiple methods with the same equivalent annotations in this category, the most specific method
 * (base on other categories of annotations) will be selected. If there is no clear winner then the exact method to be
 * selected is undefined.
 * </li>
 * <li>Class level annotations:
 * <ul>
 * <li>{@link org.kohsuke.stapler.annotations.StaplerFacet} and {@link org.kohsuke.stapler.annotations.StaplerFacets}
 * document expected {@link org.kohsuke.stapler.Facet}s that should be assoicated with the annotated class.
 * </li>
 * <li>{@link org.kohsuke.stapler.annotations.StaplerFragment} and
 * {@link org.kohsuke.stapler.annotations.StaplerFragments} document expected
 * {@link org.kohsuke.stapler.AbstractTearOff}s that should be assoicated with the annotated class.
 * </li>
 * <li>{@link org.kohsuke.stapler.annotations.StaplerObject} identifies classes that are completely annotated with
 * {@link org.kohsuke.stapler.annotations}-based annotations and therefore more complete consistency checks can
 * be enforced at compile time.
 * <p>
 * TODO: determine how to handle inheritance
 * </li>
 * </ul></li>
 * </ul>
 * @since TODO
 */
package org.kohsuke.stapler.annotations;
