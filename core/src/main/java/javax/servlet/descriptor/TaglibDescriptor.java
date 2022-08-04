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

public interface TaglibDescriptor {
    String getTaglibURI();

    String getTaglibLocation();

    default jakarta.servlet.descriptor.TaglibDescriptor toJakartaTaglibDescriptor() {
        return new jakarta.servlet.descriptor.TaglibDescriptor() {
            @Override
            public String getTaglibURI() {
                return TaglibDescriptor.this.getTaglibURI();
            }

            @Override
            public String getTaglibLocation() {
                return TaglibDescriptor.this.getTaglibLocation();
            }
        };
    }

    static TaglibDescriptor fromJakartaTaglibDescriptor(
            jakarta.servlet.descriptor.TaglibDescriptor from) {
        return new TaglibDescriptor() {
            @Override
            public String getTaglibURI() {
                return from.getTaglibURI();
            }

            @Override
            public String getTaglibLocation() {
                return from.getTaglibLocation();
            }

            @Override
            public jakarta.servlet.descriptor.TaglibDescriptor toJakartaTaglibDescriptor() {
                return from;
            }
        };
    }
}
