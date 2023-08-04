/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package javax.servlet.descriptor;

import java.util.Collection;

public interface JspPropertyGroupDescriptor {
    Collection<String> getUrlPatterns();

    String getElIgnored();

    String getPageEncoding();

    String getScriptingInvalid();

    String getIsXml();

    Collection<String> getIncludePreludes();

    Collection<String> getIncludeCodas();

    String getDeferredSyntaxAllowedAsLiteral();

    String getTrimDirectiveWhitespaces();

    String getDefaultContentType();

    String getBuffer();

    String getErrorOnUndeclaredNamespace();

    default jakarta.servlet.descriptor.JspPropertyGroupDescriptor toJakartaJspPropertyGroupDescriptor() {
        return new jakarta.servlet.descriptor.JspPropertyGroupDescriptor() {
            @Override
            public Collection<String> getUrlPatterns() {
                return JspPropertyGroupDescriptor.this.getUrlPatterns();
            }

            @Override
            public String getElIgnored() {
                return JspPropertyGroupDescriptor.this.getElIgnored();
            }

            @Override
            public String getPageEncoding() {
                return JspPropertyGroupDescriptor.this.getPageEncoding();
            }

            @Override
            public String getScriptingInvalid() {
                return JspPropertyGroupDescriptor.this.getScriptingInvalid();
            }

            @Override
            public String getIsXml() {
                return JspPropertyGroupDescriptor.this.getIsXml();
            }

            @Override
            public Collection<String> getIncludePreludes() {
                return JspPropertyGroupDescriptor.this.getIncludePreludes();
            }

            @Override
            public Collection<String> getIncludeCodas() {
                return JspPropertyGroupDescriptor.this.getIncludeCodas();
            }

            @Override
            public String getDeferredSyntaxAllowedAsLiteral() {
                return JspPropertyGroupDescriptor.this.getDeferredSyntaxAllowedAsLiteral();
            }

            @Override
            public String getTrimDirectiveWhitespaces() {
                return JspPropertyGroupDescriptor.this.getTrimDirectiveWhitespaces();
            }

            @Override
            public String getDefaultContentType() {
                return JspPropertyGroupDescriptor.this.getDefaultContentType();
            }

            @Override
            public String getBuffer() {
                return JspPropertyGroupDescriptor.this.getBuffer();
            }

            @Override
            public String getErrorOnUndeclaredNamespace() {
                return JspPropertyGroupDescriptor.this.getErrorOnUndeclaredNamespace();
            }
        };
    }

    static JspPropertyGroupDescriptor fromJakartaJspPropertyGroupDescriptor(
            jakarta.servlet.descriptor.JspPropertyGroupDescriptor from) {
        return new JspPropertyGroupDescriptor() {
            @Override
            public Collection<String> getUrlPatterns() {
                return from.getUrlPatterns();
            }

            @Override
            public String getElIgnored() {
                return from.getElIgnored();
            }

            @Override
            public String getPageEncoding() {
                return from.getPageEncoding();
            }

            @Override
            public String getScriptingInvalid() {
                return from.getScriptingInvalid();
            }

            @Override
            public String getIsXml() {
                return from.getIsXml();
            }

            @Override
            public Collection<String> getIncludePreludes() {
                return from.getIncludePreludes();
            }

            @Override
            public Collection<String> getIncludeCodas() {
                return from.getIncludeCodas();
            }

            @Override
            public String getDeferredSyntaxAllowedAsLiteral() {
                return from.getDeferredSyntaxAllowedAsLiteral();
            }

            @Override
            public String getTrimDirectiveWhitespaces() {
                return from.getTrimDirectiveWhitespaces();
            }

            @Override
            public String getDefaultContentType() {
                return from.getDefaultContentType();
            }

            @Override
            public String getBuffer() {
                return from.getBuffer();
            }

            @Override
            public String getErrorOnUndeclaredNamespace() {
                return from.getErrorOnUndeclaredNamespace();
            }

            @Override
            public jakarta.servlet.descriptor.JspPropertyGroupDescriptor toJakartaJspPropertyGroupDescriptor() {
                return from;
            }
        };
    }
}
