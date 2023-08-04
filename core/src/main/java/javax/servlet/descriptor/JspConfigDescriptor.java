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
import java.util.stream.Collectors;

public interface JspConfigDescriptor {
    Collection<TaglibDescriptor> getTaglibs();

    Collection<JspPropertyGroupDescriptor> getJspPropertyGroups();

    default jakarta.servlet.descriptor.JspConfigDescriptor toJakartaJspConfigDescriptor() {
        return new jakarta.servlet.descriptor.JspConfigDescriptor() {
            @Override
            public Collection<jakarta.servlet.descriptor.TaglibDescriptor> getTaglibs() {
                return JspConfigDescriptor.this.getTaglibs().stream()
                        .map(TaglibDescriptor::toJakartaTaglibDescriptor)
                        .collect(Collectors.toList());
            }

            @Override
            public Collection<jakarta.servlet.descriptor.JspPropertyGroupDescriptor> getJspPropertyGroups() {
                return JspConfigDescriptor.this.getJspPropertyGroups().stream()
                        .map(JspPropertyGroupDescriptor::toJakartaJspPropertyGroupDescriptor)
                        .collect(Collectors.toList());
            }
        };
    }

    static JspConfigDescriptor fromJakartaJspConfigDescriptor(jakarta.servlet.descriptor.JspConfigDescriptor from) {
        return new JspConfigDescriptor() {
            @Override
            public Collection<TaglibDescriptor> getTaglibs() {
                return from.getTaglibs().stream()
                        .map(TaglibDescriptor::fromJakartaTaglibDescriptor)
                        .collect(Collectors.toList());
            }

            @Override
            public Collection<JspPropertyGroupDescriptor> getJspPropertyGroups() {
                return from.getJspPropertyGroups().stream()
                        .map(JspPropertyGroupDescriptor::fromJakartaJspPropertyGroupDescriptor)
                        .collect(Collectors.toList());
            }

            @Override
            public jakarta.servlet.descriptor.JspConfigDescriptor toJakartaJspConfigDescriptor() {
                return from;
            }
        };
    }
}
